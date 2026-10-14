import request from '@/utils/request'

// 查询商品信息列表
export function listProduct(query) {
  return request({
    url: '/jxc/product/list',
    method: 'get',
    params: query
  })
}

// 查询商品信息详细
export function getProduct(id) {
  return request({
    url: '/jxc/product/' + id,
    method: 'get'
  })
}

// 新增商品信息
export function addProduct(data) {
  return request({
    url: '/jxc/product',
    method: 'post',
    data: data
  })
}

// 修改商品信息
export function updateProduct(data) {
  return request({
    url: '/jxc/product',
    method: 'put',
    data: data
  })
}

// 删除商品信息
export function delProduct(id) {
  return request({
    url: '/jxc/product/' + id,
    method: 'delete'
  })
}
