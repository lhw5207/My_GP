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
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="请选择业务类型" clearable style="width: 160px">
          <el-option
            v-for="dict in jxc_biz_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源单号" prop="bizNo">
        <el-input
          v-model="queryParams.bizNo"
          placeholder="单号（如 RK/XS/CG 开头）"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="flowList">
      <el-table-column label="时间" align="center" prop="createTime" width="170" />
      <el-table-column label="商品" align="center">
        <template #default="scope">
          <span>{{ scope.row.productName }}（{{ scope.row.productCode }}）</span>
        </template>
      </el-table-column>
      <el-table-column label="批次号" align="center" prop="batchNo" width="130">
        <template #default="scope">
          <span>{{ scope.row.batchNo === '' ? '（非批次）' : scope.row.batchNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="业务类型" align="center" prop="bizType" width="110">
        <template #default="scope">
          <dict-tag :options="jxc_biz_type" :value="scope.row.bizType" />
        </template>
      </el-table-column>
      <el-table-column label="方向" align="center" prop="flowType" width="80">
        <template #default="scope">
          <dict-tag :options="jxc_flow_type" :value="scope.row.flowType" />
        </template>
      </el-table-column>
      <el-table-column label="来源单号" align="center" prop="bizNo" width="160" />
      <el-table-column label="变动数量" align="center" width="120">
        <template #default="scope">
          <span :style="{ color: Number(scope.row.changeQty) >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
            {{ Number(scope.row.changeQty) >= 0 ? '+' : '' }}{{ scope.row.changeQty }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="批次结存" align="center" prop="balanceQty" width="110" />
      <el-table-column label="操作人" align="center" prop="createBy" width="90" />
      <el-table-column label="备注" align="center" prop="remark" />
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

<script setup name="JxcStockFlow">
import { listStockFlow } from "@/api/jxc/stock_flow"
import { listProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()
const { jxc_biz_type, jxc_flow_type } = useDict("jxc_biz_type", "jxc_flow_type")

const flowList = ref([])
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
    bizType: undefined,
    bizNo: undefined
  }
})

const { queryParams } = toRefs(data)

/** 加载商品下拉 */
function loadProductOptions() {
  listProduct({ pageNum: 1, pageSize: 500 }).then(response => {
    productOptions.value = response.rows || []
  })
}

/** 查询流水列表 */
function getList() {
  loading.value = true
  listStockFlow(queryParams.value).then(response => {
    flowList.value = response.rows
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
  handleQuery()
}

loadProductOptions()
getList()
</script>
