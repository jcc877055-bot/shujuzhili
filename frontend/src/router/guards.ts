import type { Router } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {pages,homePath} from './navigation'
export function installGuards(router:Router){router.beforeEach(to=>{const auth=useAuthStore();if(to.path!=='/login'&&!auth.token)return '/login';if(auth.token&&to.path==='/login')return homePath(auth.permissions);const page=pages.find(p=>p.path===to.path);if(page&&!auth.permissions.includes(page.permission))return homePath(auth.permissions);return true})}
