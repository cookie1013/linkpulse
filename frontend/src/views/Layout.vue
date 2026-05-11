<template>
  <el-container class="layout">
    <el-aside width="240px" class="sidebar">
      <div class="sidebar-brand">
        <div class="sidebar-logo">LP</div>
        <div>
          <div class="sidebar-title">LinkPulse</div>
          <div class="sidebar-subtitle">Admin Console</div>
        </div>
      </div>

      <el-menu
        :default-active="activePath"
        router
        class="side-menu"
        background-color="#0f172a"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据概览</span>
        </el-menu-item>

        <el-menu-item index="/links">
          <el-icon><Link /></el-icon>
          <span>短链管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <div class="topbar-title">{{ route.meta.title || 'LinkPulse Admin' }}</div>
          <div class="topbar-subtitle">短链接管理与访问分析平台</div>
        </div>

        <div class="topbar-actions">
          <el-tag type="success" effect="light">{{ user.role || 'ADMIN' }}</el-tag>
          <span class="username">{{ user.username || 'admin' }}</span>
          <el-button @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const activePath = computed(() => route.path)

const user = computed(() => {
  const raw = localStorage.getItem('linkpulse_user')
  return raw ? JSON.parse(raw) : {}
})

async function logout() {
  await ElMessageBox.confirm('确认退出当前账号？', '提示', {
    type: 'warning'
  })

  localStorage.removeItem('linkpulse_token')
  localStorage.removeItem('linkpulse_user')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.sidebar {
  background: #0f172a;
  color: white;
  box-shadow: 4px 0 18px rgba(15, 23, 42, 0.08);
}

.sidebar-brand {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.sidebar-logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 800;
}

.sidebar-subtitle {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 2px;
}

.side-menu {
  border-right: none;
  padding-top: 14px;
}

.topbar {
  height: 76px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 26px;
}

.topbar-title {
  font-size: 20px;
  font-weight: 800;
}

.topbar-subtitle {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  color: #334155;
  font-weight: 600;
}

.main {
  background: #f5f7fb;
  padding: 0;
}
</style>