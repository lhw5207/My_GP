import request from '@/utils/request'

// 收付款单列表
export function listPayment(query) {
  return request({
    url: '/jxc/finance/payment/list',
    method: 'get',
    params: query
  })
}

// 收付款单详情（含核销明细）
export function getPayment(id) {
  return request({
    url: '/jxc/finance/payment/' + id,
    method: 'get'
  })
}

// 登记收付款单（保存即生效，同事务一对多核销）
export function addPayment(data) {
  return request({
    url: '/jxc/finance/payment',
    method: 'post',
    data: data
  })
}

// 应付台账
export function listPayable(query) {
  return request({
    url: '/jxc/finance/payable/list',
    method: 'get',
    params: query
  })
}

// 应收台账
export function listReceivable(query) {
  return request({
    url: '/jxc/finance/receivable/list',
    method: 'get',
    params: query
  })
}

// 未核销单据（登记选择用）
export function listUnsettled(targetType, targetId) {
  return request({
    url: '/jxc/finance/unsettled',
    method: 'get',
    params: { targetType, targetId }
  })
}
