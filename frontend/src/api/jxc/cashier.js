import request from '@/utils/request'

// 收银台商品定位（barcode 精确匹配 / keyword 名称模糊）
export function findCashierProduct(query) {
  return request({
    url: '/jxc/cashier/product',
    method: 'get',
    params: query
  })
}

// 收银结算（保存即生效：FEFO 扣库存 + 成本快照 + 应收；单号留空由后端自动生成）
export function submitCashierOrder(data) {
  return request({
    url: '/jxc/cashier/submit',
    method: 'post',
    data: data
  })
}
