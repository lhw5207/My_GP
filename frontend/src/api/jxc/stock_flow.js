import request from '@/utils/request'

// 查询库存流水台账
export function listStockFlow(query) {
  return request({
    url: '/jxc/stock_flow/list',
    method: 'get',
    params: query
  })
}
