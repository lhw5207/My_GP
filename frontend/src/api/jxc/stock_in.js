import request from '@/utils/request'

// 查询采购入库单列表
export function listStock_in(query) {
  return request({
    url: '/jxc/stock_in/list',
    method: 'get',
    params: query
  })
}

// 查询采购入库单详细
export function getStock_in(id) {
  return request({
    url: '/jxc/stock_in/' + id,
    method: 'get'
  })
}

// 新增采购入库单
export function addStock_in(data) {
  return request({
    url: '/jxc/stock_in',
    method: 'post',
    data: data
  })
}

// 修改采购入库单
export function updateStock_in(data) {
  return request({
    url: '/jxc/stock_in',
    method: 'put',
    data: data
  })
}

// 删除采购入库单
export function delStock_in(id) {
  return request({
    url: '/jxc/stock_in/' + id,
    method: 'delete'
  })
}
