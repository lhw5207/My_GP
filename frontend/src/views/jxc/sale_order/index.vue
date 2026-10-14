<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="销售单号" prop="saleNo">
        <el-input
          v-model="queryParams.saleNo"
          placeholder="请输入销售单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员" prop="memberId">
        <el-select v-model="queryParams.memberId" placeholder="请选择会员" clearable style="width: 200px">
          <el-option
            v-for="m in memberOptions"
            :key="m.id"
            :label="m.memberName"
            :value="m.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式" prop="payType">
        <el-select v-model="queryParams.payType" placeholder="请选择支付方式" clearable style="width: 200px">
          <el-option
            v-for="dict in jxc_pay_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="收款状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="请选择收款状态" clearable style="width: 200px">
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

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['jxc:sale_order:add']"
        >开单（POS）</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['jxc:sale_order:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="sale_orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="销售单号" align="center" prop="saleNo" width="170" />
      <el-table-column label="会员" align="center" width="120">
        <template #default="scope">
          <span>{{ memberNameOf(scope.row.memberId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="销售日期" align="center" prop="saleDate" width="110">
        <template #default="scope">
          <span>{{ parseTime(scope.row.saleDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总金额" align="center" prop="totalAmount" width="95" />
      <el-table-column label="优惠" align="center" prop="discountAmount" width="80" />
      <el-table-column label="实收/应收" align="center" width="170">
        <template #default="scope">
          <span>{{ formatAmount(scope.row.receivedAmount) }} / {{ formatAmount(scope.row.actualAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" prop="payType" width="100">
        <template #default="scope">
          <dict-tag :options="jxc_pay_type" :value="scope.row.payType" />
        </template>
      </el-table-column>
      <el-table-column label="收款状态" align="center" prop="payStatus" width="100">
        <template #default="scope">
          <dict-tag :options="jxc_writeoff_status" :value="scope.row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="jxc_sale_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="130" class-name="small-padding fixed-width">
        <template #default="scope">
          <span style="color: #909399; font-size: 12px;">已生效（退货走销售退货）</span>
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

    <!-- 开单（保存即生效：FEFO 扣库存 + 成本快照 + 应收） -->
    <el-dialog title="开单（保存即生效）" v-model="open" width="980px" append-to-body>
      <el-form ref="sale_orderRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="销售单号" prop="saleNo">
              <el-input v-model="form.saleNo" placeholder="如 XS20261014001" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会员" prop="memberId">
              <el-select v-model="form.memberId" placeholder="散客可不选" clearable style="width: 100%">
                <el-option
                  v-for="m in memberOptions"
                  :key="m.id"
                  :label="m.memberName + (m.phone ? '（' + m.phone + '）' : '')"
                  :value="m.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="销售日期" prop="saleDate">
              <el-date-picker clearable
                v-model="form.saleDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择销售日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="支付方式" prop="payType">
              <el-select v-model="form.payType" style="width: 100%">
                <el-option
                  v-for="dict in jxc_pay_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优惠金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0" :precision="2" :controls="false" style="width: 100%" @change="recalcReceived" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实收金额" prop="receivedAmount">
              <el-input-number v-model="form.receivedAmount" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="应收合计">
              <el-input :model-value="netAmountDisplay" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="欠款">
              <el-input :model-value="unpaidDisplay" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="center">销售明细（保存时按 FEFO 近效期批次自动扣减）</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddJxcSaleOrderItem">添加商品</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteJxcSaleOrderItem">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="jxcSaleOrderItemList" :row-class-name="rowJxcSaleOrderItemIndex" @selection-change="handleJxcSaleOrderItemSelectionChange" ref="jxcSaleOrderItem">
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
          <el-table-column label="数量" prop="quantity" width="130">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" :precision="4" :controls="false" placeholder="数量" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="售价" prop="price" width="130">
            <template #default="scope">
              <el-input-number v-model="scope.row.price" :min="0" :precision="4" :controls="false" placeholder="售价" style="width: 100%" @change="recalcRowAmount(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" width="120">
            <template #default="scope">
              <span>{{ formatAmount(scope.row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">保存并生效</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Sale_order">
import { listSale_order, getSale_order, delSale_order, addSale_order, updateSale_order } from "@/api/jxc/sale_order"
import { listMember } from "@/api/jxc/member"
import { listProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()
const { jxc_pay_type, jxc_writeoff_status, jxc_sale_order_status } = useDict("jxc_pay_type", "jxc_writeoff_status", "jxc_sale_order_status")

const sale_orderList = ref([])
const jxcSaleOrderItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedJxcSaleOrderItem = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const memberOptions = ref([])
const productOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    saleNo: null,
    memberId: null,
    payType: null,
    payStatus: null,
  },
  rules: {
    saleNo: [
      { required: true, message: "销售单号不能为空", trigger: "blur" }
    ],
    saleDate: [
      { required: true, message: "销售日期不能为空", trigger: "change" }
    ],
    payType: [
      { required: true, message: "支付方式不能为空", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 明细金额合计 */
const itemsTotal = computed(() => {
  return (jxcSaleOrderItemList.value || []).reduce((acc, it) => acc + (Number(it.amount) || 0), 0)
})

/** 应收合计（总额 - 优惠） */
const netAmountDisplay = computed(() => {
  const discount = Number(form.value.discountAmount) || 0
  const net = itemsTotal.value - discount
  return formatAmount(net > 0 ? net : 0)
})

/** 欠款 = 应收 - 实收 */
const unpaidDisplay = computed(() => {
  const discount = Number(form.value.discountAmount) || 0
  const net = itemsTotal.value - discount
  const received = Number(form.value.receivedAmount) || 0
  const unpaid = net - received
  return formatAmount(unpaid > 0 ? unpaid : 0)
})

/** 加载会员下拉 */
function loadMemberOptions() {
  if (memberOptions.value.length) return
  listMember({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    memberOptions.value = response.rows || []
  })
}

/** 加载商品下拉 */
function loadProductOptions() {
  if (productOptions.value.length) return
  listProduct({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    productOptions.value = response.rows || []
  })
}

/** 会员名称回显 */
function memberNameOf(id) {
  if (!id) return "散客"
  const m = memberOptions.value.find(item => item.id === id)
  return m ? m.memberName : id
}

/** 金额格式化 */
function formatAmount(v) {
  if (v === null || v === undefined || v === "") return "0.00"
  return Number(v).toFixed(2)
}

/** 选择商品带出标准售价 */
function handleProductChange(row) {
  const p = productOptions.value.find(item => item.id === row.productId)
  if (p) {
    row.price = p.salePrice
  }
  recalcRowAmount(row)
}

/** 行金额联动 */
function recalcRowAmount(row) {
  const qty = Number(row.quantity) || 0
  const price = Number(row.price) || 0
  row.amount = Number((qty * price).toFixed(2))
}

/** 优惠变化后默认实收=应收 */
function recalcReceived() {
  const discount = Number(form.value.discountAmount) || 0
  const net = itemsTotal.value - discount
  form.value.receivedAmount = net > 0 ? Number(net.toFixed(2)) : 0
}

/** 查询销售单列表 */
function getList() {
  loading.value = true
  listSale_order(queryParams.value).then(response => {
    sale_orderList.value = response.rows
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
    saleNo: null,
    memberId: null,
    saleDate: todayStr(),
    totalAmount: 0,
    discountAmount: 0,
    actualAmount: 0,
    receivedAmount: 0,
    payType: "CASH",
    payStatus: "0",
    status: "1",
    remark: null
  }
  jxcSaleOrderItemList.value = []
  proxy.resetForm("sale_orderRef")
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

/** 开单按钮操作 */
function handleAdd() {
  reset()
  loadMemberOptions()
  loadProductOptions()
  handleAddJxcSaleOrderItem()
  open.value = true
  title.value = "开单（保存即生效）"
}

/** 提交（保存即生效） */
function submitForm() {
  proxy.$refs["sale_orderRef"].validate(valid => {
    if (valid) {
      if (!jxcSaleOrderItemList.value.length) {
        proxy.$modal.msgWarning("请至少添加一行销售明细")
        return
      }
      const invalid = jxcSaleOrderItemList.value.some(it => !it.productId || !it.quantity || it.quantity <= 0)
      if (invalid) {
        proxy.$modal.msgWarning("明细中存在未选择商品或数量为 0 的行，请检查")
        return
      }
      // 欠款校验：有欠款必须选会员
      const discount = Number(form.value.discountAmount) || 0
      const net = itemsTotal.value - discount
      const received = Number(form.value.receivedAmount) || 0
      if (net - received > 0.001 && !form.value.memberId) {
        proxy.$modal.msgWarning("存在未收款金额，请选择会员挂账，或收清全款")
        return
      }
      jxcSaleOrderItemList.value.forEach((it, idx) => {
        recalcRowAmount(it)
        if (!it.rowNo) it.rowNo = idx + 1
      })
      form.value.jxcSaleOrderItemList = jxcSaleOrderItemList.value
      addSale_order(form.value).then(response => {
        proxy.$modal.msgSuccess("开单成功，库存已按 FEFO 扣减并生成流水/应收")
        open.value = false
        getList()
      })
    }
  })
}

/** 销售明细序号 */
function rowJxcSaleOrderItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

/** 销售明细添加 */
function handleAddJxcSaleOrderItem() {
  let obj = {}
  obj.rowNo = jxcSaleOrderItemList.value.length + 1
  obj.productId = null
  obj.quantity = 1
  obj.price = null
  obj.amount = 0
  obj.remark = null
  jxcSaleOrderItemList.value.push(obj)
}

/** 销售明细删除 */
function handleDeleteJxcSaleOrderItem() {
  if (checkedJxcSaleOrderItem.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的销售明细数据")
  } else {
    const items = jxcSaleOrderItemList.value
    const checked = checkedJxcSaleOrderItem.value
    jxcSaleOrderItemList.value = items.filter(function(item) {
      return checked.indexOf(item.index) == -1
    })
  }
}

/** 明细复选框选中 */
function handleJxcSaleOrderItemSelectionChange(selection) {
  checkedJxcSaleOrderItem.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('jxc/sale_order/export', {
    ...queryParams.value
  }, `sale_order_${new Date().getTime()}.xlsx`)
}

loadMemberOptions()
loadProductOptions()
getList()
</script>
