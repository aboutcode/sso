import axios from 'axios'
import qs from 'qs'
axios.defaults.timeout = 30000
axios.defaults.baseURL = 'api'
// 返回其他状态吗
axios.defaults.validateStatus = function (status) {
  return status >= 200 && status <= 500
}
// 跨域请求，允许保存cookie
axios.defaults.withCredentials = true

// 表单序列化
export const serialize = data => {
  let list = []
  Object.keys(data).forEach(ele => {
    list.push(`${ele}=${data[ele]}`)
  })
  return list.join('&')
}

// HTTPrequest拦截
axios.interceptors.request.use(
  config => {
    let token = 'token'
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    // headers中配置serialize为true开启序列化
    if (config.methods === 'post' && config.headers.serialize) {
      config.data = serialize(config.data)
      delete config.data.serialize
    }

    // 处理get 请求的数组 springmvc 可以处理
    if (config.method === 'get') {
      config.paramsSerializer = function (params) {
        return qs.stringify(params, { arrayFormat: 'repeat' })
      }
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

export default axios
