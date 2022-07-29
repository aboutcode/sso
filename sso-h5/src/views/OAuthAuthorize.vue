<template>
  <div class="hello">
    <img src="../assets/logo.png">
    <h1>{{ clientId }}</h1>
    <h2>此第三方应用请求获得以下权限:</h2>
    <el-form ref="form" :model="form" label-width="80px">
      <el-form-item>
        <el-checkbox-group v-model="form.scopes">
          <el-checkbox label="访问你的个人信息" name="scopes" value="profile"></el-checkbox>
          <el-checkbox label="查看你的IdToken" name="scopes" value="openid"></el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onAgree">同意授权</el-button>
        <el-button @click="onReject">拒绝</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { confirmAccess } from '@/api/login'
import {authorize} from '../api/login'
export default {
  name: 'OAuthAuthorize',
  data () {
    return {
      clientId: 'OAuth2.0 授权',
      form: {
        scopes: []
      },
      profileChecked: true,
      openIdChecked: false
    }
  },
  created () {
    this.onInit()
  },
  methods: {
    onInit () {
      // 调用OAuth授权确认请求
      confirmAccess(this.$route.query.client_id, this.$route.query.response_type, this.$route.query.scope, this.$route.query.redirect_uri).then((res) => {
        console.log(res)
      })
    },
    onAgree () {
      authorize(this.$route.query.client_id, this.$route.query.response_type, this.$route.query.scope, this.$route.query.redirect_uri).then((res) => {
        console.log(res)
      })
    },
    onReject () {
    }
  }
}
</script>
