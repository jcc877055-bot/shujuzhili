package com.example.datagov.modules.quality.application;

import com.example.datagov.common.*;
import com.example.datagov.infrastructure.Db;
import com.example.datagov.infrastructure.idempotency.CommandExecutor;
import com.example.datagov.modules.audit.application.AuditWriter;
import com.example.datagov.modules.standard.domain.RulePolicy;
import com.example.datagov.security.*;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static com.example.datagov.common.Checks.*;

/** D15-M04; D17-T18/T19/T20/T34/T35. Bounded synchronous detection, not a live source scanner. */
@Service public class DetectionService {
 private final Db db;private final ScopePolicy scope;private final CommandExecutor commands;private final JsonSupport json;private final AuditWriter audit;
 public DetectionService(Db db,ScopePolicy scope,CommandExecutor commands,JsonSupport json,AuditWriter audit){this.db=db;this.scope=scope;this.commands=commands;this.json=json;this.audit=audit;}
 public record DatasetRow(String recordKey,Map<String,Object> values){}
 public record Dataset(String assetId,List<DatasetRow> rows,Long expectedVersion){}
 public record Run(Long expectedVersion){}
 private static long expected(Long n){require(n!=null&&n>=0,400,ErrorCode.INVALID_REQUEST,"必须提供批次版本");return n;}
 private Map<String,Object> batch(String bid,boolean lock){var b=db.one("SELECT * FROM detection_batch WHERE id=?"+(lock?" FOR UPDATE":""),id(bid));scope.org(Db.str(b,"org_id"));return b;}
 private Map<String,Object> rule(Map<String,Object> b,boolean lock){var r=db.one("SELECT rv.*,qr.org_id,qr.field_id,qr.rule_type,f.field_code,f.asset_id,a.status AS asset_status FROM rule_version rv JOIN quality_rule qr ON qr.id=rv.rule_id JOIN metadata_field f ON f.id=qr.field_id JOIN data_asset a ON a.id=f.asset_id WHERE rv.id=? AND qr.org_id=?"+(lock?" FOR UPDATE":""),Db.str(b,"rule_version_id"),Db.str(b,"org_id"));require("PUBLISHED".equals(r.get("status"))&&"PUBLISHED".equals(r.get("asset_status")),409,ErrorCode.STATE_CONFLICT,"检测要求资产及规则快照均已发布");return r;}
 private Map<String,Object> present(Map<String,Object> b){var out=Db.dto(b);var ds=db.list("SELECT asset_id,content_hash,row_count,created_at,updated_at FROM detection_dataset WHERE batch_id=?",Db.str(b,"id"));out.put("dataset",ds.isEmpty()?null:Db.dto(ds.get(0)));return out;}
 public Object detail(String bid){PrincipalContext.current().permission("quality:read");var b=batch(bid,false);var out=present(b);var r=db.one("SELECT rv.content_hash,qr.rule_type,f.field_code,f.asset_id FROM rule_version rv JOIN quality_rule qr ON qr.id=rv.rule_id JOIN metadata_field f ON f.id=qr.field_id WHERE rv.id=?",Db.str(b,"rule_version_id"));out.put("ruleSnapshot",Db.dto(r));return out;}
 public Object findings(String bid,int page,int size){PrincipalContext.current().permission("quality:read");batch(bid,false);Db.paging(page,size);return Db.page(db.list("SELECT f.*,q.status AS issue_status,q.issue_no FROM detection_finding f JOIN quality_issue q ON q.id=f.issue_id WHERE f.batch_id=? ORDER BY f.row_no LIMIT ? OFFSET ?",bid,size,(page-1)*size),page,size,db.count("SELECT COUNT(*) FROM detection_finding WHERE batch_id=?",bid));}
 private void registered(Map<String,Object> b){require("ENGINE".equals(b.get("source_type"))&&"REGISTERED".equals(b.get("status")),409,ErrorCode.STATE_CONFLICT,"仅未运行的ENGINE批次支持提交数据与执行");}
 public Object submit(String key,String bid,Dataset body){return commands.execute("quality:execute",key,body,()->{
  var b=batch(bid,true);version(Db.num(b,"version"),expected(body.expectedVersion()));registered(b);var r=rule(b,true);require(id(body.assetId()).equals(Db.str(r,"asset_id")),400,ErrorCode.INVALID_ASSOCIATION,"数据集资产与规则不一致");
  require(body.rows()!=null&&!body.rows().isEmpty()&&body.rows().size()<=1000,400,ErrorCode.INVALID_REQUEST,"数据集需有1至1000条记录");
  var fields=db.list("SELECT id,field_code FROM metadata_field WHERE asset_id=?",body.assetId());Set<String> allowed=new HashSet<>();fields.forEach(f->allowed.add(Db.str(f,"field_code")));
  Map<String,Object> params=RulePolicy.parameters(Db.str(r,"rule_type"),RulePolicy.object(json.parse(Db.str(r,"parameters"))));String target=Db.str(r,"field_code"),other=null;
  if("CONSISTENCY".equals(r.get("rule_type")))other=Db.str(db.one("SELECT field_code FROM metadata_field WHERE id=? AND asset_id=?",params.get("otherFieldId"),body.assetId()),"field_code");
  Set<String> keys=new HashSet<>();for(DatasetRow row:body.rows()){
   require(row!=null&&row.recordKey()!=null&&row.recordKey().matches("[A-Za-z0-9_.:-]{1,128}")&&keys.add(row.recordKey()),400,ErrorCode.INVALID_REQUEST,"记录键需为唯一的1至128字符逻辑代码");
   require(row.values()!=null&&row.values().size()<=50&&allowed.containsAll(row.values().keySet())&&row.values().containsKey(target)&&(other==null||row.values().containsKey(other)),400,ErrorCode.INVALID_REQUEST,"记录需含目标及对照字段，字段须来自该资产元数据");
   for(Object v:row.values().values()){
    require(v==null||v instanceof String||v instanceof Number||v instanceof Boolean,400,ErrorCode.INVALID_REQUEST,"字段值仅允许文本、数值、布尔或null");
    require(v==null||v.toString().length()<=1024,400,ErrorCode.INVALID_REQUEST,"单字段值超过1024字符");
    if(v instanceof Number)require(v.toString().length()<=128&&Math.abs((long)new java.math.BigDecimal(v.toString()).scale())<=20,400,ErrorCode.INVALID_REQUEST,"输入数值精度超限");
   }
  }
  String content=json.canonical(json.parse(json.json(body.rows())));byte[] bytes=content.getBytes(StandardCharsets.UTF_8);require(bytes.length<=1048576,413,ErrorCode.INVALID_REQUEST,"数据集最多1MiB");String digest=sha256(bytes);
  db.update("INSERT INTO detection_dataset(batch_id,asset_id,rows_json,content_hash,row_count,created_by) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE rows_json=VALUES(rows_json),content_hash=VALUES(content_hash),row_count=VALUES(row_count),created_by=VALUES(created_by),updated_at=UTC_TIMESTAMP(3)",bid,body.assetId(),content,digest,body.rows().size(),PrincipalContext.current().id());
  db.update("UPDATE detection_batch SET version=version+1,updated_at=UTC_TIMESTAMP(3) WHERE id=?",bid);audit.write(Db.str(b,"org_id"),"DETECTION_DATASET_SUBMIT","detection_batch",bid,null,Map.of("contentHash",digest,"rowCount",body.rows().size()),"提交检测数据集，不记录原值");return present(batch(bid,false));
 });}
 public Object run(String key,String bid,Run body){return commands.execute("quality:execute",key,body,()->{
  var b=batch(bid,true);version(Db.num(b,"version"),expected(body.expectedVersion()));registered(b);var r=rule(b,true);var ds=db.one("SELECT * FROM detection_dataset WHERE batch_id=? FOR UPDATE",bid);String raw=Db.str(ds,"rows_json");
  Object parsed=json.parse(raw);require(parsed instanceof List<?>,409,ErrorCode.STATE_CONFLICT,"存储数据集格式无效");var rows=(List<?>)parsed;
  require(rows.size()==Db.num(ds,"row_count")&&sha256(json.canonical(parsed).getBytes(StandardCharsets.UTF_8)).equals(ds.get("content_hash")),409,ErrorCode.STATE_CONFLICT,"数据集摘要不匹配");
  Map<String,Object> params=RulePolicy.parameters(Db.str(r,"rule_type"),RulePolicy.object(json.parse(Db.str(r,"parameters"))));String field=Db.str(r,"field_code"),type=Db.str(r,"rule_type"),other=null;
  if(type.equals("CONSISTENCY"))other=Db.str(db.one("SELECT field_code FROM metadata_field WHERE id=? AND asset_id=?",params.get("otherFieldId"),Db.str(r,"asset_id")),"field_code");
  Map<String,Object> content=new LinkedHashMap<>();content.put("fieldId",Db.str(r,"field_id"));content.put("ruleType",r.get("rule_type"));content.put("standardVersionId",Db.str(r,"standard_version_id"));content.put("parameters",params);require(sha256(json.canonical(content).getBytes(StandardCharsets.UTF_8)).equals(r.get("content_hash")),409,ErrorCode.STATE_CONFLICT,"发布规则摘要不匹配");
  Set<Object> duplicates=new HashSet<>(),seen=new HashSet<>();if(type.equals("UNIQUENESS"))for(Object row:rows){Object v=RulePolicy.object(RulePolicy.object(row).get("values")).get(field);if(!RulePolicy.missing(v,params)&&!seen.add(RulePolicy.normalized(v,params)))duplicates.add(RulePolicy.normalized(v,params));}
  db.update("UPDATE detection_batch SET status='RUNNING',started_at=UTC_TIMESTAMP(3),updated_at=UTC_TIMESTAMP(3) WHERE id=?",bid);int failed=0,newIssues=0;
  for(int i=0;i<rows.size();i++){
   var row=RulePolicy.object(rows.get(i));var values=RulePolicy.object(row.get("values"));String reason=RulePolicy.violation(type,values.get(field),other==null?null:values.get(other),params,duplicates);if(reason==null)continue;failed++;
   String keyHash=sha256(row.get("recordKey").toString().getBytes(StandardCharsets.UTF_8));String loc=json.canonical(Map.of("recordKeyHash",keyHash));String org=Db.str(b,"org_id"),asset=Db.str(r,"asset_id"),fid=Db.str(r,"field_id"),rv=Db.str(b,"rule_version_id");String fp=sha256((org+"\n"+asset+"\n"+fid+"\n"+rv+"\n"+loc).getBytes(StandardCharsets.UTF_8));
   db.update("INSERT INTO issue_identity(org_id,fingerprint) VALUES(?,?) ON DUPLICATE KEY UPDATE fingerprint=VALUES(fingerprint)",org,fp);var identity=db.one("SELECT * FROM issue_identity WHERE org_id=? AND fingerprint=? FOR UPDATE",org,fp);String issue=Db.str(identity,"active_issue_id");
   if(issue==null){var candidates=db.list("SELECT id FROM quality_issue WHERE org_id=? AND fingerprint=? AND status='CANDIDATE' ORDER BY id LIMIT 1 FOR UPDATE",org,fp);if(!candidates.isEmpty())issue=Db.str(candidates.get(0),"id");}
   if(issue==null){issue=db.insert("INSERT INTO quality_issue(org_id,asset_id,field_id,rule_version_id,batch_id,issue_no,summary,fingerprint,status,locator_json,created_by) VALUES(?,?,?,?,?,?,?,?,'CANDIDATE',?,?)",org,asset,fid,rv,bid,"QI-"+UUID.randomUUID(),"字段 "+field+" 检测异常："+reason,fp,loc,PrincipalContext.current().id());newIssues++;audit.write(org,"ISSUE_DETECTED","quality_issue",issue,null,Map.of("batchId",bid,"reasonCode",reason,"fingerprint",fp),"引擎发现候选，仍需人工确认");}
   db.insert("INSERT INTO detection_finding(batch_id,row_no,record_key_hash,reason_code,issue_id) VALUES(?,?,?,?,?)",bid,i+1,keyHash,reason,issue);
  }
  db.update("UPDATE detection_batch SET status='SUCCEEDED',scanned_count=?,failed_count=?,ended_at=UTC_TIMESTAMP(3),version=version+1,updated_at=UTC_TIMESTAMP(3) WHERE id=?",rows.size(),failed,bid);
  audit.write(Db.str(b,"org_id"),"DETECTION_RUN","detection_batch",bid,Map.of("status","REGISTERED"),Map.of("status","SUCCEEDED","scannedCount",rows.size(),"failedCount",failed,"newIssues",newIssues,"ruleHash",r.get("content_hash")),"实际执行提交数据集检测");var out=present(batch(bid,false));out.put("newIssueCount",newIssues);return out;
 });}
}
