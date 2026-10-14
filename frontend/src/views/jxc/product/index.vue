<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="商品编码" prop="productCode">
            <el-input
               v-model="queryParams.productCode"
               placeholder="请输入商品编码"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="商品名称" prop="productName">
            <el-input
               v-model="queryParams.productName"
               placeholder="请输入商品名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="商品条码" prop="barcode">
            <el-input
               v-model="queryParams.barcode"
               placeholder="请输入商品条码"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="是否批次" prop="enableBatch">
            <el-select v-model="queryParams.enableBatch" placeholder="请选择" clearable style="width: 200px">
               <el-option label="是" value="1" />
               <el-option label="否" value="0" />
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
               v-hasPermi="['jxc:product:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['jxc:product:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['jxc:product:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['jxc:product:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="商品ID" align="center" prop="id" width="80" />
         <el-table-column label="商品编码" align="center" prop="productCode" />
         <el-table-column label="商品名称" align="center" prop="productName" />
         <el-table-column label="条码" align="center" prop="barcode" width="140" />
         <el-table-column label="单位" align="center" prop="unit" width="70" />
         <el-table-column label="规格" align="center" prop="spec" width="100" />
         <el-table-column label="参考进价" align="center" prop="purchasePrice" width="90" />
         <el-table-column label="标准售价" align="center" prop="salePrice" width="90" />
         <el-table-column label="库存下限" align="center" prop="stockMin" width="90" />
         <el-table-column label="保质期(天)" align="center" prop="shelfLifeDays" width="100" />
         <el-table-column label="批次管理" align="center" prop="enableBatch" width="90">
            <template #default="scope">
               <el-tag :type="scope.row.enableBatch === '1' ? 'success' : 'info'">
                  {{ scope.row.enableBatch === '1' ? '是' : '否' }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['jxc:product:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['jxc:product:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <!-- 添加或修改商品信息对话框 -->
      <el-dialog :title="title" v-model="open" width="620px" append-to-body>
         <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="商品编码" prop="productCode">
                     <el-input v-model="form.productCode" placeholder="请输入商品编码" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="商品名称" prop="productName">
                     <el-input v-model="form.productName" placeholder="请输入商品名称" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="商品分类" prop="categoryId">
                     <el-input-number v-model="form.categoryId" :min="1" :precision="0" :controls="false" style="width: 100%" placeholder="请输入分类ID（待分类模块后改为选择器）" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="商品条码" prop="barcode">
                     <el-input v-model="form.barcode" placeholder="请输入商品条码" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="计量单位" prop="unit">
                     <el-input v-model="form.unit" placeholder="请输入计量单位" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="规格" prop="spec">
                     <el-input v-model="form.spec" placeholder="请输入规格" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="参考进价" prop="purchasePrice">
                     <el-input-number v-model="form.purchasePrice" :min="0" :precision="4" :controls="false" style="width: 100%" placeholder="请输入参考进价" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="标准售价" prop="salePrice">
                     <el-input-number v-model="form.salePrice" :min="0" :precision="2" :controls="false" style="width: 100%" placeholder="请输入标准售价" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="库存下限" prop="stockMin">
                     <el-input-number v-model="form.stockMin" :min="0" :precision="4" :controls="false" style="width: 100%" placeholder="请输入库存预警下限" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="库存上限" prop="stockMax">
                     <el-input-number v-model="form.stockMax" :min="0" :precision="4" :controls="false" style="width: 100%" placeholder="请输入库存预警上限" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="保质期(天)" prop="shelfLifeDays">
                     <el-input-number v-model="form.shelfLifeDays" :min="0" :precision="0" :controls="false" style="width: 100%" placeholder="请输入保质期天数" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="批次管理" prop="enableBatch">
                     <el-radio-group v-model="form.enableBatch">
                        <el-radio value="1">是</el-radio>
                        <el-radio value="0">否</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                     <el-radio-group v-model="form.status">
                        <el-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
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

<script setup name="JxcProduct">
import { listProduct, getProduct, delProduct, addProduct, updateProduct } from "@/api/jxc/product"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const productList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productCode: undefined,
    productName: undefined,
    barcode: undefined,
    enableBatch: undefined
  },
  rules: {
    categoryId: [{ required: true, message: "商品分类不能为空", trigger: "blur" }],
    productCode: [{ required: true, message: "商品编码不能为空", trigger: "blur" }],
    productName: [{ required: true, message: "商品名称不能为空", trigger: "blur" }],
    unit: [{ required: true, message: "计量单位不能为空", trigger: "blur" }],
    purchasePrice: [{ required: true, message: "参考进价不能为空", trigger: "blur" }],
    salePrice: [{ required: true, message: "标准售价不能为空", trigger: "blur" }],
    stockMin: [{ required: true, message: "库存预警下限不能为空", trigger: "blur" }],
    enableBatch: [{ required: true, message: "是否批次管理不能为空", trigger: "change" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询商品信息列表 */
function getList() {
  loading.value = true
  listProduct(queryParams.value).then(response => {
    productList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    categoryId: undefined,
    productCode: undefined,
    productName: undefined,
    barcode: undefined,
    unit: undefined,
    spec: undefined,
    purchasePrice: undefined,
    salePrice: undefined,
    stockMin: undefined,
    stockMax: undefined,
    shelfLifeDays: undefined,
    enableBatch: "0",
    status: "0",
    remark: undefined
  }
  proxy.resetForm("productRef")
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

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加商品信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getProduct(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商品信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["productRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateProduct(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProduct(form.value).then(() => {
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
  const delIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除商品信息编号为"' + delIds + '"的数据项？').then(function() {
    return delProduct(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('jxc/product/export', {
    ...queryParams.value
  }, `jxc_product_${new Date().getTime()}.xlsx`)
}
</script>
