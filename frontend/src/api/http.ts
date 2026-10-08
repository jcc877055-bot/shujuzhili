import type { ApiResponse, Endpoint } from '../types/api'
import { useAuthStore } from '../stores/auth'
import { ApiError } from '../utils/errors'
export async function request<T>(endpoint: Endpoint, params: Record<string, string> = {}, body?: unknown, query: Record<string, string> = {}, requestId?: string): Promise<T> {
  let path = endpoint.path.replace('/api/v1', '')
  path = path.replace(/\{(\w+)\}/g, (_, key: string) => { const v = params[key]; if (!v) throw new Error('缺少路径参数 ' + key); return encodeURIComponent(v) })
  const base = import.meta.env.VITE_API_BASE_URL || '/api/v1'
  const url = base.replace(/\/$/, '') + path + (Object.keys(query).length ? '?' + new URLSearchParams(query) : '')
  const headers = new Headers({ Accept: 'application/json' })
  const auth = useAuthStore(); if (auth.token) headers.set('Authorization', 'Bearer ' + auth.token)
  if (requestId) headers.set('X-Request-Id', requestId)
  const multipart = body instanceof FormData
  if (body !== undefined && !multipart) headers.set('Content-Type', 'application/json')
  const res = await fetch(url, { method: endpoint.method, headers, body: body === undefined ? undefined : multipart ? body : JSON.stringify(body), credentials: 'omit' })
  if (res.ok && !res.headers.get('Content-Type')?.includes('application/json')) return await res.blob() as T
  const value: ApiResponse<T> = await res.json()
  if (!res.ok) { if (res.status === 401) auth.clear(); throw new ApiError(res.status, value.code, value.message, value.traceId) }
  return value.data
}
