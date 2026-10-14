<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="供应商" prop="supplierId">
        <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable style="width: 200px">
          <el-option
            v-for="s in supplierOptions"
            :key="s.id"
            :label="s.supplierName"
            :value="s.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="核销状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px">
          <el-option
            v-for="dict in jxc_writeoff_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="payableList">
      <el-table-column label="ID" align="center" prop="id" width="70" />
      <el-table-column label="供应商" align="center" prop="supplierName" width="150" />
      <el-table-column label="来源单号" align="center" prop="sourceNo" width="170" />
      <el-table-column label="来源类型" align="center" prop="sourceType" width="120" />
      <el-table-column label="应付金额" align="center" prop="amount" width="110" />
      <el-table-column label="已付金额" align="center" prop="paidAmount" width="110" />
      <el-table-column label="未付余额" align="center" width="110">
        <template #default="scope">
          <span :style="{ color: Number(scope.row.balance) > 0 ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">{{ scope.row.balance }}</span>
        </template>
      </el-table-column>
      <el-table-column label="核销状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="jxc_writeoff_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="记账时间" align="center" prop="createTime" width="170" />
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="JxcPayable">
import { listPayable } from "@/api/jxc/finance"
import { listSupplier } from "@/api/jxc/supplier"

const { proxy } = getCurrentInstance()
const { jxc_writeoff_status } = useDict("jxc_writeoff_status")

const payableList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const supplierOptions = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    supplierId: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

function loadSupplierOptions() {
  listSupplier({ pageNum: 1, pageSize: 500 }).then(response => {
    supplierOptions.value = response.rows || []
  })
}

function getList() {
  loading.value = true
  listPayable(queryParams.value).then(response => {
    payableList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

loadSupplierOptions()
getList()
</script>
