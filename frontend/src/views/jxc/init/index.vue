<template>
  <div class="app-container">
    <el-alert
      title="期初数据导入（数据迁移）"
      type="info"
      :closable="false"
      description="用于系统上线时批量导入商品期初库存。流程：①下载模板 ②填写后上传校验（试运行不落库）③确认导入。仅允许对尚未建账（库存为0）的商品+批次导入。"
      show-icon
      style="margin-bottom: 16px"
    />

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>① 下载模板</b></template>
          <el-button type="primary" plain icon="Download" @click="downloadTemplate">下载期初库存导入模板</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>② 上传并校验（试运行）</b></template>
          <el-upload
            :show-file-list="false"
            :before-upload="handleBeforeUpload"
            :http-request="handleUpload"
            accept=".xlsx,.xls"
          >
            <el-button type="success" plain icon="Upload" :loading="parsing">选择 Excel 并校验</el-button>
          </el-upload>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><b>③ 确认导入</b></template>
          <el-button type="danger" plain icon="Check" :disabled="!validRows.length" :loading="importing" @click="handleCommit">
            导入校验通过的 {{ validRows.length }} 行
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb8" style="margin-top: 16px" v-if="parsed">
      <el-col :span="8">
        <el-statistic title="解析总行数" :value="summary.total" />
      </el-col>
      <el-col :span="8">
        <el-statistic title="校验通过" :value="summary.validCount" :value-style="{ color: '#67C23A' }" />
      </el-col>
      <el-col :span="8">
        <el-statistic title="校验失败" :value="summary.invalidCount" :value-style="{ color: '#F56C6C' }" />
      </el-col>
    </el-row>

    <el-table v-if="parsed" v-loading="parsing" :data="rows" :row-class-name="rowClass">
      <el-table-column label="行号" align="center" prop="rowNum" width="70" />
      <el-table-column label="商品编码" align="center" prop="productCode" width="110" />
      <el-table-column label="商品名称" align="center" prop="productName" width="140" />
      <el-table-column label="批次号" align="center" width="120">
        <template #default="scope">
          <span>{{ scope.row.batchNo === '' ? '（非批次）' : scope.row.batchNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生产日期" align="center" prop="productionDate" width="110" />
      <el-table-column label="到期日" align="center" prop="expiryDate" width="110" />
      <el-table-column label="数量" align="center" prop="quantity" width="100" />
      <el-table-column label="成本单价" align="center" prop="costPrice" width="100" />
      <el-table-column label="校验结果" align="center" min-width="240">
        <template #default="scope">
          <el-tag v-if="scope.row.valid" type="success">通过</el-tag>
          <template v-else>
            <el-tag type="danger" v-for="(err, idx) in scope.row.errors" :key="idx" style="margin: 2px">{{ err }}</el-tag>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="JxcInit">
import { parseInit, commitInit } from "@/api/jxc/init"

const { proxy } = getCurrentInstance()

const parsing = ref(false)
const importing = ref(false)
const parsed = ref(false)
const rows = ref([])
const summary = ref({ total: 0, validCount: 0, invalidCount: 0 })

const validRows = computed(() => rows.value.filter(r => r.valid))

/** 下载模板 */
function downloadTemplate() {
  proxy.download('jxc/init/template', {}, `期初库存导入模板_${new Date().getTime()}.xlsx`)
}

function handleBeforeUpload(file) {
  const isExcel = /\.(xlsx|xls)$/i.test(file.name)
  if (!isExcel) {
    proxy.$modal.msgError("仅支持 .xlsx / .xls 文件")
  }
  return isExcel
}

/** 上传解析（自定义上传） */
function handleUpload(options) {
  const formData = new FormData()
  formData.append("file", options.file)
  parsing.value = true
  parseInit(formData).then(response => {
    const data = response.data || {}
    rows.value = data.rows || []
    summary.value = {
      total: data.total || 0,
      validCount: data.validCount || 0,
      invalidCount: data.invalidCount || 0
    }
    parsed.value = true
    parsing.value = false
    if (summary.value.total === 0) {
      proxy.$modal.msgWarning("文件解析后没有数据行，请检查文件内容")
    } else {
      proxy.$modal.msgSuccess("解析完成：通过 " + summary.value.validCount + " 行，失败 " + summary.value.invalidCount + " 行")
    }
  }).catch(() => {
    parsing.value = false
  })
}

/** 确认导入（仅提交校验通过的行） */
function handleCommit() {
  const payload = validRows.value.map(r => ({
    productCode: r.productCode,
    batchNo: r.batchNo,
    productionDate: r.productionDate,
    expiryDate: r.expiryDate,
    quantity: r.quantity,
    costPrice: r.costPrice
  }))
  proxy.$modal.confirm('确认导入 ' + payload.length + ' 行期初数据？导入后将生成期初库存与台账流水。').then(function() {
    importing.value = true
    return commitInit(payload)
  }).then(response => {
    const data = response.data || {}
    importing.value = false
    proxy.$modal.alert('导入完成：成功 ' + data.successCount + ' 行，失败 ' + data.failCount + ' 行' +
      (data.failed && data.failed.length ? '\n失败明细：\n' + data.failed.join('\n') : '')).then(() => {
      parsed.value = false
      rows.value = []
      summary.value = { total: 0, validCount: 0, invalidCount: 0 }
    })
  }).catch(() => {
    importing.value = false
  })
}

/** 失败行标红 */
function rowClass({ row }) {
  return row.valid ? "" : "invalid-row"
}
</script>

<style scoped>
:deep(.invalid-row) {
  background-color: #fef0f0;
}
</style>
