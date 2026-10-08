import { defineStore } from 'pinia'
import type { Scope } from '../types/permission'
export const useScopeStore = defineStore('scope', { state: () => ({ scopes: [] as Scope[] }) })
