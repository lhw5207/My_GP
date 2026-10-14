<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="商品" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="请选择商品" clearable filterable style="width: 220px">
          <el-option
            v-for="p in productOptions"
            :key="p.id"
            :label="p.productName + '（' + p.productCode + '）'"
            :value="p.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          placeholder="请输入批次号"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="临期筛选" prop="nearExpiryDays">
        <el-select v-model="queryParams.nearExpiryDays" placeholder="全部" clearable style="width: 150px">
          <el-option label="7 天内到期" :value="7" />
          <el-option label="30 天内到期" :value="30" />
          <el-option label="90 天内到期" :value="90" />
        </el-select>
      </el-form-item>
      <el-form-item label="含零库存" prop="includeZero">
        <el-switch v-model="queryParams.includeZero" active-value="1" inactive-value="0" @change="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="stockList">
      <el-table-column label="商品编码" align="center" prop="productCode" width="100" />
      <el-table-column label="商品名称" align="center" prop="productName" />
      <el-table-column label="单位" align="center" prop="unit" width="70" />
      <el-table-column label="批次号" align="center" prop="batchNo" width="130">
        <template #default="scope">
          <span>{{ scope.row.batchNo === '' ? '（非批次）' : scope.row.batchNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生产日期" align="center" prop="productionDate" width="110" />
      <el-table-column label="到期日" align="center" prop="expiryDate" width="110" />
      <el-table-column label="临期状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.daysToExpiry === null || scope.row.daysToExpiry === undefined" type="info">无效期</el-tag>
          <el-tag v-else-if="scope.row.daysToExpiry < 0" type="danger">已过期</el-tag>
          <el-tag v-else-if="scope.row.daysToExpiry <= 30" type="warning">临期 {{ scope.row.daysToExpiry }}天</el-tag>
          <el-tag v-else type="success">{{ scope.row.daysToExpiry }}天</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="现存量" align="center" width="110">
        <template #default="scope">
          <span :style="{ color: isBelowMin(scope.row) ? '#F56C6C' : '' }">{{ scope.row.quantity }}</span>
          <el-tag v-if="isBelowMin(scope.row)" type="danger" size="small" style="margin-left: 4px;">低</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预警下限" align="center" prop="stockMin" width="90" />
      <el-table-column label="成本单价" align="center" prop="costPrice" width="100" />
      <el-table-column label="库存金额" align="center" prop="stockAmount" width="110" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="170" />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="JxcStock">
import { listStock } from "@/api/jxc/stock"
import { listProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()

const stockList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const productOptions = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productId: undefined,
    batchNo: undefined,
    nearExpiryDays: undefined,
    includeZero: "0"
  }
})

const { queryParams } = toRefs(data)

/** 加载商品下拉 */
function loadProductOptions() {
  listProduct({ pageNum: 1, pageSize: 500 }).then(response => {
    productOptions.value = response.rows || []
  })
}

/** 是否低于预警下限 */
function isBelowMin(row) {
  const qty = Number(row.quantity) || 0
  const min = Number(row.stockMin)
  return !isNaN(min) && qty <= min
}

/** 查询库存列表 */
function getList() {
  loading.value = true
  listStock(queryParams.value).then(response => {
    stockList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.includeZero = "0"
  handleQuery()
}

loadProductOptions()
getList()
</script>
