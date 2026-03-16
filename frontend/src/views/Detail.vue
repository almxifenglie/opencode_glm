<template>
  <div>
    <div class="nav-bar" style="background: #666;">
      <span class="nav-icon" @click="$router.back()">← 返回</span>
      <span>{{ fund?.name || '基金详情' }}</span>
      <button 
        class="watchlist-btn" 
        :class="{ added: isWatched }"
        @click="toggleWatchlist"
      >
        {{ isWatched ? '已收藏' : '收藏' }}
      </button>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="fund" class="detail-page">
      <div class="detail-card">
        <div style="text-align: center; padding: 20px 0;">
          <div 
            class="premium-badge" 
            :class="fund.premium_rate >= 0 ? 'positive' : 'negative'"
            style="font-size: 24px; padding: 10px 20px;"
          >
            {{ fund.premium_rate >= 0 ? '+' : '' }}{{ fund.premium_rate?.toFixed(2) || '-' }}%
          </div>
          <div style="margin-top: 8px; color: #666;">溢价率</div>
        </div>
      </div>
      
      <div class="detail-card">
        <h3>基本信息</h3>
        <div class="detail-row">
          <span class="detail-label">基金代码</span>
          <span class="detail-value">{{ fund.code }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">基金名称</span>
          <span class="detail-value">{{ fund.name }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">基金类型</span>
          <span class="detail-value">{{ fund.fund_type }}</span>
        </div>
        <div class="detail-row" v-if="fund.scale">
          <span class="detail-label">基金规模</span>
          <span class="detail-value">{{ fund.scale }}亿</span>
        </div>
      </div>
      
      <div class="detail-card">
        <h3>行情信息</h3>
        <div class="detail-row">
          <span class="detail-label">最新价</span>
          <span class="detail-value">{{ fund.price?.toFixed(3) || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">涨跌幅</span>
          <span 
            class="detail-value"
            :class="{ up: fund.change_pct > 0, down: fund.change_pct < 0 }"
          >
            {{ fund.change_pct >= 0 ? '+' : '' }}{{ fund.change_pct?.toFixed(2) || '-' }}%
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">T-1净值</span>
          <span class="detail-value">{{ fund.nav_t1?.toFixed(4) || '-' }}</span>
        </div>
        <div class="detail-row" v-if="fund.nav_estimate">
          <span class="detail-label">估算净值</span>
          <span class="detail-value">{{ fund.nav_estimate?.toFixed(4) }}</span>
        </div>
        <div class="detail-row" v-if="fund.volume">
          <span class="detail-label">成交量</span>
          <span class="detail-value">{{ formatVolume(fund.volume) }}</span>
        </div>
        <div class="detail-row" v-if="fund.amount">
          <span class="detail-label">成交额</span>
          <span class="detail-value">{{ formatAmount(fund.amount) }}</span>
        </div>
      </div>
      
      <div class="detail-card">
        <h3>申购信息</h3>
        <div class="detail-row">
          <span class="detail-label">申购状态</span>
          <span 
            class="detail-value"
            :style="{ color: fund.purchase_status === '开放' ? '#52c41a' : '#faad14' }"
          >
            {{ fund.purchase_status || '-' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">赎回状态</span>
          <span 
            class="detail-value"
            :style="{ color: fund.redeem_status === '开放' ? '#52c41a' : '#faad14' }"
          >
            {{ fund.redeem_status || '-' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">申购限额</span>
          <span class="detail-value">
            {{ fund.purchase_limit ? fund.purchase_limit + '万' : '不限' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">申购费率</span>
          <span class="detail-value">{{ fund.purchase_fee ? fund.purchase_fee + '%' : '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">赎回费率</span>
          <span class="detail-value">{{ fund.redeem_fee ? fund.redeem_fee + '%' : '-' }}</span>
        </div>
      </div>
      
      <div v-if="fund.update_time" class="update-time">
        更新时间: {{ formatTime(fund.update_time) }}
      </div>
    </div>
    
    <div v-else class="empty-state">
      <span class="icon">❌</span>
      <span>基金不存在</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api, { watchlist } from '../api'

const route = useRoute()
const loading = ref(false)
const fund = ref(null)

const isWatched = computed(() => watchlist.has(route.params.code))

const fetchData = async () => {
  loading.value = true
  try {
    const data = await api.getFundDetail(route.params.code)
    fund.value = data
  } catch (error) {
    console.error('获取基金详情失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleWatchlist = () => {
  if (isWatched.value) {
    watchlist.remove(route.params.code)
  } else {
    watchlist.add(route.params.code, fund.value?.name)
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const formatVolume = (vol) => {
  if (!vol) return '-'
  if (vol >= 10000) {
    return (vol / 10000).toFixed(2) + '万'
  }
  return vol.toString()
}

const formatAmount = (amt) => {
  if (!amt) return '-'
  if (amt >= 100000000) {
    return (amt / 100000000).toFixed(2) + '亿'
  }
  if (amt >= 10000) {
    return (amt / 10000).toFixed(2) + '万'
  }
  return amt.toString()
}

onMounted(fetchData)

defineExpose({ fetchData })
</script>
