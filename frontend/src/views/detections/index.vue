<script setup lang="ts">
// FR-DETECTION-001/002; D18-API-020/021/022/061..063; TC-DETECTION-001..007
import {ref,reactive,computed,onMounted} from 'vue';import {useAuthStore} from '../../stores/auth';import {get,command,errorText,type Row,type Page} from '../../api/implemented';import {type Rule,type Batch,ruleTypes,batchStates} from '../../api/governance';
const auth=useAuthStore(),send=command(),rows=ref<Batch[]>([]),rules=ref<Rule[]>([]),findings=ref<Row[]>([]),selected=ref<Batch|null>(null),error=ref(''),message=ref(''),busy=ref(false),page=ref(1),total=ref(0),findingPage=ref(1),findingTotal=ref(0),datasetText=ref(''),form=reactive({ruleVersionId:'',batchNo:'',sourceType:auth.permissions.includes('quality:execute')?'ENGINE':'MANUAL'});
const chosenRule=computed(()=>rules.value.find(r=>r.versions.some(v=>v.id===form.ruleVersionId))),example=computed(()=>{const code=selected.value?.ruleSnapshot?.fieldCode||chosenRule.value?.fieldCode||'fieldCode';return JSON.stringify([{recordKey:'record-001',values:{[code]:'填写实际值或null'}}],null,2)});
async function load(){const p=await get<Page>('D18-API-020',{}, {page:String(page.value),size:'20'});rows.value=p.items as Batch[];total.value=p.total}
async function loadFindings(){if(!selected.value)return;const p=await get<Page>('D18-API-063',{id:selected.value.id},{page:String(findingPage.value),size:'20'});findings.value=p.items;findingTotal.value=p.total}
async function select(b:Batch){selected.value=await get<Batch>('D18-API-061',{id:b.id});findingPage.value=1;datasetText.value='';await loadFindings()}
async function run(f:()=>Promise<unknown>){busy.value=true;error.value='';message.value='';try{await f();await load()}catch(e){error.value=errorText(e)}finally{busy.value=false}}
async function create(){if(!chosenRule.value)throw new Error('请选择已发布规则版本');const b=await send<Batch>('D18-API-021',{}, {...form,orgId:chosenRule.value.orgId});form.batchNo='';await select(b);message.value=form.sourceType==='ENGINE'?'批次已登记，请提交数据集':'手工接收批次已登记，可在质量问题页登记异常'}
async function submit(){if(!selected.value?.ruleSnapshot)return;let rows:unknown;try{rows=JSON.parse(datasetText.value)}catch{throw new Error('数据格式错误，请提交JSON记录数组')}selected.value=await send<Batch>('D18-API-062',{id:selected.value.id},{assetId:selected.value.ruleSnapshot.assetId,expectedVersion:selected.value.version,rows});selected.value=await get<Batch>('D18-API-061',{id:selected.value.id});datasetText.value='';message.value='数据集已保存；执行前可以替换，执行后固定'}
async function execute(){if(!selected.value)return;selected.value=await send<Batch>('D18-API-022',{id:selected.value.id},{expectedVersion:selected.value.version});selected.value=await get<Batch>('D18-API-061',{id:selected.value.id});await loadFindings();message.value='检测已完成，异常候选已关联质量问题'}
async function fileChanged(e:Event){const file=(e.target as HTMLInputElement).files?.[0];if(!file)return;if(file.size>1048576)throw new Error('文件最多1MiB');datasetText.value=await file.text()}
function closeDetail(){selected.value=null;findings.value=[];findingTotal.value=0;datasetText.value=''}
onMounted(()=>run(async()=>{await load();if(auth.permissions.includes('rule:read'))rules.value=(await get<Page>('D18-API-018',{}, {status:'PUBLISHED',size:'100'})).items as Rule[]}));
</script>
<template>
  <section>
    <h2>质量检测</h2>
    <p>对提交的数据集执行四类规则。单批次固定一个规则快照，最多1000条、1MiB；真实数据源自动采集尚未接入。</p>
    <p class="error" role="alert">{{error}}</p><p role="status">{{message}}</p>
    <form v-if="auth.permissions.includes('quality:register')" @submit.prevent="run(create)"><h3>登记检测批次</h3><div class="columns"><label>发布规则版本<select v-model="form.ruleVersionId" required><option value="">请选择</option><optgroup v-for="r in rules" :key="r.id" :label="r.assetName+' / '+r.fieldCode"><option v-for="v in r.versions.filter(v=>v.status==='PUBLISHED')" :key="v.id" :value="v.id">{{r.code}} · {{ruleTypes[r.ruleType]}} · V{{v.versionNo}}</option></optgroup></select></label><label>批次代码<input v-model="form.batchNo" required maxlength="64"></label><label>批次用途<select v-model="form.sourceType"><option v-if="auth.permissions.includes('quality:execute')" value="ENGINE">提交数据集检测</option><option value="MANUAL">手工异常接收</option></select></label></div><button :disabled="busy">登记</button></form>
    <button :disabled="busy" @click="run(load)">刷新批次</button>
    <table><thead><tr><th>批次</th><th>用途</th><th>状态</th><th>检测 / 失败数</th><th>操作</th></tr></thead><tbody><tr v-for="b in rows" :key="b.id"><td>{{b.batchNo}}</td><td>{{b.sourceType==='ENGINE'?'提交数据集':'手工接收'}}</td><td>{{batchStates[b.status]}}</td><td>{{b.scannedCount??'未扫描'}} / {{b.failedCount??'未统计'}}</td><td><button @click="run(()=>select(b))">查看</button></td></tr></tbody></table>
    <p v-if="!rows.length">暂无检测批次。</p>
    <div class="actions"><button :disabled="page<=1||busy" @click="page--;run(load)">上一页</button>第{{page}}页 · 共{{total}}项<button :disabled="page*20>=total||busy" @click="page++;run(load)">下一页</button></div>
  </section>
  <Teleport to="body">
    <div v-if="selected" class="modal-backdrop" @click.self="closeDetail">
      <section class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="detection-detail-title" tabindex="-1" @keydown.esc="closeDetail">
        <div class="modal-header"><div><span class="eyebrow">DETECTION BATCH DETAIL</span><h2 id="detection-detail-title">批次 {{selected.batchNo}}</h2></div><button class="close-button" aria-label="关闭批次详情" @click="closeDetail">×</button></div>
        <p>{{batchStates[selected.status]}} · 字段 {{selected.ruleSnapshot?.fieldCode}} · {{ruleTypes[selected.ruleSnapshot?.ruleType||'']}}</p>
        <p v-if="selected.dataset">已提交{{selected.dataset.rowCount}}条记录；数据集摘要 <code>{{selected.dataset.contentHash}}</code></p>
        <form v-if="selected.sourceType==='ENGINE'&&selected.status==='REGISTERED'&&auth.permissions.includes('quality:execute')" @submit.prevent="run(submit)"><h3>提交记录</h3><p>recordKey 使用稳定的逻辑代码；values 的键来自资产字段代码。一致性规则需同时提交对照字段。只检查本次数据集，记录键摘要用于重复问题关联。</p><details><summary>查看记录格式</summary><pre>{{example}}</pre></details><label>导入JSON文件<input type="file" accept=".json,application/json" @change="run(()=>fileChanged($event))"></label><label>记录数组<textarea v-model="datasetText" required rows="10" spellcheck="false" placeholder="粘贴JSON数组或导入文件"/></label><button :disabled="busy">保存数据集</button><button type="button" :disabled="busy||!selected.dataset" @click="run(execute)">执行检测</button></form>
        <p v-if="selected.sourceType==='MANUAL'">此批次接收手工登记异常，不产生扫描统计。<RouterLink to="/issues" @click="closeDetail">登记质量问题</RouterLink></p>
        <h3>检测失败记录</h3>
        <table><thead><tr><th>行号</th><th>失败原因</th><th>记录摘要</th><th>问题</th></tr></thead><tbody><tr v-for="f in findings" :key="f.id"><td>{{f.rowNo}}</td><td>{{f.reasonCode}}</td><td><code>{{String(f.recordKeyHash).slice(0,16)}}…</code></td><td><RouterLink :to="'/issues?issue='+f.issueId" @click="closeDetail">{{f.issueNo}}</RouterLink> · {{f.issueStatus}}</td></tr></tbody></table>
        <p v-if="!findings.length">{{selected.status==='SUCCEEDED'?'本次没有失败记录。':'执行检测后显示失败记录。'}}</p>
        <div class="actions"><button :disabled="findingPage<=1||busy" @click="findingPage--;run(loadFindings)">上一页</button>第{{findingPage}}页 · 共{{findingTotal}}条<button :disabled="findingPage*20>=findingTotal||busy" @click="findingPage++;run(loadFindings)">下一页</button></div>
        <p class="muted">失败数包含复用已有问题的记录，可能大于新增候选问题数。检测完成不会自动确认问题或关闭工单。</p>
      </section>
    </div>
  </Teleport>
</template>
<style scoped>
.modal-backdrop{position:fixed;inset:0;z-index:1000;display:grid;place-items:center;padding:24px;background:rgba(2,12,27,.78);backdrop-filter:blur(7px)}
.modal-panel{width:min(1040px,100%);max-height:calc(100vh - 48px);overflow:auto;padding:26px;border:1px solid #2f77a8;border-radius:14px;background:linear-gradient(145deg,#09233f,#07182d);box-shadow:0 22px 70px rgba(0,0,0,.52)}
.modal-header{display:flex;align-items:flex-start;justify-content:space-between;gap:20px}.modal-header h2{margin:4px 0 0}.close-button{min-width:38px;height:38px;padding:0;font-size:26px;line-height:1}.modal-panel table{min-width:720px}.modal-panel code{word-break:break-all}
@media (max-width:720px){.modal-backdrop{padding:10px}.modal-panel{max-height:calc(100vh - 20px);padding:18px}.modal-panel table{min-width:620px}}
</style>
