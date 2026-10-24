import request from '@/utils/request'

// 生成收款码挂单（channel: ALIPAY 支付宝沙箱 / WECHAT 微信适配层模拟）
export function createPayQrcode(data) {
  return request({
    url: '/jxc/pay/qrcode',
    method: 'post',
    data: data
  })
}

// 模拟微信到账（适配层演示触发器，走同一条广播管线）
export function mockPayNotify(data) {
  return request({
    url: '/jxc/pay/notify/mock',
    method: 'post',
    data: data
  })
}
