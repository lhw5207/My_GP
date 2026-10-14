import request from '@/utils/request'

// 上传解析 + 试运行校验（不落库）
export function parseInit(data) {
  return request({
    url: '/jxc/init/parse',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 确认导入（仅导入校验通过的行）
export function commitInit(data) {
  return request({
    url: '/jxc/init/commit',
    method: 'post',
    data: data
  })
}
