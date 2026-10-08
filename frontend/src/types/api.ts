export interface ApiResponse<T> { code: string; message: string; data: T; traceId: string }
export interface PageResult<T> { items: T[]; page: number; size: number; total: number }
export type ResourceId = string
export interface Endpoint { id: string; method: string; path: string; permission: string }
