<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-logo">LP</div>
        <div>
          <div class="brand-title">LinkPulse Admin</div>
          <div class="brand-subtitle">短链接管理与访问分析平台</div>
        </div>
      </div>

      <div class="hero">
        <h1>让短链管理、访问统计和高并发保护更清晰。</h1>
        <p>
          集成 Redis 缓存、RocketMQ 异步统计、Sentinel 限流、ShardingSphere 分表与多维访问分析能力。
        </p>

        <div class="hero-grid">
          <div class="hero-card">
            <div class="hero-card-title">短链管理</div>
            <div class="hero-card-desc">创建、分页、筛选、禁用、过期控制</div>
          </div>
          <div class="hero-card">
            <div class="hero-card-title">访问分析</div>
            <div class="hero-card-desc">PV 趋势、Referer、User-Agent、TopN</div>
          </div>
          <div class="hero-card">
            <div class="hero-card-title">高并发保护</div>
            <div class="hero-card-desc">Redis、RocketMQ、Sentinel、分表</div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <el-card class="login-card" shadow="always">
        <template #header>
          <div>
            <div class="login-title">管理员登录</div>
            <div class="login-tip">登录后可管理短链并查看访问统计</div>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              size="large"
              placeholder="请输入用户名"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              placeholder="请输入密码"
              show-password
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button
            class="login-button"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登录后台
          </el-button>
        </el-form>

        <div class="demo-account">
          默认账号：<span>admin</span> / <span>Admin@123456</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'Admin@123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()

  loading.value = true
  try {
    const res = await loginApi(form)

    if (res.code !== 200) {
      ElMessage.error(res.message || '登录失败')
      return
    }

    localStorage.setItem('linkpulse_token', res.data.token)
    localStorage.setItem(
      'linkpulse_user',
      JSON.stringify({
        username: res.data.username,
        role: res.data.role
      })
    )

    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  background:
    radial-gradient(circle at 10% 20%, rgba(64, 158, 255, 0.18), transparent 28%),
    radial-gradient(circle at 80% 10%, rgba(103, 194, 58, 0.14), transparent 24%),
    linear-gradient(135deg, #eef5ff 0%, #f8fafc 45%, #eef2ff 100%);
}

.login-left {
  padding: 56px 72px;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-logo {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-title {
  font-size: 22px;
  font-weight: 800;
}

.brand-subtitle {
  color: #64748b;
  margin-top: 4px;
}

.hero {
  margin-top: 110px;
  max-width: 720px;
}

.hero h1 {
  font-size: 48px;
  line-height: 1.18;
  margin: 0;
  color: #0f172a;
}

.hero p {
  color: #64748b;
  font-size: 17px;
  line-height: 1.8;
  margin-top: 22px;
}

.hero-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-top: 42px;
}

.hero-card {
  padding: 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.hero-card-title {
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.hero-card-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.login-right {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px;
}

.login-card {
  width: 430px;
  border-radius: 22px;
}

.login-title {
  font-size: 22px;
  font-weight: 800;
}

.login-tip {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}

.login-button {
  width: 100%;
  margin-top: 12px;
}

.demo-account {
  margin-top: 20px;
  color: #64748b;
  font-size: 13px;
  text-align: center;
}

.demo-account span {
  color: #2563eb;
  font-weight: 700;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-left {
    display: none;
  }

  .login-right {
    min-height: 100vh;
  }
}
</style>