import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

export default {
  async getQDIIRanking(params = {}) {
    const { data } = await api.get('/qdii/ranking', { params })
    return data
  },
  
  async getLOFRanking(params = {}) {
    const { data } = await api.get('/lof/ranking', { params })
    return data
  },
  
  async getAllRanking(params = {}) {
    const { data } = await api.get('/all/ranking', { params })
    return data
  },
  
  async getFundDetail(code) {
    const { data } = await api.get(`/fund/${code}`)
    return data
  },
  
  async getPurchaseStatus(code) {
    const { data } = await api.get(`/purchase/${code}`)
    return data
  },
  
  async searchFund(keyword) {
    const { data } = await api.get('/search', { params: { keyword } })
    return data
  },
  
  async getIndexData() {
    const { data } = await api.get('/index')
    return data
  },
  
  async refreshFund(code) {
    const { data } = await api.post(`/refresh/${code}`)
    return data
  },
}

export const watchlist = {
  get() {
    const data = localStorage.getItem('watchlist')
    return data ? JSON.parse(data) : []
  },
  
  add(code, name) {
    const list = this.get()
    if (!list.find(item => item.code === code)) {
      list.push({ code, name, addedAt: Date.now() })
      localStorage.setItem('watchlist', JSON.stringify(list))
    }
    return list
  },
  
  remove(code) {
    let list = this.get()
    list = list.filter(item => item.code !== code)
    localStorage.setItem('watchlist', JSON.stringify(list))
    return list
  },
  
  has(code) {
    const list = this.get()
    return list.some(item => item.code === code)
  },
}
