import type {Row} from './implemented'
export interface Snapshot extends Row {versionNo:number;status:string;definition?:{description:string;constraints:Record<string,unknown>};parameters?:Record<string,unknown>;standardVersionId?:string;contentHash?:string}
export interface Standard extends Row {orgId:string;code:string;name:string;versions:Snapshot[]}
export interface Rule extends Row {orgId:string;fieldId:string;code:string;ruleType:string;fieldCode:string;assetId:string;assetName:string;versions:Snapshot[]}
export interface Batch extends Row {orgId:string;batchNo:string;ruleVersionId:string;sourceType:string;status:string;scannedCount:number|null;failedCount:number|null;dataset?:{assetId:string;rowCount:number;contentHash:string}|null;ruleSnapshot?:{assetId:string;fieldCode:string;ruleType:string;contentHash:string}}
export const ruleTypes:Record<string,string>={COMPLETENESS:'完整性',UNIQUENESS:'唯一性',VALIDITY:'有效性',CONSISTENCY:'一致性'}
export const versionStates:Record<string,string>={DRAFT:'草稿',PUBLISHED:'已发布',RETIRED:'已退役'}
export const batchStates:Record<string,string>={REGISTERED:'待执行',RUNNING:'执行中',SUCCEEDED:'已完成',ERROR:'执行错误'}
export interface Constraints {allowNull:boolean;trim:boolean;ignoreCase:boolean;min:string;max:string;minLength:string;maxLength:string;format:string;enumText:string}
export function constraintsForm():Constraints{return {allowNull:false,trim:true,ignoreCase:false,min:'',max:'',minLength:'',maxLength:'',format:'',enumText:''}}
export function constraintsBody(f:Constraints){const p:Record<string,unknown>={allowNull:f.allowNull,trim:f.trim,ignoreCase:f.ignoreCase};for(const key of ['min','max','minLength','maxLength','format'] as const)if(f[key]!=='')p[key]=f[key];if(f.enumText.trim())p.enumValues=f.enumText.split('\n').map(v=>v.trim()).filter(Boolean);return p}
export function fillConstraints(f:Constraints,p:Record<string,unknown>){Object.assign(f,constraintsForm(),{allowNull:p.allowNull===true,trim:p.trim!==false,ignoreCase:p.ignoreCase===true});for(const key of ['min','max','minLength','maxLength','format'] as const)f[key]=p[key]===undefined?'':String(p[key]);f.enumText=Array.isArray(p.enumValues)?p.enumValues.join('\n'):''}
