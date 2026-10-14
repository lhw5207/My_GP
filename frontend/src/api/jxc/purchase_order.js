import request from '@/utils/request'

// 查询采购订单列表
export function listPurchase_order(query) {
  return request({
    url: '/jxc/purchase_order/list',
    method: 'get',
    params: query
  })
}

// 查询采购订单详细
export function getPurchase_order(id) {
  return request({
    url: '/jxc/purchase_order/' + id,
    method: 'get'
  })
}

// 新增采购订单
export function addPurchase_order(data) {
  return request({
    url: '/jxc/purchase_order',
    method: 'post',
    data: data
  })
}

// 修改采购订单
export function updatePurchase_order(data) {
  return request({
    url: '/jxc/purchase_order',
    method: 'put',
    data: data
  })
}

// 删除采购订单
export function delPurchase_order(id) {
  return request({
    url: '/jxc/purchase_order/' + id,
    method: 'delete'
  })
}
