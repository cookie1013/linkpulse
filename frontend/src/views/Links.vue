<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">短链管理</div>
        <div class="page-subtitle">
          支持短链分页查询、条件筛选、创建、禁用和统计概览查看。
        </div>
      </div>

      <el-button type="primary" size="large" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        创建短链
      </el-button>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :model="query" inline>
        <el-form-item label="短码">
          <el-input
            v-model="query.shortCode"
            placeholder="例如 773ec2"
            clearable
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="原始链接">
          <el-input
            v-model="query.originalUrl"
            placeholder="按原始链接模糊搜索"
            clearable
            style="width: 280px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="90" />

        <el-table-column prop="shortCode" label="短码" width="120">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.shortCode }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="shortUrl" label="短链接" min-width="230">
          <template #default="{ row }">
            <div class="short-url-row">
              <span class="short-url">{{ row.shortUrl }}</span>
              <el-button link type="primary" @click="copyText(row.shortUrl)">
                复制
              </el-button>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="originalUrl" label="原始链接" min-width="280">
          <template #default="{ row }">
            <el-tooltip :content="row.originalUrl" placement="top">
              <span class="ellipsis">{{ row.originalUrl }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="pv" label="PV" width="100" sortable />

        <el-table-column prop="lastAccessTime" label="最近访问" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastAccessTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="expireTime" label="过期时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.expireTime) || '永久有效' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openStatsDrawer(row)">
              统计
            </el-button>

            <el-button
              link
              type="warning"
              :disabled="row.status !== 1"
              @click="handleDisable(row)"
            >
              禁用
            </el-button>

            <el-button link type="success" @click="openShortUrl(row.shortUrl)">
              访问
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchLinks"
          @current-change="fetchLinks"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="createDialogVisible"
      title="创建短链接"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-position="top"
      >
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input
            v-model="createForm.originalUrl"
            type="textarea"
            :rows="3"
            placeholder="请输入以 http:// 或 https:// 开头的原始链接"
          />
        </el-form-item>

        <el-form-item label="过期时间">
          <el-date-picker
            v-model="createForm.expireTime"
            type="datetime"
            placeholder="不选择表示永久有效"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">
          创建
        </el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="statsDrawerVisible"
      size="520px"
      title="短链统计概览"
      destroy-on-close
    >
      <div v-if="currentStats" class="stats-panel">
        <div class="stats-title">
          <div>
            <div class="stats-short-code">{{ currentStats.shortCode }}</div>
            <div class="stats-original-url">{{ currentStats.originalUrl }}</div>
          </div>
          <el-tag type="success">统计概览</el-tag>
        </div>

        <el-row :gutter="14" class="stats-grid">
          <el-col :span="12">
            <el-card shadow="never" class="mini-stat-card">
              <div class="mini-label">总 PV</div>
              <div class="mini-value">{{ currentStats.totalPv }}</div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never" class="mini-stat-card">
              <div class="mini-label">今日 PV</div>
              <div class="mini-value">{{ currentStats.todayPv }}</div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never" class="mini-stat-card">
              <div class="mini-label">昨日 PV</div>
              <div class="mini-value">{{ currentStats.yesterdayPv }}</div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never" class="mini-stat-card">
              <div class="mini-label">近 7 日 PV</div>
              <div class="mini-value">{{ currentStats.last7DaysPv }}</div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never" class="mini-stat-card">
              <div class="mini-label">独立 IP 数</div>
              <div class="mini-value">{{ currentStats.uniqueIpCount }}</div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never" class="mini-stat-card">
              <div class="mini-label">最近访问</div>
              <div class="mini-time">
                {{ formatDateTime(currentStats.lastAccessTime) || '暂无访问' }}
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider />

        <div class="chart-section">
          <div class="chart-header">
            <div>
              <div class="chart-title">近 7 日 PV 趋势</div>
              <div class="chart-subtitle">按天聚合短链访问量</div>
            </div>
          </div>
          <div ref="pvTrendChartRef" class="chart-box"></div>
        </div>

        <div class="chart-section">
          <div class="chart-header">
            <div>
              <div class="chart-title">Referer 来源分布</div>
              <div class="chart-subtitle">识别直接访问与外部来源</div>
            </div>
          </div>
          <div ref="refererChartRef" class="chart-box"></div>
        </div>

        <div class="chart-section">
          <div class="chart-header">
            <div>
              <div class="chart-title">客户端类型分布</div>
              <div class="chart-subtitle">Browser / curl / Postman / Other</div>
            </div>
          </div>
          <div ref="userAgentChartRef" class="chart-box"></div>
        </div>

        <el-divider />

        <div class="drawer-actions">
          <el-button type="primary" @click="copyText(currentStats.shortUrl)">
            复制短链
          </el-button>
          <el-button @click="openShortUrl(currentStats.shortUrl)">
            访问短链
          </el-button>
        </div>
      </div>

      <el-empty v-else description="暂无统计数据" />
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createLinkApi,
  disableLinkApi,
  listLinksApi
} from '../api/links'
import {
  getLinkOverviewApi,
  getPvTrendApi,
  getReferersApi,
  getUserAgentsApi
} from '../api/stats'
import * as echarts from 'echarts'
const loading = ref(false)
const creating = ref(false)
const records = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  shortCode: '',
  originalUrl: ''
})

const createDialogVisible = ref(false)
const createFormRef = ref()

const createForm = reactive({
  originalUrl: '',
  expireTime: ''
})

const createRules = {
  originalUrl: [
    { required: true, message: '请输入原始链接', trigger: 'blur' },
    {
      pattern: /^https?:\/\/.+/i,
      message: '链接必须以 http:// 或 https:// 开头',
      trigger: 'blur'
    }
  ]
}

const statsDrawerVisible = ref(false)
const currentStats = ref(null)
const pvTrendChartRef = ref(null)
const refererChartRef = ref(null)
const userAgentChartRef = ref(null)

let pvTrendChart = null
let refererChart = null
let userAgentChart = null


onMounted(() => {
  fetchLinks()
})

async function fetchLinks() {
  loading.value = true
  try {
    const res = await listLinksApi({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      shortCode: query.shortCode || undefined,
      originalUrl: query.originalUrl || undefined
    })

    if (res.code !== 200) {
      ElMessage.error(res.message || '查询失败')
      return
    }

    records.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchLinks()
}

function handleReset() {
  query.pageNum = 1
  query.pageSize = 10
  query.shortCode = ''
  query.originalUrl = ''
  fetchLinks()
}

function openCreateDialog() {
  createForm.originalUrl = ''
  createForm.expireTime = ''
  createDialogVisible.value = true
}

async function handleCreate() {
  await createFormRef.value.validate()

  creating.value = true
  try {
    const payload = {
      originalUrl: createForm.originalUrl
    }

    if (createForm.expireTime) {
      payload.expireTime = createForm.expireTime
    }

    const res = await createLinkApi(payload)

    if (res.code !== 200) {
      ElMessage.error(res.message || '创建失败')
      return
    }

    ElMessage.success('短链创建成功')
    createDialogVisible.value = false
    query.pageNum = 1
    fetchLinks()
  } finally {
    creating.value = false
  }
}

async function handleDisable(row) {
  await ElMessageBox.confirm(
    `确认禁用短链 ${row.shortCode}？禁用后用户将无法继续跳转。`,
    '禁用短链',
    { type: 'warning' }
  )

  const res = await disableLinkApi(row.id)

  if (res.code !== 200) {
    ElMessage.error(res.message || '禁用失败')
    return
  }

  ElMessage.success('短链已禁用')
  fetchLinks()
}

async function openStatsDrawer(row) {
  currentStats.value = null
  statsDrawerVisible.value = true
  disposeCharts()

  const res = await getLinkOverviewApi(row.id)

  if (res.code !== 200) {
    ElMessage.error(res.message || '获取统计失败')
    return
  }

  currentStats.value = res.data

  await nextTick()
  await loadCharts(row.id)
}
async function loadCharts(id) {
  const [trendRes, refererRes, userAgentRes] = await Promise.all([
    getPvTrendApi(id, 7),
    getReferersApi(id),
    getUserAgentsApi(id)
  ])

  if (trendRes.code === 200) {
    renderPvTrendChart(trendRes.data || [])
  }

  if (refererRes.code === 200) {
    renderRefererChart(refererRes.data || [])
  }

  if (userAgentRes.code === 200) {
    renderUserAgentChart(userAgentRes.data || [])
  }
}

function renderPvTrendChart(data) {
  if (!pvTrendChartRef.value) return

  pvTrendChart = echarts.init(pvTrendChartRef.value)

  pvTrendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 36,
      right: 18,
      top: 32,
      bottom: 32
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date),
      axisLabel: {
        color: '#64748b'
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: {
        color: '#64748b'
      },
      splitLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      }
    },
    series: [
      {
        name: 'PV',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        areaStyle: {},
        data: data.map(item => item.pv)
      }
    ]
  })
}

function renderRefererChart(data) {
  if (!refererChartRef.value) return

  refererChart = echarts.init(refererChartRef.value)

  refererChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 42,
      right: 18,
      top: 32,
      bottom: 42
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.referer),
      axisLabel: {
        color: '#64748b',
        interval: 0,
        rotate: data.length > 3 ? 30 : 0
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: {
        color: '#64748b'
      },
      splitLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      }
    },
    series: [
      {
        name: 'PV',
        type: 'bar',
        barMaxWidth: 38,
        data: data.map(item => item.pv)
      }
    ]
  })
}

function renderUserAgentChart(data) {
  if (!userAgentChartRef.value) return

  userAgentChart = echarts.init(userAgentChartRef.value)

  userAgentChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 42,
      right: 18,
      top: 32,
      bottom: 36
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.clientType),
      axisLabel: {
        color: '#64748b'
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: {
        color: '#64748b'
      },
      splitLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      }
    },
    series: [
      {
        name: 'PV',
        type: 'bar',
        barMaxWidth: 38,
        data: data.map(item => item.pv)
      }
    ]
  })
}

function disposeCharts() {
  if (pvTrendChart) {
    pvTrendChart.dispose()
    pvTrendChart = null
  }

  if (refererChart) {
    refererChart.dispose()
    refererChart = null
  }

  if (userAgentChart) {
    userAgentChart.dispose()
    userAgentChart = null
  }
}

onBeforeUnmount(() => {
  disposeCharts()
})
async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

function openShortUrl(url) {
  window.open(url, '_blank')
}

function formatDateTime(value) {
  if (!value) {
    return ''
  }

  return String(value).replace('T', ' ')
}
</script>

<style scoped>
.chart-section {
  margin-top: 18px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.chart-title {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.chart-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.chart-box {
  width: 100%;
  height: 260px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.filter-card,
.table-card {
  border-radius: 16px;
  margin-bottom: 18px;
}

.short-url-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.short-url {
  color: #2563eb;
  font-weight: 600;
}

.ellipsis {
  display: inline-block;
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 18px;
}

.stats-panel {
  padding-right: 4px;
}

.stats-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.stats-short-code {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.stats-original-url {
  margin-top: 6px;
  color: #64748b;
  line-height: 1.5;
  word-break: break-all;
}

.stats-grid {
  row-gap: 14px;
}

.mini-stat-card {
  border-radius: 14px;
  background: #f8fafc;
}

.mini-label {
  color: #64748b;
  font-size: 13px;
}

.mini-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 800;
  color: #1d4ed8;
}

.mini-time {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.drawer-actions {
  display: flex;
  gap: 12px;
}
</style>