<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="12" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">销售单数</div>
          <div class="stat-value">{{ summary.saleCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">销售额</div>
          <div class="stat-value">¥{{ summary.saleAmount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">销售成本（个别计价）</div>
          <div class="stat-value">¥{{ summary.costAmount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">毛利</div>
          <div class="stat-value" :style="{ color: Number(summary.grossProfit) >= 0 ? '#67C23A' : '#F56C6C' }">¥{{ summary.grossProfit }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="销售日报" name="daily">
        <el-table v-loading="loading" :data="dailyList">
          <el-table-column label="日期" align="center" prop="saleDate" width="130" />
          <el-table-column label="销售单数" align="center" prop="saleCount" width="110" />
          <el-table-column label="销售额" align="center" prop="saleAmount" width="130" />
          <el-table-column label="成本" align="center" prop="costAmount" width="130" />
          <el-table-column label="毛利" align="center" width="130">
            <template #default="scope">
              <span :style="{ color: Number(scope.row.grossProfit) >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">{{ scope.row.grossProfit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="毛利率" align="center">
            <template #default="scope">
              <span>{{ grossRate(scope.row.saleAmount, scope.row.grossProfit) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="商品销售排行（畅销榜）" name="rank">
        <el-table v-loading="loading" :data="rankList">
          <el-table-column label="排名" align="center" width="70">
            <template #default="scope">
              <el-tag :type="scope.$index < 3 ? 'danger' : 'info'" size="small">{{ scope.$index + 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="商品编码" align="center" prop="productCode" width="110" />
          <el-table-column label="商品名称" align="center" prop="productName" />
          <el-table-column label="单位" align="center" prop="unit" width="70" />
          <el-table-column label="销量" align="center" prop="saleQty" width="110" />
          <el-table-column label="销售额" align="center" prop="saleAmount" width="120" />
          <el-table-column label="成本" align="center" prop="costAmount" width="120" />
          <el-table-column label="毛利" align="center" width="120">
            <template #default="scope">
              <span :style="{ color: Number(scope.row.grossProfit) >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">{{ scope.row.grossProfit }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="JxcReportSale">
import { saleDaily, saleSummary, saleByProduct } from "@/api/jxc/report"

const loading = ref(true)
const showSearch = ref(true)
const activeTab = ref("daily")
const dateRange = ref([])
const dailyList = ref([])
const rankList = ref([])
const summary = ref({ saleCount: 0, saleAmount: "0.00", costAmount: "0.00", grossProfit: "0.00" })

const queryParams = computed(() => {
  return {
    beginDate: dateRange.value && dateRange.value.length ? dateRange.value[0] : undefined,
    endDate: dateRange.value && dateRange.value.length ? dateRange.value[1] : undefined
  }
})

/** 毛利率 */
function grossRate(saleAmount, grossProfit) {
  const s = Number(saleAmount) || 0
  if (!s) return "-"
  return ((Number(grossProfit) || 0) / s * 100).toFixed(1) + "%"
}

function getList() {
  loading.value = true
  const q = queryParams.value
  Promise.all([
    saleDaily(q),
    saleByProduct(q),
    saleSummary(q)
  ]).then(([dailyRes, rankRes, sumRes]) => {
    dailyList.value = dailyRes.rows || []
    rankList.value = rankRes.rows || []
    if (sumRes.data) {
      summary.value = sumRes.data
    }
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  dateRange.value = []
  getList()
}

getList()
</script>

<style scoped>
.stat-title {
  color: #909399;
  font-size: 13px;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}
</style>
