<template>
  <div class="app-container cashier-container">
    <el-row :gutter="16">
      <!-- 左侧：扫码/搜索加购 + 购物车 -->
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="cart-card">
          <div class="scan-bar">
            <el-input
              ref="scanRef"
              v-model="scanKeyword"
              size="large"
              placeholder="扫码枪输入条码 / 输入商品名称后回车加购"
              clearable
              @keyup.enter="handleScan"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button size="large" type="primary" @click="handleScan">加 购</el-button>
          </div>

          <el-table :data="cart" empty-text="购物车为空，扫码或输入商品名称加购" class="cart-table">
            <el-table-column label="#" type="index" width="46" align="center" />
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div class="prod-cell">
                  <span class="prod-name">{{ scope.row.productName }}</span>
                  <span class="prod-code">{{ scope.row.productCode }}<template v-if="scope.row.unit"> · {{ scope.row.unit }}</template></span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="售价" width="140" align="center">
              <template #default="scope">
                <el-input-number v-model="scope.row.price" :min="0" :precision="2" :controls="false" style="width: 100%" @change="touchReceived" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="190" align="center">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="1" :precision="2" style="width: 100%" @change="touchReceived" />
              </template>
            </el-table-column>
            <el-table-column label="金额" width="110" align="center">
              <template #default="scope">
                <span class="row-amount">{{ formatAmount(rowAmount(scope.row)) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button type="danger" link icon="Delete" @click="removeRow(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="cart-foot">
            <el-button icon="Delete" @click="clearCart">清空购物车</el-button>
            <span class="cart-total">共 <b>{{ cart.length }}</b> 种商品，合计 <b>{{ formatAmount(itemsTotal) }}</b> 元</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：结算面板 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="settle-card">
          <template #header><span class="settle-title">结算</span></template>

          <div class="amount-lines">
            <div class="amount-line">
              <span>商品合计</span><span>{{ formatAmount(itemsTotal) }}</span>
            </div>
            <div class="amount-line">
              <span>优惠金额</span>
              <el-input-number v-model="discount" :min="0" :precision="2" :controls="false" style="width: 130px" @change="touchReceived" />
            </div>
            <div class="amount-line receivable">
              <span>应收金额</span><span>{{ formatAmount(actualAmount) }}</span>
            </div>
          </div>

          <el-form label-width="70px" class="settle-form">
            <el-form-item label="会员">
              <el-select v-model="memberId" placeholder="散客可不选；挂账必选" clearable filterable style="width: 100%">
                <el-option
                  v-for="m in memberOptions"
                  :key="m.id"
                  :label="m.memberName + (m.phone ? '（' + m.phone + '）' : '')"
                  :value="m.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="支付方式">
              <el-select v-model="payType" style="width: 100%">
                <el-option
                  v-for="dict in jxc_pay_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="实收金额">
              <el-input-number v-model="received" :min="0" :precision="2" :controls="false" style="width: 100%" @change="receivedTouched = true" />
            </el-form-item>
          </el-form>

          <div class="change-box" :class="{ positive: changeAmount > 0 }">
            <template v-if="changeAmount > 0">找零：{{ formatAmount(changeAmount) }} 元</template>
            <template v-else-if="unpaidAmount > 0">待收（挂账）：{{ formatAmount(unpaidAmount) }} 元</template>
            <template v-else>已收清</template>
          </div>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            v-hasPermi="['jxc:cashier:add']"
            @click="handleSubmit"
          >结算并生效（Enter 加购，本按钮出单）</el-button>
          <div class="settle-tip">保存即生效：FEFO 近效期先出，自动扣库存并快照成本；单号自动生成</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 多候选商品选择 -->
    <el-dialog v-model="pickerOpen" title="匹配到多个商品，请选择" width="680px" append-to-body>
      <el-table :data="pickerList" highlight-current-row @row-click="handlePick" class="picker-table">
        <el-table-column label="商品名称" prop="productName" min-width="160" />
        <el-table-column label="编码" prop="productCode" width="110" align="center" />
        <el-table-column label="条码" prop="barcode" width="130" align="center" />
        <el-table-column label="单位" prop="unit" width="70" align="center" />
        <el-table-column label="售价" width="90" align="center">
          <template #default="scope">{{ formatAmount(scope.row.salePrice) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="Cashier">
import { findCashierProduct, submitCashierOrder } from "@/api/jxc/cashier"
import { listMember } from "@/api/jxc/member"

const { proxy } = getCurrentInstance()
const { jxc_pay_type } = useDict("jxc_pay_type")

const scanRef = ref(null)
const scanKeyword = ref("")
const cart = ref([])
const memberOptions = ref([])
const memberId = ref(null)
const payType = ref("CASH")
const discount = ref(0)
const received = ref(0)
const receivedTouched = ref(false)
const submitting = ref(false)
const pickerOpen = ref(false)
const pickerList = ref([])

/** 行金额 */
function rowAmount(row) {
  const qty = Number(row.quantity) || 0
  const price = Number(row.price) || 0
  return Number((qty * price).toFixed(2))
}

/** 商品合计 */
const itemsTotal = computed(() => {
  return cart.value.reduce((acc, it) => acc + rowAmount(it), 0)
})

/** 应收 = 合计 - 优惠（下限 0，与后端口径一致） */
const actualAmount = computed(() => {
  const net = itemsTotal.value - (Number(discount.value) || 0)
  return net > 0 ? Number(net.toFixed(2)) : 0
})

/** 待收/挂账 = 应收 - 实收 */
const unpaidAmount = computed(() => {
  const unpaid = actualAmount.value - (Number(received.value) || 0)
  return unpaid > 0 ? Number(unpaid.toFixed(2)) : 0
})

/** 找零 = 实收 - 应收（>0 时提示） */
const changeAmount = computed(() => {
  const change = (Number(received.value) || 0) - actualAmount.value
  return change > 0 ? Number(change.toFixed(2)) : 0
})

function formatAmount(v) {
  if (v === null || v === undefined || v === "") return "0.00"
  return Number(v).toFixed(2)
}

/** 购物车/优惠变动时，实收默认跟随应收（收银员手改过则不再跟随） */
function touchReceived() {
  if (!receivedTouched.value) {
    received.value = actualAmount.value
  }
}

function focusScan() {
  nextTick(() => {
    scanRef.value && scanRef.value.focus && scanRef.value.focus()
  })
}

/** 扫码/搜索回车：纯数字≥6位按条码精确查，否则按名称模糊查 */
function handleScan() {
  const kw = scanKeyword.value.trim()
  if (!kw) return
  const isBarcode = /^\d{6,}$/.test(kw)
  findCashierProduct(isBarcode ? { barcode: kw } : { keyword: kw }).then(response => {
    const list = response.data || []
    if (list.length === 1) {
      addToCart(list[0])
    } else if (list.length === 0) {
      proxy.$modal.msgError(isBarcode ? `未找到条码为 ${kw} 的在售商品` : `未找到名称含「${kw}」的在售商品`)
    } else {
      pickerList.value = list
      pickerOpen.value = true
    }
    scanKeyword.value = ""
    focusScan()
  }).catch(() => {
    focusScan()
  })
}

/** 弹窗选中商品 */
function handlePick(row) {
  addToCart(row)
  pickerOpen.value = false
  focusScan()
}

/** 加入购物车：同商品数量累加 */
function addToCart(p) {
  const exist = cart.value.find(it => it.productId === p.id)
  if (exist) {
    exist.quantity = Number((Number(exist.quantity) + 1).toFixed(2))
    touchReceived()
  } else {
    cart.value.push({
      productId: p.id,
      productCode: p.productCode,
      productName: p.productName,
      unit: p.unit,
      price: p.salePrice,
      quantity: 1
    })
    touchReceived()
  }
}

function removeRow(index) {
  cart.value.splice(index, 1)
  touchReceived()
}

function clearCart() {
  cart.value = []
  discount.value = 0
  memberId.value = null
  receivedTouched.value = false
  received.value = 0
  focusScan()
}

function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, "0")
  const day = String(d.getDate()).padStart(2, "0")
  return `${d.getFullYear()}-${m}-${day}`
}

/** 结算提交（后端实收封顶为应收） */
function handleSubmit() {
  if (submitting.value) return
  if (!cart.value.length) {
    proxy.$modal.msgWarning("购物车为空，请先扫码或搜索加购")
    return
  }
  const invalid = cart.value.some(it => !it.productId || !it.quantity || it.quantity <= 0 || it.price === null || it.price === undefined)
  if (invalid) {
    proxy.$modal.msgWarning("存在数量为 0 或售价缺失的商品，请检查购物车")
    return
  }
  if (unpaidAmount.value > 0.001 && !memberId.value) {
    proxy.$modal.msgWarning("存在待收金额，请选择会员挂账，或收清全款")
    return
  }
  const data = {
    saleNo: null,
    memberId: memberId.value || null,
    saleDate: todayStr(),
    discountAmount: Number(discount.value) || 0,
    receivedAmount: Math.min(Number(received.value) || 0, actualAmount.value),
    payType: payType.value,
    remark: "前台收银",
    jxcSaleOrderItemList: cart.value.map((it, idx) => ({
      rowNo: idx + 1,
      productId: it.productId,
      quantity: it.quantity,
      price: it.price
    }))
  }
  submitting.value = true
  submitCashierOrder(data).then(response => {
    const saleNo = response.data || ""
    const changeTip = changeAmount.value > 0 ? `，找零 ${formatAmount(changeAmount.value)} 元` : ""
    proxy.$modal.msgSuccess(`结算成功，单号 ${saleNo}${changeTip}；库存已按 FEFO 扣减`)
    clearCart()
  }).finally(() => {
    submitting.value = false
  })
}

/** 会员下拉 */
function loadMemberOptions() {
  listMember({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    memberOptions.value = response.rows || []
  })
}

loadMemberOptions()
focusScan()
</script>

<style lang="scss" scoped>
.cashier-container {
  .scan-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 14px;

    .el-input {
      flex: 1;
    }
  }

  .cart-card {
    min-height: 420px;
  }

  .prod-cell {
    display: flex;
    flex-direction: column;
    line-height: 1.5;

    .prod-name {
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .prod-code {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .row-amount {
    font-weight: 600;
  }

  .cart-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;

    .cart-total {
      font-size: 13px;
      color: var(--el-text-color-secondary);

      b {
        color: var(--el-color-danger);
        font-size: 15px;
        margin: 0 2px;
      }
    }
  }

  .settle-card {
    .settle-title {
      font-size: 14px;
      font-weight: 600;
    }
  }

  .amount-lines {
    margin-bottom: 14px;

    .amount-line {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 0;
      font-size: 13px;
      color: var(--el-text-color-regular);

      &.receivable {
        border-top: 1px dashed var(--el-border-color);
        margin-top: 4px;
        padding-top: 10px;
        font-size: 15px;
        font-weight: 600;
        color: var(--el-color-danger);
      }
    }
  }

  .change-box {
    margin: 10px 0 14px;
    padding: 10px 12px;
    border-radius: 6px;
    text-align: center;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-light);

    &.positive {
      color: var(--el-color-success);
      background: var(--el-color-success-light-9);
      font-weight: 700;
      font-size: 16px;
    }
  }

  .submit-btn {
    width: 100%;
    letter-spacing: 1px;
  }

  .settle-tip {
    margin-top: 10px;
    font-size: 12px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }

  .picker-table {
    cursor: pointer;
  }
}
</style>
