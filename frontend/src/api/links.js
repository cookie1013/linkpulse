import request from './request'

export function listLinksApi(params) {
  return request.get('/api/admin/links', { params })
}

export function createLinkApi(data) {
  return request.post('/api/admin/links', data)
}

export function disableLinkApi(id) {
  return request.patch(`/api/admin/links/${id}/disable`)
}