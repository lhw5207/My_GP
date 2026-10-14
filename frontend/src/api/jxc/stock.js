import request from '@/utils/request'

// 查询现货存量列表（联表商品，含临期天数）
export function listStock(query) {
  return request({
    url: '/jxc/stock/list',
    method: 'get',
    params: query
  })
}
