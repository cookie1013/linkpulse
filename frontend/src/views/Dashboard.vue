<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <div class="page-title">数据概览</div>
        <div class="page-subtitle">
          汇总短链数量、启用状态、热门短链和访问趋势，用于快速查看平台运行情况。
        </div>
      </div>

      <el-button type="primary" @click="goLinks">
        进入短链管理
      </el-button>
    </div>

    <el-row :gutter="18">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-label">短链总数</div>
          <div class="stat-value">{{ summary.totalLinks }}</div>
          <div class="stat-desc">后台已创建短链数量</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-label">启用短链</div>
          <div class="stat-value">{{ summary.activeLinks }}</div>
          <div class="stat-desc">当前仍可正常跳转</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-label">热门短链 PV</div>
          <div class="stat-value">{{ summary.topPv }}</div>
          <div class="stat-desc">Top 1 短链累计访问量</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-label">TopN 统计</div>
          <div class="stat-value">{{ topLinks.length }}</div>
          <div class="stat-desc">按 PV 排序的热门短链</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18" class="dashboard-row">
      <el-col :span="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <div class="panel-title">热门短链访问趋势</div>
                <div class="panel-subtitle">
                  默认展示 Top 1 短链最近 7 天 PV 趋势
                </div>
              </div>
              <el-tag v-if="currentTopLink" type="primary">
                {{ currentTopLink.shortCode }}
              </el-tag>
            </div>
          </template>

          <div v-if="currentTopLink" ref="trendChartRef" class="trend-chart"></div>
          <el-empty v-else description="暂无短链数据" />
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-header">
              <div>
                <div class="panel-title">热门短链 Top 5</div>
                <div class="panel-subtitle">根据累计 PV 倒序展示</div>
              </div>
            </div>
          </template>

          <el-table :data="topLinks" size="small" border>
            <el-table-column label="短码" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.shortCode }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column label="原始链接" min-width="180">
              <template #default="{ row }">
                <el-tooltip :content="row.originalUrl" placement="top">
                  <span class="ellipsis">{{ row.originalUrl }}</span>
                </el-tooltip>
              </template>
            </el-table-column>

            <el-table-column prop="pv" label="PV" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="panel-title">项目能力闭环</div>
      </template>

      <el-steps :active="5" finish-status="success" align-center>
        <el-step title="登录鉴权" description="JWT 保护后台接口" />
        <el-step title="短链管理" description="创建、分页、禁用、过期" />
        <el-step title="缓存跳转" description="Redis 优先查询" />
        <el-step title="异步统计" description="RocketMQ 幂等消费" />
        <el-step title="访问分析" description="趋势、来源、客户端统计" />
      </el-steps>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

import { listLinksApi } from '../api/links'
import { getPvTrendApi, getTopLinksApi } from '../api/stats'

const router = useRouter()

const summary = reactive({
  totalLinks: 0,
  activeLinks: 0,
  topPv: 0
})

const topLinks = ref([])
const currentTopLink = ref(null)
const trendChartRef = ref(null)
let trendChart = null

onMounted(async () => {
  await loadDashboard()
})

onBeforeUnmount(() => {
  disposeChart()
})

function goLinks() {
  router.push('/links')
}

async function loadDashboard() {
  await Promise.all([
    loadLinkSummary(),
    loadTopLinks()
  ])

  if (topLinks.value.length > 0) {
    currentTopLink.value = topLinks.value[0]
    summary.topPv = topLinks.value[0].pv || 0

    await nextTick()
    await loadTrendChart(currentTopLink.value.id)
  }
}

async function loadLinkSummary() {
  const res = await listLinksApi({
    pageNum: 1,
    pageSize: 100
  })

  if (res.code !== 200) {
    return
  }

  const records = res.data.records || []

  summary.totalLinks = res.data.total || 0
  summary.activeLinks = records.filter(item => item.status === 1).length
}

async function loadTopLinks() {
  const res = await getTopLinksApi(5)

  if (res.code !== 200) {
    return
  }

  topLinks.value = res.data || []
}

async function loadTrendChart(id) {
  const res = await getPvTrendApi(id, 7)

  if (res.code !== 200) {
    return
  }

  renderTrendChart(res.data || [])
}

function renderTrendChart(data) {
  if (!trendChartRef.value) return

  disposeChart()

  trendChart = echarts.init(trendChartRef.value)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 42,
      right: 24,
      top: 36,
      bottom: 36
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

function disposeChart() {
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.stat-card {
  min-height: 150px;
  border-radius: 16px;
}

.stat-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  margin-top: 12px;
}

.dashboard-row {
  margin-top: 22px;
}

.panel-card {
  margin-top: 22px;
  border-radius: 16px;
}

.dashboard-row .panel-card {
  margin-top: 0;
  min-height: 390px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.panel-title {
  font-weight: 800;
  font-size: 16px;
}

.panel-subtitle {
  margin-top: 5px;
  color: #64748b;
  font-size: 13px;
}

.trend-chart {
  width: 100%;
  height: 300px;
}

.ellipsis {
  display: inline-block;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}
</style>