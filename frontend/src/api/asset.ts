// D15-M02; FR-ASSET-001..004; TC-ASSET-001..008 (not executed).
import { endpoints } from './endpoints'
import type { Row } from './implemented'
export const assetEndpoints = ['D18-API-010','D18-API-011','D18-API-012','D18-API-013','D18-API-014','D18-API-046','D18-API-047','D18-API-048','D18-API-049','D18-API-050','D18-API-051','D18-API-052'].map(id=>endpoints[id as keyof typeof endpoints])
export interface Asset extends Row {orgId:string;sourceId:string|null;assetCode:string;name:string;classification:string;ownerId:string|null;status:string;fieldCount:number;allowedActions:string[];owner?:{id:string;displayName:string;status:string};source?:Source}
export interface Source extends Row {orgId:string;code:string;sourceType:string;endpointRef:string|null;status:string;credentialConfigured:boolean}
export interface Field extends Row {assetId:string;fieldCode:string;displayName:string;dataType:string;nullable:boolean|number;sensitiveLevel:string;ruleCount?:number;standardCount?:number;issueCount?:number}
export const classifications:Record<string,string>={PUBLIC:'公开',INTERNAL:'内部',CONFIDENTIAL:'保密',RESTRICTED:'严格受限'}
export const assetStates:Record<string,string>={DRAFT:'草稿',PUBLISHED:'已发布',DISABLED:'已停用'}
export const dataTypes=['VARCHAR','TEXT','INT','BIGINT','DECIMAL','DATE','DATETIME','BOOLEAN','JSON']
export const sensitivities:Record<string,string>={LOW:'低',MEDIUM:'中',HIGH:'高'}
