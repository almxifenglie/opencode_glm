<template>
  <div>
    <div class="nav-bar">
      <h1>套利监控</h1>
      <div class="nav-icons">
        <span class="nav-icon" @click="$router.push('/watchlist')">⭐</span>
        <span class="nav-icon" @click="refresh">🔄</span>
      </div>
    </div>
    
    <div class="main-content">
      <router-view :key="$route.fullPath" ref="view" />
    </div>
    
    <div class="tab-bar">
      <router-link to="/" class="tab-item" :class="{ active: $route.path === '/' }">
        <span class="icon">📊</span>
        <span>综合</span>
      </router-link>
      <router-link to="/qdii" class="tab-item" :class="{ active: $route.path === '/qdii' }">
        <span class="icon">🌍</span>
        <span>QDII</span>
      </router-link>
      <router-link to="/lof" class="tab-item" :class="{ active: $route.path === '/lof' }">
        <span class="icon">📈</span>
        <span>LOF</span>
      </router-link>
      <router-link to="/watchlist" class="tab-item" :class="{ active: $route.path === '/watchlist' }">
        <span class="icon">⭐</span>
        <span>自选</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const view = ref(null)

const refresh = () => {
  if (view.value && view.value.fetchData) {
    view.value.fetchData()
  }
}
</script>
