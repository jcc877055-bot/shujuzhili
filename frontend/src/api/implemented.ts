import { request } from './http'
import { endpoints } from './endpoints'
export type ApiId = keyof typeof endpoints
export type Row = Record<string, unknown> & { id: string; version: number }
export interface Page { items: Row[]; page: number; size: number; total: number }
export interface Scope { orgId: string; includeDescendants: boolean | number; scopeType: string }
export interface Me { id: string; orgId: string; displayName: string; roleCodes: string[]; permissions: string[]; scopes: Scope[]; roleCatalog?: {id:string;code:string;name:string}[] }
export interface LoginResult { accessToken: string; expiresAt: string; user: Me }
export function get<T>(id: ApiId, params: Record<string,string>={},query:Record<string,string>={}) { return request<T>(endpoints[id],params,undefined,query) }
/** Reuse the key after transport failure; a changed body is a new command. */
export function command() { let signature='';let key='';return <T>(id:ApiId,params:Record<string,string>,body:unknown) => { const next=JSON.stringify([id,params,body]);if(next!==signature){key=crypto.randomUUID();signature=next}return request<T>(endpoints[id],params,body,{},key) } }
export const write = command()
export function errorText(e:unknown) { const message=e instanceof Error ? e.message + ('traceId' in e ? ' · 追踪号 '+String(e.traceId) : '') : '操作失败'; if(typeof window!=='undefined')window.dispatchEvent(new CustomEvent('datagov:error',{detail:message})); return message }
