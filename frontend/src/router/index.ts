import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { installGuards } from './guards'
const router=createRouter({ history: createWebHistory(), routes: [{ path: '/', component: AppLayout, redirect: '/dashboard', children: [
{ path: 'no-access', component: () => import('../views/no-access.vue') },
{ path: 'login', component: () => import('../views/login/index.vue') },
{ path: 'workspace', component: () => import('../views/workspace/index.vue') },
{ path: 'organization', component: () => import('../views/organization/index.vue') },
{ path: 'assets', component: () => import('../views/assets/index.vue') },
{ path: 'metadata', component: () => import('../views/metadata/index.vue') },
{ path: 'standards', component: () => import('../views/standards/index.vue') },
{ path: 'rules', component: () => import('../views/rules/index.vue') },
{ path: 'detections', component: () => import('../views/detections/index.vue') },
{ path: 'issues', component: () => import('../views/issues/index.vue') },
{ path: 'workorders', component: () => import('../views/workorders/index.vue') },
{ path: 'audit', component: () => import('../views/audit/index.vue') },
{ path: 'dashboard', component: () => import('../views/dashboard/index.vue') }
] }, { path: '/:pathMatch(.*)*', redirect: '/workspace' }] })

installGuards(router)
export default router
