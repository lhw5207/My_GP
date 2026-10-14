<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="单号" prop="payNo">
        <el-input
          v-model="queryParams.payNo"
          placeholder="请输入收付款单号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="targetType">
        <el-select v-model="queryParams.targetType" placeholder="请选择类型" clearable style="width: 200px">
          <el-option label="供应商付款" value="1" />
          <el-option label="会员收款" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Money" @click="handleAdd('1')" v-hasPermi="['jxc:fin:add']">付款登记</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Wallet" @click="handleAdd('2')" v-hasPermi="['jxc:fin:add']">收款登记</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="paymentList">
      <el-table-column label="单号" align="center" prop="payNo" width="170" />
      <el-table-column label="类型" align="center" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.targetType === '1' ? 'warning' : 'success'">
            {{ scope.row.targetType === '1' ? '付款' : '收款' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="往来单位" align="center" prop="targetName" width="150" />
      <el-table-column label="日期" align="center" prop="payDate" width="110" />
      <el-table-column label="金额" align="center" prop="amount" width="110" />
      <el-table-column label="支付方式" align="center" prop="payType" width="100">
        <template #default="scope">
          <dict-tag :options="jxc_pay_type" :value="scope.row.payType" />
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="createBy" width="90" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">核销明细</el-button>
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

    <!-- 登记对话框（保存即生效：一对多核销） -->
    <el-dialog :title="title" v-model="open" width="980px" append-to-body>
      <el-form ref="paymentRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="单号" prop="payNo">
              <el-input v-model="form.payNo" :placeholder="form.targetType === '1' ? '如 FK20261014001' : '如 SK20261014001'" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="日期" prop="payDate">
              <el-date-picker clearable
                v-model="form.payDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                placeholder="请选择日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
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
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="类型" prop="targetType">
              <el-radio-group v-model="form.targetType" @change="handleTypeChange">
                <el-radio value="1">供应商付款</el-radio>
                <el-radio value="2">会员收款</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="form.targetType === '1' ? '供应商' : '会员'" prop="targetId">
              <el-select v-model="form.targetId" :placeholder="form.targetType === '1' ? '请选择供应商' : '请选择会员'" filterable style="width: 100%" @change="loadUnsettled">
                <el-option
                  v-for="opt in (form.targetType === '1' ? supplierOptions : memberOptions)"
                  :key="opt.id"
                  :label="form.targetType === '1' ? opt.supplierName : opt.memberName"
                  :value="opt.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="本单总额">
              <el-input :model-value="totalWriteoffDisplay" disabled placeholder="= 核销金额合计（差额视为预收/预付）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="备注" />
        </el-form-item>
        <el-divider content-position="center">
          待核销单据（{{ form.targetType === '1' ? '应付' : '应收' }}，一对多核销 / 支持部分核销）
        </el-divider>
        <el-table :data="unsettledList" :row-class-name="rowIndex" ref="unsettledRef">
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="单据号" align="center" prop="sourceNo" width="170" />
          <el-table-column label="来源" align="center" prop="sourceType" width="120" />
          <el-table-column label="单据金额" align="center" prop="amount" width="110" />
          <el-table-column label="已核销" align="center" prop="paidAmount" width="100" />
          <el-table-column label="待核销余额" align="center" prop="balance" width="110" />
          <el-table-column label="本次核销金额" align="center" width="200">
            <template #default="scope">
              <el-input-number v-model="scope.row.writeoffAmount" :min="0" :max="Number(scope.row.balance)" :precision="2" :controls="false" style="width: 130px" />
              <el-button link type="primary" style="margin-left: 6px" @click="fillAll(scope.row)">全额</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!unsettledList.length" description="暂无待核销单据（请先选择往来单位）" :image-size="60" />
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">登记并核销</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 核销明细对话框 -->
    <el-dialog title="核销明细" v-model="viewOpen" width="680px" append-to-body>
      <el-table :data="viewItems">
        <el-table-column label="序号" align="center" prop="rowNo" width="60" />
        <el-table-column label="单据类型" align="center" width="100">
          <template #default="scope">
            <span>{{ scope.row.billType === '1' ? '应付' : '应收' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="往来单号" align="center" prop="billNo" />
        <el-table-column label="核销金额" align="center" prop="writeoffAmount" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="JxcFinance">
import { listPayment, getPayment, addPayment, listUnsettled } from "@/api/jxc/finance"
import { listSupplier } from "@/api/jxc/supplier"
import { listMember } from "@/api/jxc/member"

const { proxy } = getCurrentInstance()
const { jxc_pay_type } = useDict("jxc_pay_type")

const paymentList = ref([])
const unsettledList = ref([])
const viewItems = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")

const supplierOptions = ref([])
const memberOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    payNo: null,
    targetType: null
  },
  rules: {
    payNo: [{ required: true, message: "单号不能为空", trigger: "blur" }],
    payDate: [{ required: true, message: "日期不能为空", trigger: "change" }],
    targetType: [{ required: true, message: "请选择类型", trigger: "change" }],
    targetId: [{ required: true, message: "请选择往来单位", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 核销合计 */
const totalWriteoffDisplay = computed(() => {
  const sum = (unsettledList.value || []).reduce((acc, it) => acc + (Number(it.writeoffAmount) || 0), 0)
  return Number(sum).toFixed(2)
})

function loadSupplierOptions() {
  if (supplierOptions.value.length) return
  listSupplier({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    supplierOptions.value = response.rows || []
  })
}

function loadMemberOptions() {
  if (memberOptions.value.length) return
  listMember({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    memberOptions.value = response.rows || []
  })
}

function rowIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

function fillAll(row) {
  row.writeoffAmount = Number(row.balance)
}

function handleTypeChange() {
  form.value.targetId = null
  unsettledList.value = []
}

/** 加载未核销单据 */
function loadUnsettled() {
  if (!form.value.targetId) return
  listUnsettled(form.value.targetType, form.value.targetId).then(response => {
    const rows = response.data || []
    rows.forEach(r => {
      r.writeoffAmount = 0
    })
    unsettledList.value = rows
  })
}

function getList() {
  loading.value = true
  listPayment(queryParams.value).then(response => {
    paymentList.value = response.rows
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
    payNo: null,
    targetType: "1",
    targetId: null,
    payDate: todayStr(),
    amount: null,
    payType: "TRANSFER",
    remark: null
  }
  unsettledList.value = []
  proxy.resetForm("paymentRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 登记入口：targetType 1付款 2收款 */
function handleAdd(targetType) {
  reset()
  loadSupplierOptions()
  loadMemberOptions()
  form.value.targetType = targetType
  form.value.payNo = (targetType === "1" ? "FK" : "SK") + todayStr().replace(/-/g, "") + "001"
  open.value = true
  title.value = targetType === "1" ? "供应商付款登记（一对多核销应付）" : "会员收款登记（一对多核销应收）"
}

/** 查看核销明细 */
function handleView(row) {
  getPayment(row.id).then(response => {
    viewItems.value = (response.data && response.data.jxcPaymentItemList) || []
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs["paymentRef"].validate(valid => {
    if (valid) {
      const items = (unsettledList.value || []).filter(it => Number(it.writeoffAmount) > 0)
      if (!items.length) {
        proxy.$modal.msgWarning("请至少为一张待核销单据填写核销金额")
        return
      }
      const payload = {
        payNo: form.value.payNo,
        targetType: form.value.targetType,
        targetId: form.value.targetId,
        payDate: form.value.payDate,
        payType: form.value.payType,
        amount: Number(totalWriteoffDisplay.value),
        remark: form.value.remark,
        jxcPaymentItemList: items.map(it => ({
          billType: form.value.targetType === "1" ? "1" : "2",
          billId: it.id,
          writeoffAmount: Number(it.writeoffAmount)
        }))
      }
      addPayment(payload).then(() => {
        proxy.$modal.msgSuccess("登记成功，台账已核销")
        open.value = false
        getList()
      })
    }
  })
}

loadSupplierOptions()
loadMemberOptions()
getList()
</script>
