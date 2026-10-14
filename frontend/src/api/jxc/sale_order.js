import request from '@/utils/request'

// 查询销售单列表
export function listSale_order(query) {
  return request({
    url: '/jxc/sale_order/list',
    method: 'get',
    params: query
  })
}

// 查询销售单详细
export function getSale_order(id) {
  return request({
    url: '/jxc/sale_order/' + id,
    method: 'get'
  })
}

// 新增销售单
export function addSale_order(data) {
  return request({
    url: '/jxc/sale_order',
    method: 'post',
    data: data
  })
}

// 修改销售单
export function updateSale_order(data) {
  return request({
    url: '/jxc/sale_order',
    method: 'put',
    data: data
  })
}

// 删除销售单
export function delSale_order(id) {
  return request({
    url: '/jxc/sale_order/' + id,
    method: 'delete'
  })
}
