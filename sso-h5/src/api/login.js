import request from '@/utils/request'

export function login (username, password) {
  return request({
    url: '/auth/oauth/login',
    method: 'post',
    params: { username, password }
  })
}

export function confirmAccess (clientId, responseType, scopes, redirectUri) {
  return request({
    url: '/auth/oauth/confirmAccess',
    method: 'get',
    params: { clientId, responseType, scopes, redirectUri }
  })
}

export function authorize (clientId, responseType, scopes, redirectUri) {
  return request({
    url: '/auth/oauth/customAuthorize',
    method: 'post',
    params: {
      user_oauth_approval: true,
      authorize: 'Authorize',
      client_id: clientId,
      response_type: responseType,
      scopes: scopes,
      redirect_uri: redirectUri
    }
  })
}
