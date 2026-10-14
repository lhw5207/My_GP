<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="低库存预警（需补货）" name="low">
        <el-table v-loading="loading" :data="lowList">
          <el-table-column label="商品编码" align="center" prop="productCode" width="110" />
          <el-table-column label="商品名称" align="center" prop="productName" />
          <el-table-column label="单位" align="center" prop="unit" width="70" />
          <el-table-column label="现存量" align="center" prop="stockQty" width="110" />
          <el-table-column label="预警下限" align="center" prop="stockMin" width="110" />
          <el-table-column label="预警上限" align="center" prop="stockMax" width="110" />
          <el-table-column label="预警类型" align="center" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.warnType === '已缺货' ? 'danger' : 'warning'">{{ scope.row.warnType }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="lowTotal > 0"
          :total="lowTotal"
          v-model:page="lowQuery.pageNum"
          v-model:limit="lowQuery.pageSize"
          @pagination="getLowList"
        />
      </el-tab-pane>
      <el-tab-pane label="临期/过期预警" name="expiry">
        <el-form :inline="true" label-width="90px">
          <el-form-item label="到期天数阈值">
            <el-select v-model="expiryDays" style="width: 160px" @change="getExpiryList">
              <el-option label="7 天内" :value="7" />
              <el-option label="30 天内" :value="30" />
              <el-option label="90 天内" :value="90" />
              <el-option label="365 天内" :value="365" />
            </el-select>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="expiryList">
          <el-table-column label="商品编码" align="center" prop="productCode" width="110" />
          <el-table-column label="商品名称" align="center" prop="productName" />
          <el-table-column label="批次号" align="center" prop="batchNo" width="130" />
          <el-table-column label="到期日" align="center" prop="expiryDate" width="120" />
          <el-table-column label="剩余天数" align="center" width="110">
            <template #default="scope">
              <el-tag v-if="Number(scope.row.daysToExpiry) < 0" type="danger">已过期 {{ Math.abs(Number(scope.row.daysToExpiry)) }} 天</el-tag>
              <el-tag v-else-if="Number(scope.row.daysToExpiry) <= 30" type="warning">{{ scope.row.daysToExpiry }} 天</el-tag>
              <el-tag v-else type="success">{{ scope.row.daysToExpiry }} 天</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="库存数量" align="center" prop="quantity" width="110" />
          <el-table-column label="库存金额" align="center" prop="stockAmount" width="110" />
        </el-table>
        <pagination
          v-show="expiryTotal > 0"
          :total="expiryTotal"
          v-model:page="expiryQuery.pageNum"
          v-model:limit="expiryQuery.pageSize"
          @pagination="getExpiryList"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="JxcReportWarning">
import { lowStock, nearExpiry } from "@/api/jxc/report"

const loading = ref(true)
const activeTab = ref("low")
const lowList = ref([])
const lowTotal = ref(0)
const lowQuery = reactive({ pageNum: 1, pageSize: 10 })
const expiryList = ref([])
const expiryTotal = ref(0)
const expiryDays = ref(30)
const expiryQuery = reactive({ pageNum: 1, pageSize: 10 })

function getLowList() {
  loading.value = true
  lowStock(lowQuery).then(response => {
    lowList.value = response.rows || []
    lowTotal.value = response.total || 0
    loading.value = false
  })
}

function getExpiryList() {
  loading.value = true
  nearExpiry({ ...expiryQuery, days: expiryDays.value }).then(response => {
    expiryList.value = response.rows || []
    expiryTotal.value = response.total || 0
    loading.value = false
  })
}

getLowList()
getExpiryList()
</script>
