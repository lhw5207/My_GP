import request from '@/utils/request'

// 查询销售退货列表
export function listSale_return(query) {
  return request({
    url: '/jxc/sale_return/list',
    method: 'get',
    params: query
  })
}

// 查询销售退货详细
export function getSale_return(id) {
  return request({
    url: '/jxc/sale_return/' + id,
    method: 'get'
  })
}

// 新增销售退货
export function addSale_return(data) {
  return request({
    url: '/jxc/sale_return',
    method: 'post',
    data: data
  })
}

// 修改销售退货
export function updateSale_return(data) {
  return request({
    url: '/jxc/sale_return',
    method: 'put',
    data: data
  })
}

// 删除销售退货
export function delSale_return(id) {
  return request({
    url: '/jxc/sale_return/' + id,
    method: 'delete'
  })
}

// 审核销售退货单（审核即执行：原批次回增库存 + 流水 + 应收红冲）
export function auditSale_return(id) {
  return request({
    url: '/jxc/sale_return/audit/' + id,
    method: 'put'
  })
}
