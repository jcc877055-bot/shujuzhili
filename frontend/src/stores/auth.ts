import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Me } from '../api/implemented'
export const useAuthStore = defineStore('auth', () => { const token = ref<string | null>(null); const permissions = ref<string[]>([]);const user=ref<Me|null>(null);function set(value:Me){user.value=value;permissions.value=value.permissions} function clear() { token.value = null; permissions.value = [];user.value=null } return { token, permissions,user,set,clear } })
