import request from '@/utils/request'

// 查询会员管理列表
export function listMember(query) {
  return request({
    url: '/jxc/member/list',
    method: 'get',
    params: query
  })
}

// 查询会员管理详细
export function getMember(id) {
  return request({
    url: '/jxc/member/' + id,
    method: 'get'
  })
}

// 新增会员管理
export function addMember(data) {
  return request({
    url: '/jxc/member',
    method: 'post',
    data: data
  })
}

// 修改会员管理
export function updateMember(data) {
  return request({
    url: '/jxc/member',
    method: 'put',
    data: data
  })
}

// 删除会员管理
export function delMember(id) {
  return request({
    url: '/jxc/member/' + id,
    method: 'delete'
  })
}
