<template>
  <div>
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="watchlistItems.length === 0" class="empty-state">
      <span class="icon">⭐</span>
      <span>暂无自选基金</span>
      <span style="margin-top: 8px; font-size: 12px;">在基金详情页点击收藏添加</span>
    </div>
    
    <div v-else class="fund-list">
      <div 
        class="fund-card" 
        v-for="item in funds" 
        :key="item.code"
        @click="$router.push(`/detail/${item.code}`)"
      >
        <div class="fund-header">
          <div>
            <div class="fund-name">{{ item.name }}</div>
            <div class="fund-code">{{ item.code }}</div>
          </div>
          <div 
            class="premium-badge" 
            :class="item.premium_rate >= 0 ? 'positive' : 'negative'"
          >
            {{ item.premium_rate >= 0 ? '+' : '' }}{{ item.premium_rate?.toFixed(2) || '-' }}%
          </div>
        </div>
        <div class="fund-info">
          <div>
            <span>现价: </span>
            <span class="value">{{ item.price?.toFixed(3) || '-' }}</span>
          </div>
          <div>
            <span>涨跌: </span>
            <span 
              class="value" 
              :class="{ 
                up: item.change_pct > 0, 
                down: item.change_pct < 0 
              }"
            >
              {{ item.change_pct >= 0 ? '+' : '' }}{{ item.change_pct?.toFixed(2) || '-' }}%
            </span>
          </div>
          <div>
            <span>净值: </span>
            <span class="value">{{ item.nav_t1?.toFixed(3) || '-' }}</span>
          </div>
        </div>
        <div style="margin-top: 8px;">
          <button 
            class="watchlist-btn" 
            style="font-size: 11px; padding: 4px 8px;"
            @click.stop="removeFromWatchlist(item.code)"
          >
            移除
          </button>
          <span 
            v-if="item.purchase_status" 
            class="purchase-tag" 
            :class="item.purchase_status === '开放' ? 'open' : 'closed'"
            style="margin-left: 8px;"
          >
            {{ item.purchase_status === '开放' ? '可申购' : '暂停' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api, { watchlist } from '../api'

const loading = ref(false)
const watchlistItems = ref([])
const funds = ref([])

const fetchData = async () => {
  loading.value = true
  watchlistItems.value = watchlist.get()
  
  if (watchlistItems.value.length === 0) {
    loading.value = false
    return
  }
  
  try {
    const promises = watchlistItems.value.map(item => 
      api.getFundDetail(item.code).catch(() => null)
    )
    const results = await Promise.all(promises)
    funds.value = results.filter(r => r !== null)
  } catch (error) {
    console.error('获取自选数据失败:', error)
  } finally {
    loading.value = false
  }
}

const removeFromWatchlist = (code) => {
  watchlist.remove(code)
  watchlistItems.value = watchlist.get()
  funds.value = funds.value.filter(f => f.code !== code)
}

onMounted(fetchData)

defineExpose({ fetchData })
</script>
