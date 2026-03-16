<template>
  <div>
    <div class="filter-bar">
      <button 
        class="filter-btn" 
        :class="{ active: purchaseOnly }" 
        @click="setFilter(-50, true)"
      >可申购</button>
      <button 
        class="filter-btn" 
        :class="{ active: !purchaseOnly && minPremium === -50 }" 
        @click="setFilter(-50, false)"
      >全部</button>
      <button 
        class="filter-btn" 
        :class="{ active: minPremium === 1 && !purchaseOnly }" 
        @click="setFilter(1, false)"
      >溢价>1%</button>
      <button 
        class="filter-btn" 
        :class="{ active: minPremium === 2 && !purchaseOnly }" 
        @click="setFilter(2, false)"
      >溢价>2%</button>
      <button 
        class="filter-btn" 
        :class="{ active: minPremium === 3 && !purchaseOnly }" 
        @click="setFilter(3, false)"
      >溢价>3%</button>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else>
      <div class="fund-list">
        <div 
          class="fund-card" 
          v-for="item in funds" 
          :key="item.code"
        >
          <div class="fund-header" @click="$router.push(`/detail/${item.code}`)">
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
          <div class="fund-info" @click="$router.push(`/detail/${item.code}`)">
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
              <span>T-1净值: </span>
              <span class="value">{{ item.nav_t1?.toFixed(3) || '-' }}</span>
            </div>
          </div>
          <div class="fund-info" style="margin-top: 6px;" @click="$router.push(`/detail/${item.code}`)">
            <div v-if="item.nav_estimate">
              <span>估算净值: </span>
              <span class="value">{{ item.nav_estimate?.toFixed(3) }}</span>
            </div>
            <div v-if="item.purchase_limit">
              <span>限额: </span>
              <span class="value">{{ item.purchase_limit }}万</span>
            </div>
          </div>
          <div class="fund-actions">
            <span 
              v-if="item.purchase_status" 
              class="purchase-tag" 
              :class="item.purchase_status === '开放' ? 'open' : 'closed'"
            >
              {{ item.purchase_status === '开放' ? '可申购' : '暂停申购' }}
            </span>
            <button 
              class="refresh-single-btn" 
              @click.stop="refreshSingle(item.code)"
              :disabled="refreshingCode === item.code"
            >
              {{ refreshingCode === item.code ? '刷新中...' : '刷新' }}
            </button>
          </div>
        </div>
      </div>
      
      <div class="pagination" v-if="totalPages > 1">
        <button 
          class="page-btn" 
          :disabled="page === 1" 
          @click="goToPage(page - 1)"
        >上一页</button>
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <button 
          class="page-btn" 
          :disabled="page === totalPages" 
          @click="goToPage(page + 1)"
        >下一页</button>
      </div>
    </div>
    
    <div v-if="updateTime" class="update-time">
      更新时间: {{ formatTime(updateTime) }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const loading = ref(false)
const funds = ref([])
const minPremium = ref(-50)
const purchaseOnly = ref(true)
const updateTime = ref(null)
const page = ref(1)
const pageSize = ref(20)
const totalPages = ref(1)
const refreshingCode = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await api.getLOFRanking({ 
      min_premium: minPremium.value,
      purchase_open: purchaseOnly.value,
      page: page.value,
      page_size: pageSize.value
    })
    funds.value = data.items || []
    updateTime.value = data.update_time
    totalPages.value = data.total_pages || 1
  } catch (error) {
    console.error('获取LOF数据失败:', error)
  } finally {
    loading.value = false
  }
}

const setFilter = (premium, purchase) => {
  minPremium.value = premium
  purchaseOnly.value = purchase
  page.value = 1
  fetchData()
}

const goToPage = (p) => {
  page.value = p
  fetchData()
  window.scrollTo(0, 0)
}

const refreshSingle = async (code) => {
  refreshingCode.value = code
  try {
    await api.refreshFund(code)
    await fetchData()
  } catch (error) {
    console.error('刷新失败:', error)
  } finally {
    refreshingCode.value = null
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(fetchData)

defineExpose({ fetchData })
</script>
