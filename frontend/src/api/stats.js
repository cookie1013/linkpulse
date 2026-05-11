import request from './request'

export function getLinkOverviewApi(id) {
  return request.get(`/api/admin/stats/links/${id}/overview`)
}

export function getPvTrendApi(id, days = 7) {
  return request.get(`/api/admin/stats/links/${id}/pv-trend`, {
    params: { days }
  })
}

export function getReferersApi(id) {
  return request.get(`/api/admin/stats/links/${id}/referers`)
}

export function getUserAgentsApi(id) {
  return request.get(`/api/admin/stats/links/${id}/user-agents`)
}
export function getTopLinksApi(limit = 5) {
  return request.get('/api/admin/stats/top-links', {
    params: { limit }
  })
}