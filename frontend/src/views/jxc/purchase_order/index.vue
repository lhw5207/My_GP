<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="采购订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入采购订单号"
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
            v-for="dict in jxc_purchase_order_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="单据日期" prop="orderDate">
        <el-date-picker clearable
          v-model="queryParams.orderDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择单据日期">
        </el-date-picker>
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
          v-hasPermi="['jxc:purchase_order:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['jxc:purchase_order:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['jxc:purchase_order:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['jxc:purchase_order:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="purchase_orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="采购订单号" align="center" prop="orderNo" width="170" />
      <el-table-column label="供应商" align="center" width="160">
        <template #default="scope">
          <span>{{ supplierNameOf(scope.row.supplierId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单据日期" align="center" prop="orderDate" width="110">
        <template #default="scope">
          <span>{{ parseTime(scope.row.orderDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单总金额" align="center" prop="totalAmount" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="jxc_purchase_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditBy" width="90" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['jxc:purchase_order:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['jxc:purchase_order:remove']">删除</el-button>
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

    <!-- 添加或修改采购订单对话框 -->
    <el-dialog :title="title" v-model="open" width="860px" append-to-body>
      <el-form ref="purchase_orderRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="采购订单号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="如 CG20261014001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单据日期" prop="orderDate">
              <el-date-picker clearable
                v-model="form.orderDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择单据日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单总金额">
              <el-input :model-value="totalAmountDisplay" disabled placeholder="由明细自动汇总" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-divider content-position="center">采购订单明细信息</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddJxcPurchaseOrderItem">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteJxcPurchaseOrderItem">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="jxcPurchaseOrderItemList" :row-class-name="rowJxcPurchaseOrderItemIndex" @selection-change="handleJxcPurchaseOrderItemSelectionChange" ref="jxcPurchaseOrderItem">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="商品" prop="productId" min-width="180">
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
          <el-table-column label="采购数量" prop="quantity" width="130">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" :precision="4" :controls="false" placeholder="数量" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="采购单价" prop="price" width="130">
            <template #default="scope">
              <el-input-number v-model="scope.row.price" :min="0" :precision="4" :controls="false" placeholder="单价" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" width="110">
            <template #default="scope">
              <span>{{ formatAmount(scope.row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已入库" prop="receivedQty" width="90">
            <template #default="scope">
              <span>{{ scope.row.receivedQty || 0 }}</span>
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

<script setup name="Purchase_order">
import { listPurchase_order, getPurchase_order, delPurchase_order, addPurchase_order, updatePurchase_order } from "@/api/jxc/purchase_order"
import { listSupplier } from "@/api/jxc/supplier"
import { listProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()
const { jxc_purchase_order_status } = useDict("jxc_purchase_order_status")

const purchase_orderList = ref([])
const jxcPurchaseOrderItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedJxcPurchaseOrderItem = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const supplierOptions = ref([])
const productOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: null,
    supplierId: null,
    orderDate: null,
    status: null,
  },
  rules: {
    orderNo: [
      { required: true, message: "采购订单号不能为空", trigger: "blur" }
    ],
    supplierId: [
      { required: true, message: "供应商不能为空", trigger: "change" }
    ],
    orderDate: [
      { required: true, message: "单据日期不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 明细金额合计（响应式） */
const totalAmountDisplay = computed(() => {
  const sum = (jxcPurchaseOrderItemList.value || []).reduce((acc, it) => {
    return acc + (Number(it.amount) || 0)
  }, 0)
  return formatAmount(sum)
})

/** 加载供应商下拉 */
function loadSupplierOptions() {
  if (supplierOptions.value.length) return
  listSupplier({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    supplierOptions.value = response.rows || []
  })
}

/** 加载商品下拉 */
function loadProductOptions() {
  if (productOptions.value.length) return
  listProduct({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    productOptions.value = response.rows || []
  })
}

/** 供应商名称回显 */
function supplierNameOf(id) {
  const s = supplierOptions.value.find(item => item.id === id)
  return s ? s.supplierName : id
}

/** 金额格式化 */
function formatAmount(v) {
  if (v === null || v === undefined || v === "") return "0.00"
  return Number(v).toFixed(2)
}

/** 选择商品后带出参考进价 */
function handleProductChange(row) {
  const p = productOptions.value.find(item => item.id === row.productId)
  if (p) {
    row.price = p.purchasePrice
  }
  recalcRowAmount(row)
}

/** 行金额联动计算 */
function recalcRowAmount(row) {
  const qty = Number(row.quantity) || 0
  const price = Number(row.price) || 0
  row.amount = Number((qty * price).toFixed(2))
}

/** 查询采购订单列表 */
function getList() {
  loading.value = true
  listPurchase_order(queryParams.value).then(response => {
    purchase_orderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

/** 获取今天日期字符串 */
function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, "0")
  const day = String(d.getDate()).padStart(2, "0")
  return `${d.getFullYear()}-${m}-${day}`
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    orderNo: null,
    supplierId: null,
    orderDate: todayStr(),
    totalAmount: 0,
    status: "0",
    auditBy: null,
    auditTime: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  jxcPurchaseOrderItemList.value = []
  proxy.resetForm("purchase_orderRef")
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

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  loadSupplierOptions()
  loadProductOptions()
  handleAddJxcPurchaseOrderItem()
  open.value = true
  title.value = "添加采购订单"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  loadSupplierOptions()
  loadProductOptions()
  const _id = row.id || ids.value
  getPurchase_order(_id).then(response => {
    form.value = response.data
    jxcPurchaseOrderItemList.value = response.data.jxcPurchaseOrderItemList || []
    open.value = true
    title.value = "修改采购订单"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["purchase_orderRef"].validate(valid => {
    if (valid) {
      if (!jxcPurchaseOrderItemList.value.length) {
        proxy.$modal.msgWarning("请至少添加一行采购明细")
        return
      }
      const invalid = jxcPurchaseOrderItemList.value.some(it => !it.productId || !it.quantity || it.quantity <= 0)
      if (invalid) {
        proxy.$modal.msgWarning("明细中存在未选择商品或数量为 0 的行，请检查")
        return
      }
      // 计算行金额与总金额
      jxcPurchaseOrderItemList.value.forEach((it, idx) => {
        recalcRowAmount(it)
        if (!it.receivedQty) it.receivedQty = 0
        if (!it.rowNo) it.rowNo = idx + 1
      })
      form.value.totalAmount = jxcPurchaseOrderItemList.value.reduce((acc, it) => acc + (Number(it.amount) || 0), 0)
      form.value.jxcPurchaseOrderItemList = jxcPurchaseOrderItemList.value
      if (form.value.id != null) {
        updatePurchase_order(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPurchase_order(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除采购订单编号为"' + _ids + '"的数据项？').then(function() {
    return delPurchase_order(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 采购订单明细序号 */
function rowJxcPurchaseOrderItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

/** 采购订单明细添加按钮操作 */
function handleAddJxcPurchaseOrderItem() {
  let obj = {}
  obj.rowNo = jxcPurchaseOrderItemList.value.length + 1
  obj.productId = null
  obj.quantity = 1
  obj.price = null
  obj.amount = 0
  obj.receivedQty = 0
  obj.remark = null
  jxcPurchaseOrderItemList.value.push(obj)
}

/** 采购订单明细删除按钮操作 */
function handleDeleteJxcPurchaseOrderItem() {
  if (checkedJxcPurchaseOrderItem.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的采购订单明细数据")
  } else {
    const jxcPurchaseOrderItems = jxcPurchaseOrderItemList.value
    const checkedJxcPurchaseOrderItems = checkedJxcPurchaseOrderItem.value
    jxcPurchaseOrderItemList.value = jxcPurchaseOrderItems.filter(function(item) {
      return checkedJxcPurchaseOrderItems.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
function handleJxcPurchaseOrderItemSelectionChange(selection) {
  checkedJxcPurchaseOrderItem.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('jxc/purchase_order/export', {
    ...queryParams.value
  }, `purchase_order_${new Date().getTime()}.xlsx`)
}

loadSupplierOptions()
getList()
</script>
