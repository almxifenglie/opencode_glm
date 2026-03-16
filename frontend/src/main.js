import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

const routes = [
  { path: '/', name: 'Home', component: () => import('./views/Home.vue') },
  { path: '/qdii', name: 'QDII', component: () => import('./views/QDII.vue') },
  { path: '/lof', name: 'LOF', component: () => import('./views/LOF.vue') },
  { path: '/detail/:code', name: 'Detail', component: () => import('./views/Detail.vue') },
  { path: '/watchlist', name: 'Watchlist', component: () => import('./views/Watchlist.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const app = createApp(App)
app.use(router)
app.mount('#app')
