import request from '@/utils/request'

// 查询库存盘点列表
export function listStock_check(query) {
  return request({
    url: '/jxc/stock_check/list',
    method: 'get',
    params: query
  })
}

// 查询库存盘点详细
export function getStock_check(id) {
  return request({
    url: '/jxc/stock_check/' + id,
    method: 'get'
  })
}

// 新增库存盘点
export function addStock_check(data) {
  return request({
    url: '/jxc/stock_check',
    method: 'post',
    data: data
  })
}

// 修改库存盘点
export function updateStock_check(data) {
  return request({
    url: '/jxc/stock_check',
    method: 'put',
    data: data
  })
}

// 删除库存盘点
export function delStock_check(id) {
  return request({
    url: '/jxc/stock_check/' + id,
    method: 'delete'
  })
}

// 审核盘点单（审核即执行：盘盈生成入库单 / 盘亏生成出库单）
export function auditStock_check(id) {
  return request({
    url: '/jxc/stock_check/audit/' + id,
    method: 'put'
  })
}
