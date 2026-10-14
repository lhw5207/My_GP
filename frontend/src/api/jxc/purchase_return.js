import request from '@/utils/request'

// 查询采购退货列表
export function listPurchase_return(query) {
  return request({
    url: '/jxc/purchase_return/list',
    method: 'get',
    params: query
  })
}

// 查询采购退货详细
export function getPurchase_return(id) {
  return request({
    url: '/jxc/purchase_return/' + id,
    method: 'get'
  })
}

// 新增采购退货
export function addPurchase_return(data) {
  return request({
    url: '/jxc/purchase_return',
    method: 'post',
    data: data
  })
}

// 修改采购退货
export function updatePurchase_return(data) {
  return request({
    url: '/jxc/purchase_return',
    method: 'put',
    data: data
  })
}

// 删除采购退货
export function delPurchase_return(id) {
  return request({
    url: '/jxc/purchase_return/' + id,
    method: 'delete'
  })
}

// 审核采购退货单（审核即执行：库存扣减 + 流水 + 应付红冲）
export function auditPurchase_return(id) {
  return request({
    url: '/jxc/purchase_return/audit/' + id,
    method: 'put'
  })
}
