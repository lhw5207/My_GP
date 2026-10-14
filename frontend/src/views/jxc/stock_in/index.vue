<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="入库单号" prop="inNo">
        <el-input
          v-model="queryParams.inNo"
          placeholder="请输入入库单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务类型" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="请选择业务类型" clearable style="width: 200px">
          <el-option
            v-for="dict in jxc_stock_in_source_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['jxc:stock_in:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['jxc:stock_in:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['jxc:stock_in:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['jxc:stock_in:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stock_inList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="入库单号" align="center" prop="inNo" width="170" />
      <el-table-column label="业务类型" align="center" prop="sourceType" width="100">
        <template #default="scope">
          <dict-tag :options="jxc_stock_in_source_type" :value="scope.row.sourceType" />
        </template>
      </el-table-column>
      <el-table-column label="来源单号" align="center" prop="sourceNo" width="170" />
      <el-table-column label="供应商" align="center" width="150">
        <template #default="scope">
          <span>{{ supplierNameOf(scope.row.supplierId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库日期" align="center" prop="inDate" width="110">
        <template #default="scope">
          <span>{{ parseTime(scope.row.inDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总金额" align="center" prop="totalAmount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="jxc_bill_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['jxc:stock_in:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['jxc:stock_in:remove']">删除</el-button>
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

    <!-- 添加或修改采购入库单对话框 -->
    <el-dialog :title="title" v-model="open" width="960px" append-to-body>
      <el-form ref="stock_inRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="入库单号" prop="inNo">
              <el-input v-model="form.inNo" placeholder="如 RK20261014001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务类型" prop="sourceType">
              <el-select v-model="form.sourceType" style="width: 100%">
                <el-option
                  v-for="dict in jxc_stock_in_source_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable style="width: 100%">
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
          <el-col :span="8">
            <el-form-item label="来源单号" prop="sourceNo">
              <el-input v-model="form.sourceNo" placeholder="关联采购订单号（可空）" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库日期" prop="inDate">
              <el-date-picker clearable
                v-model="form.inDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择入库日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总金额">
              <el-input :model-value="totalAmountDisplay" disabled placeholder="由明细自动汇总" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-divider content-position="center">入库明细信息</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddJxcStockInItem">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteJxcStockInItem">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="jxcStockInItemList" :row-class-name="rowJxcStockInItemIndex" @selection-change="handleJxcStockInItemSelectionChange" ref="jxcStockInItem">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="商品" prop="productId" min-width="170">
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
              <el-input v-model="scope.row.batchNo" placeholder="非批次商品可空" :disabled="!isBatchProduct(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="生产日期" prop="productionDate" width="140">
            <template #default="scope">
              <el-date-picker v-model="scope.row.productionDate" type="date" value-format="YYYY-MM-DD" placeholder="生产日期" style="width: 100%" :disabled="!isBatchProduct(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="到期日" prop="expiryDate" width="140">
            <template #default="scope">
              <el-date-picker v-model="scope.row.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="到期日" style="width: 100%" :disabled="!isBatchProduct(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="110">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" :precision="4" :controls="false" placeholder="数量" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="price" width="110">
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

<script setup name="Stock_in">
import { listStock_in, getStock_in, delStock_in, addStock_in, updateStock_in } from "@/api/jxc/stock_in"
import { listSupplier } from "@/api/jxc/supplier"
import { listProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()
const { jxc_bill_status, jxc_stock_in_source_type } = useDict("jxc_bill_status", "jxc_stock_in_source_type")

const stock_inList = ref([])
const jxcStockInItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedJxcStockInItem = ref([])
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
    inNo: null,
    sourceType: null,
    supplierId: null,
    status: null,
  },
  rules: {
    inNo: [
      { required: true, message: "入库单号不能为空", trigger: "blur" }
    ],
    sourceType: [
      { required: true, message: "业务类型不能为空", trigger: "change" }
    ],
    inDate: [
      { required: true, message: "入库日期不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 明细金额合计（响应式） */
const totalAmountDisplay = computed(() => {
  const sum = (jxcStockInItemList.value || []).reduce((acc, it) => {
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

/** 是否批次管理商品 */
function isBatchProduct(row) {
  const p = productOptions.value.find(item => item.id === row.productId)
  return p ? p.enableBatch === "1" : false
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

/** 查询采购入库单列表 */
function getList() {
  loading.value = true
  listStock_in(queryParams.value).then(response => {
    stock_inList.value = response.rows
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
    inNo: null,
    sourceType: "PURCHASE",
    sourceNo: null,
    supplierId: null,
    inDate: todayStr(),
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
  jxcStockInItemList.value = []
  proxy.resetForm("stock_inRef")
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
  handleAddJxcStockInItem()
  open.value = true
  title.value = "添加采购入库单"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  loadSupplierOptions()
  loadProductOptions()
  const _id = row.id || ids.value
  getStock_in(_id).then(response => {
    form.value = response.data
    jxcStockInItemList.value = response.data.jxcStockInItemList || []
    open.value = true
    title.value = "修改采购入库单"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["stock_inRef"].validate(valid => {
    if (valid) {
      if (!jxcStockInItemList.value.length) {
        proxy.$modal.msgWarning("请至少添加一行入库明细")
        return
      }
      const invalid = jxcStockInItemList.value.some(it => !it.productId || !it.quantity || it.quantity <= 0)
      if (invalid) {
        proxy.$modal.msgWarning("明细中存在未选择商品或数量为 0 的行，请检查")
        return
      }
      // 批次号空串约定 + 行金额/行号
      jxcStockInItemList.value.forEach((it, idx) => {
        recalcRowAmount(it)
        if (it.batchNo === null || it.batchNo === undefined) it.batchNo = ""
        if (!it.rowNo) it.rowNo = idx + 1
      })
      form.value.totalAmount = jxcStockInItemList.value.reduce((acc, it) => acc + (Number(it.amount) || 0), 0)
      form.value.jxcStockInItemList = jxcStockInItemList.value
      if (form.value.id != null) {
        updateStock_in(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStock_in(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除采购入库单编号为"' + _ids + '"的数据项？').then(function() {
    return delStock_in(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 入库明细序号 */
function rowJxcStockInItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

/** 入库明细添加按钮操作 */
function handleAddJxcStockInItem() {
  let obj = {}
  obj.rowNo = jxcStockInItemList.value.length + 1
  obj.productId = null
  obj.batchNo = ""
  obj.productionDate = null
  obj.expiryDate = null
  obj.quantity = 1
  obj.price = null
  obj.amount = 0
  obj.remark = null
  jxcStockInItemList.value.push(obj)
}

/** 入库明细删除按钮操作 */
function handleDeleteJxcStockInItem() {
  if (checkedJxcStockInItem.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的入库明细数据")
  } else {
    const jxcStockInItems = jxcStockInItemList.value
    const checkedJxcStockInItems = checkedJxcStockInItem.value
    jxcStockInItemList.value = jxcStockInItems.filter(function(item) {
      return checkedJxcStockInItems.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
function handleJxcStockInItemSelectionChange(selection) {
  checkedJxcStockInItem.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('jxc/stock_in/export', {
    ...queryParams.value
  }, `stock_in_${new Date().getTime()}.xlsx`)
}

loadSupplierOptions()
loadProductOptions()
getList()
</script>
