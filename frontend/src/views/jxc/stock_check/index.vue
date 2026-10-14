<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="盘点单号" prop="checkNo">
        <el-input
          v-model="queryParams.checkNo"
          placeholder="请输入盘点单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['jxc:stock_check:add']"
        >新增盘点单</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['jxc:stock_check:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stock_checkList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="盘点单号" align="center" prop="checkNo" width="190" />
      <el-table-column label="盘点日期" align="center" prop="checkDate" width="110">
        <template #default="scope">
          <span>{{ parseTime(scope.row.checkDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="jxc_bill_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditBy" width="90" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="scope">
          <template v-if="scope.row.status === '0'">
            <el-button link type="success" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['jxc:stock_check:audit']">审核</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['jxc:stock_check:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['jxc:stock_check:remove']">删除</el-button>
          </template>
          <span v-else style="color: #909399; font-size: 12px;">已审核（生成调整单）</span>
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

    <!-- 添加或修改盘点单对话框 -->
    <el-dialog :title="title" v-model="open" width="980px" append-to-body>
      <el-form ref="stock_checkRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="盘点单号" prop="checkNo">
              <el-input v-model="form.checkNo" placeholder="如 PD20261014001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点日期" prop="checkDate">
              <el-date-picker clearable
                v-model="form.checkDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择盘点日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="center">盘点明细（选择商品后自动带出账面数量，录入实盘数量自动算差异）</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddJxcStockCheckItem">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteJxcStockCheckItem">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="jxcStockCheckItemList" :row-class-name="rowJxcStockCheckItemIndex" @selection-change="handleJxcStockCheckItemSelectionChange" ref="jxcStockCheckItem">
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
          <el-table-column label="批次号" prop="batchNo" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.batchNo" placeholder="非批次留空" @change="loadBookQty(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="账面数量" prop="bookQty" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.bookQty" :precision="4" :controls="false" style="width: 100%" @change="recalcDiff(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="实盘数量" prop="actualQty" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.actualQty" :min="0" :precision="4" :controls="false" placeholder="实盘" style="width: 100%" @change="recalcDiff(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="差异" prop="diffQty" width="110">
            <template #default="scope">
              <span :style="{ color: Number(scope.row.diffQty) > 0 ? '#67C23A' : (Number(scope.row.diffQty) < 0 ? '#F56C6C' : '#909399'), fontWeight: 'bold' }">
                {{ Number(scope.row.diffQty) > 0 ? '+' : '' }}{{ scope.row.diffQty }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="调整单" align="center" width="170">
            <template #default="scope">
              <span v-if="scope.row.genInNo" style="color: #67C23A; font-size: 12px;">{{ scope.row.genInNo }}</span>
              <span v-if="scope.row.genOutNo" style="color: #F56C6C; font-size: 12px;">{{ scope.row.genOutNo }}</span>
              <span v-if="!scope.row.genInNo && !scope.row.genOutNo" style="color: #909399; font-size: 12px;">审核后生成</span>
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

<script setup name="Stock_check">
import { listStock_check, getStock_check, delStock_check, addStock_check, updateStock_check, auditStock_check } from "@/api/jxc/stock_check"
import { listProduct } from "@/api/jxc/product"
import { listStock } from "@/api/jxc/stock"

const { proxy } = getCurrentInstance()
const { jxc_bill_status } = useDict("jxc_bill_status")

const stock_checkList = ref([])
const jxcStockCheckItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedJxcStockCheckItem = ref([])
const total = ref(0)
const title = ref("")

const productOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    checkNo: null,
    status: null,
  },
  rules: {
    checkNo: [
      { required: true, message: "盘点单号不能为空", trigger: "blur" }
    ],
    checkDate: [
      { required: true, message: "盘点日期不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

function loadProductOptions() {
  if (productOptions.value.length) return
  listProduct({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    productOptions.value = response.rows || []
  })
}

/** 选择商品后带出账面数量（查现存量） */
function handleProductChange(row) {
  loadBookQty(row)
}

/** 按商品+批次查现存量填充账面数量 */
function loadBookQty(row) {
  if (!row.productId) return
  listStock({ pageNum: 1, pageSize: 100, productId: row.productId, batchNo: row.batchNo || "", includeZero: "1" }).then(response => {
    const rows = response.rows || []
    let qty = 0
    const target = (row.batchNo || "")
    const hit = rows.find(item => (item.batchNo || "") === target)
    if (hit) {
      qty = Number(hit.quantity) || 0
    }
    row.bookQty = qty
    recalcDiff(row)
  })
}

/** 差异 = 实盘 - 账面 */
function recalcDiff(row) {
  const book = Number(row.bookQty) || 0
  const actual = Number(row.actualQty) || 0
  row.diffQty = Number((actual - book).toFixed(4))
}

function getList() {
  loading.value = true
  listStock_check(queryParams.value).then(response => {
    stock_checkList.value = response.rows
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
    checkNo: null,
    checkDate: todayStr(),
    status: "0",
    remark: null
  }
  jxcStockCheckItemList.value = []
  proxy.resetForm("stock_checkRef")
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
  loadProductOptions()
  handleAddJxcStockCheckItem()
  open.value = true
  title.value = "新增盘点单"
}

function handleUpdate(row) {
  reset()
  loadProductOptions()
  const _id = row.id || ids.value
  getStock_check(_id).then(response => {
    form.value = response.data
    jxcStockCheckItemList.value = response.data.jxcStockCheckItemList || []
    open.value = true
    title.value = "修改盘点单"
  })
}

function submitForm() {
  proxy.$refs["stock_checkRef"].validate(valid => {
    if (valid) {
      if (!jxcStockCheckItemList.value.length) {
        proxy.$modal.msgWarning("请至少添加一行盘点明细")
        return
      }
      const invalid = jxcStockCheckItemList.value.some(it => !it.productId || it.actualQty === null || it.actualQty === undefined)
      if (invalid) {
        proxy.$modal.msgWarning("明细中存在未选择商品或未录入实盘数量的行，请检查")
        return
      }
      jxcStockCheckItemList.value.forEach((it, idx) => {
        recalcDiff(it)
        if (it.batchNo === null || it.batchNo === undefined) it.batchNo = ""
        if (!it.rowNo) it.rowNo = idx + 1
      })
      form.value.jxcStockCheckItemList = jxcStockCheckItemList.value
      if (form.value.id != null) {
        updateStock_check(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStock_check(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功（草稿），审核后生成调整单")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 审核（审核即执行：盘盈生成入库单 / 盘亏生成出库单） */
function handleAudit(row) {
  if (!row || !row.id) {
    proxy.$modal.msgWarning("请选择要审核的盘点单")
    return
  }
  proxy.$modal.confirm('审核后将按差异自动生成盘盈入库单/盘亏出库单并联动库存，是否确认审核"' + row.checkNo + '"？').then(function() {
    return auditStock_check(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功，调整单已生成、库存已联动")
    getList()
  }).catch(() => {})
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除盘点单编号为"' + _ids + '"的数据项？').then(function() {
    return delStock_check(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function rowJxcStockCheckItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

function handleAddJxcStockCheckItem() {
  let obj = {}
  obj.rowNo = jxcStockCheckItemList.value.length + 1
  obj.productId = null
  obj.batchNo = ""
  obj.bookQty = 0
  obj.actualQty = 0
  obj.diffQty = 0
  obj.remark = null
  jxcStockCheckItemList.value.push(obj)
}

function handleDeleteJxcStockCheckItem() {
  if (checkedJxcStockCheckItem.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的盘点明细数据")
  } else {
    const items = jxcStockCheckItemList.value
    const checked = checkedJxcStockCheckItem.value
    jxcStockCheckItemList.value = items.filter(function(item) {
      return checked.indexOf(item.index) == -1
    })
  }
}

function handleJxcStockCheckItemSelectionChange(selection) {
  checkedJxcStockCheckItem.value = selection.map(item => item.index)
}

function handleExport() {
  proxy.download('jxc/stock_check/export', {
    ...queryParams.value
  }, `stock_check_${new Date().getTime()}.xlsx`)
}

loadProductOptions()
getList()
</script>
