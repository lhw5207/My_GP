import request from '@/utils/request'

// 销售日报（含毛利）
export function saleDaily(query) {
  return request({
    url: '/jxc/report/sale/daily',
    method: 'get',
    params: query
  })
}

// 销售汇总（合计卡片）
export function saleSummary(query) {
  return request({
    url: '/jxc/report/sale/summary',
    method: 'get',
    params: query
  })
}

// 商品销售排行
export function saleByProduct(query) {
  return request({
    url: '/jxc/report/sale/by-product',
    method: 'get',
    params: query
  })
}

// 低库存预警
export function lowStock(query) {
  return request({
    url: '/jxc/report/stock/low',
    method: 'get',
    params: query
  })
}

// 临期预警
export function nearExpiry(query) {
  return request({
    url: '/jxc/report/stock/near-expiry',
    method: 'get',
    params: query
  })
}
