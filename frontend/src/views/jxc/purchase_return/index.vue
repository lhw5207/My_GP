<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="退货单号" prop="returnNo">
        <el-input
          v-model="queryParams.returnNo"
          placeholder="请输入退货单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
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
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option
            v-for="dict in jxc_bill_status"
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['jxc:purchase_return:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['jxc:purchase_return:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="purchase_returnList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="退货单号" align="center" prop="returnNo" width="170" />
      <el-table-column label="供应商" align="center" width="150">
        <template #default="scope">
          <span>{{ supplierNameOf(scope.row.supplierId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="原入库单号" align="center" prop="sourceInNo" width="170" />
      <el-table-column label="退货日期" align="center" prop="returnDate" width="110">
        <template #default="scope">
          <span>{{ parseTime(scope.row.returnDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退货金额" align="center" prop="totalAmount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="jxc_bill_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="success" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['jxc:purchase_return:audit']">审核</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['jxc:purchase_return:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['jxc:purchase_return:remove']">删除</el-button>
          </template>
          <span v-else style="color: #909399; font-size: 12px;">已审核（红冲完成）</span>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改采购退货对话框 -->
    <el-dialog :title="title" v-model="open" width="920px" append-to-body>
      <el-form ref="purchase_returnRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="退货单号" prop="returnNo">
              <el-input v-model="form.returnNo" placeholder="如 CTH20261014001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
                <el-option
                  v-for="s in supplierOptions"
                  :key="s.id"
                  :label="s.supplierName"
                  :value="s.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原入库单号" prop="sourceInNo">
              <el-input v-model="form.sourceInNo" placeholder="关联入库单号（可空）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="退货日期" prop="returnDate">
              <el-date-picker clearable
                v-model="form.returnDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择退货日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="退货金额">
              <el-input :model-value="totalAmountDisplay" disabled placeholder="由明细自动汇总" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="center">退货明细（审核时按原批次扣减库存并红冲应付）</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddJxcPurchaseReturnItem">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteJxcPurchaseReturnItem">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="jxcPurchaseReturnItemList" :row-class-name="rowJxcPurchaseReturnItemIndex" @selection-change="handleJxcPurchaseReturnItemSelectionChange" ref="jxcPurchaseReturnItem">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="商品" prop="productId" min-width="200">
            <template #default="scope">
              <el-select v-model="scope.row.productId" placeholder="请选择商品" filterable @change="handleProductChange(scope.row)" style="width: 100%">
                <el-option
                  v-for="p in productOptions"
                  :key="p.id"
                  :label="p.productName + '（' + p.productCode + '）'"
                  :value="p.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchNo" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.batchNo" placeholder="非批次商品留空" />
            </template>
          </el-table-column>
          <el-table-column label="退货数量" prop="quantity" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" :precision="4" :controls="false" placeholder="数量" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="退货单价" prop="price" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.price" :min="0" :precision="4" :controls="false" placeholder="单价" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" width="100">
            <template #default="scope">
              <span>{{ formatAmount(scope.row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Purchase_return">
import { listPurchase_return, getPurchase_return, delPurchase_return, addPurchase_return, updatePurchase_return, auditPurchase_return } from "@/api/jxc/purchase_return"
import { listSupplier } from "@/api/jxc/supplier"
import { listProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()
const { jxc_bill_status } = useDict("jxc_bill_status")

const purchase_returnList = ref([])
const jxcPurchaseReturnItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedJxcPurchaseReturnItem = ref([])
const total = ref(0)
const title = ref("")

const supplierOptions = ref([])
const productOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    returnNo: null,
    supplierId: null,
    status: null,
  },
  rules: {
    returnNo: [
      { required: true, message: "退货单号不能为空", trigger: "blur" }
    ],
    supplierId: [
      { required: true, message: "供应商不能为空", trigger: "change" }
    ],
    returnDate: [
      { required: true, message: "退货日期不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 明细金额合计 */
const totalAmountDisplay = computed(() => {
  const sum = (jxcPurchaseReturnItemList.value || []).reduce((acc, it) => acc + (Number(it.amount) || 0), 0)
  return formatAmount(sum)
})

function loadSupplierOptions() {
  if (supplierOptions.value.length) return
  listSupplier({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    supplierOptions.value = response.rows || []
  })
}

function loadProductOptions() {
  if (productOptions.value.length) return
  listProduct({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    productOptions.value = response.rows || []
  })
}

function supplierNameOf(id) {
  const s = supplierOptions.value.find(item => item.id === id)
  return s ? s.supplierName : id
}

function formatAmount(v) {
  if (v === null || v === undefined || v === "") return "0.00"
  return Number(v).toFixed(2)
}

function handleProductChange(row) {
  const p = productOptions.value.find(item => item.id === row.productId)
  if (p) {
    row.price = p.purchasePrice
  }
  recalcRowAmount(row)
}

function recalcRowAmount(row) {
  const qty = Number(row.quantity) || 0
  const price = Number(row.price) || 0
  row.amount = Number((qty * price).toFixed(2))
}

function getList() {
  loading.value = true
  listPurchase_return(queryParams.value).then(response => {
    purchase_returnList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, "0")
  const day = String(d.getDate()).padStart(2, "0")
  return `${d.getFullYear()}-${m}-${day}`
}

function reset() {
  form.value = {
    id: null,
    returnNo: null,
    supplierId: null,
    sourceInNo: null,
    returnDate: todayStr(),
    totalAmount: 0,
    status: "0",
    remark: null
  }
  jxcPurchaseReturnItemList.value = []
  proxy.resetForm("purchase_returnRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
}

function handleAdd() {
  reset()
  loadSupplierOptions()
  loadProductOptions()
  handleAddJxcPurchaseReturnItem()
  open.value = true
  title.value = "添加采购退货单"
}

function handleUpdate(row) {
  reset()
  loadSupplierOptions()
  loadProductOptions()
  const _id = row.id || ids.value
  getPurchase_return(_id).then(response => {
    form.value = response.data
    jxcPurchaseReturnItemList.value = response.data.jxcPurchaseReturnItemList || []
    open.value = true
    title.value = "修改采购退货单"
  })
}

function submitForm() {
  proxy.$refs["purchase_returnRef"].validate(valid => {
    if (valid) {
      if (!jxcPurchaseReturnItemList.value.length) {
        proxy.$modal.msgWarning("请至少添加一行退货明细")
        return
      }
      const invalid = jxcPurchaseReturnItemList.value.some(it => !it.productId || !it.quantity || it.quantity <= 0)
      if (invalid) {
        proxy.$modal.msgWarning("明细中存在未选择商品或数量为 0 的行，请检查")
        return
      }
      jxcPurchaseReturnItemList.value.forEach((it, idx) => {
        recalcRowAmount(it)
        if (it.batchNo === null || it.batchNo === undefined) it.batchNo = ""
        if (!it.rowNo) it.rowNo = idx + 1
      })
      form.value.totalAmount = jxcPurchaseReturnItemList.value.reduce((acc, it) => acc + (Number(it.amount) || 0), 0)
      form.value.jxcPurchaseReturnItemList = jxcPurchaseReturnItemList.value
      if (form.value.id != null) {
        updatePurchase_return(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPurchase_return(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功（草稿），审核后生效")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 审核（审核即执行：按原批次扣库存 + 流水 + 应付红冲） */
function handleAudit(row) {
  if (!row || !row.id) {
    proxy.$modal.msgWarning("请选择要审核的退货单")
    return
  }
  proxy.$modal.confirm('审核后将按原批次扣减库存并红冲应付账款，是否确认审核"' + row.returnNo + '"？').then(function() {
    return auditPurchase_return(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功，库存已扣减、应付已红冲")
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除采购退货单编号为"' + _ids + '"的数据项？').then(function() {
    return delPurchase_return(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function rowJxcPurchaseReturnItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

function handleAddJxcPurchaseReturnItem() {
  let obj = {}
  obj.rowNo = jxcPurchaseReturnItemList.value.length + 1
  obj.productId = null
  obj.batchNo = ""
  obj.quantity = 1
  obj.price = null
  obj.amount = 0
  obj.remark = null
  jxcPurchaseReturnItemList.value.push(obj)
}

function handleDeleteJxcPurchaseReturnItem() {
  if (checkedJxcPurchaseReturnItem.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的退货明细数据")
  } else {
    const items = jxcPurchaseReturnItemList.value
    const checked = checkedJxcPurchaseReturnItem.value
    jxcPurchaseReturnItemList.value = items.filter(function(item) {
      return checked.indexOf(item.index) == -1
    })
  }
}

function handleJxcPurchaseReturnItemSelectionChange(selection) {
  checkedJxcPurchaseReturnItem.value = selection.map(item => item.index)
}

function handleExport() {
  proxy.download('jxc/purchase_return/export', {
    ...queryParams.value
  }, `purchase_return_${new Date().getTime()}.xlsx`)
}

loadSupplierOptions()
loadProductOptions()
getList()
</script>
