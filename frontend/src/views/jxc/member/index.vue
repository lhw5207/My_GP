<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="会员号" prop="memberNo">
            <el-input
               v-model="queryParams.memberNo"
               placeholder="请输入会员号"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="会员姓名" prop="memberName">
            <el-input
               v-model="queryParams.memberName"
               placeholder="请输入会员姓名"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="手机号" prop="phone">
            <el-input
               v-model="queryParams.phone"
               placeholder="请输入手机号"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="会员等级" prop="level">
            <el-select v-model="queryParams.level" placeholder="请选择会员等级" clearable style="width: 200px">
               <el-option
                  v-for="dict in jxc_member_level"
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
               v-hasPermi="['jxc:member:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['jxc:member:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['jxc:member:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['jxc:member:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="memberList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="ID" align="center" prop="id" width="70" />
         <el-table-column label="会员号" align="center" prop="memberNo" />
         <el-table-column label="会员姓名" align="center" prop="memberName" />
         <el-table-column label="手机号" align="center" prop="phone" width="130" />
         <el-table-column label="等级" align="center" prop="level" width="100">
            <template #default="scope">
               <dict-tag :options="jxc_member_level" :value="scope.row.level" />
            </template>
         </el-table-column>
         <el-table-column label="折扣(%)" align="center" prop="discount" width="90" />
         <el-table-column label="积分" align="center" prop="points" width="80" />
         <el-table-column label="储值余额" align="center" prop="balance" width="100" />
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['jxc:member:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['jxc:member:remove']">删除</el-button>
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

      <!-- 添加或修改会员对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="memberRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
               <el-col :span="12">
                  <el-form-item label="会员号" prop="memberNo">
                     <el-input v-model="form.memberNo" placeholder="请输入会员号" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="会员姓名" prop="memberName">
                     <el-input v-model="form.memberName" placeholder="请输入会员姓名" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                     <el-input v-model="form.phone" placeholder="请输入手机号" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="会员等级" prop="level">
                     <el-select v-model="form.level" placeholder="请选择会员等级" style="width: 100%">
                        <el-option
                           v-for="dict in jxc_member_level"
                           :key="dict.value"
                           :label="dict.label"
                           :value="dict.value"
                        />
                     </el-select>
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="折扣(%)" prop="discount">
                     <el-input-number v-model="form.discount" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" placeholder="如 95 表示95折" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="积分" prop="points">
                     <el-input-number v-model="form.points" :min="0" :precision="0" :controls="false" style="width: 100%" placeholder="请输入积分" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-row>
               <el-col :span="12">
                  <el-form-item label="储值余额" prop="balance">
                     <el-input-number v-model="form.balance" :min="0" :precision="2" :controls="false" style="width: 100%" placeholder="请输入储值余额" />
                  </el-form-item>
               </el-col>
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

<script setup name="JxcMember">
import { listMember, getMember, delMember, addMember, updateMember } from "@/api/jxc/member"

const { proxy } = getCurrentInstance()
const { sys_normal_disable, jxc_member_level } = useDict("sys_normal_disable", "jxc_member_level")

const memberList = ref([])
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
    memberNo: undefined,
    memberName: undefined,
    phone: undefined,
    level: undefined
  },
  rules: {
    memberNo: [{ required: true, message: "会员号不能为空", trigger: "blur" }],
    memberName: [{ required: true, message: "会员姓名不能为空", trigger: "blur" }],
    level: [{ required: true, message: "会员等级不能为空", trigger: "change" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询会员列表 */
function getList() {
  loading.value = true
  listMember(queryParams.value).then(response => {
    memberList.value = response.rows
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
    memberNo: undefined,
    memberName: undefined,
    phone: undefined,
    level: "0",
    discount: 100,
    points: 0,
    balance: 0,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("memberRef")
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
  title.value = "添加会员"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getMember(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改会员"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["memberRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateMember(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMember(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除会员编号为"' + delIds + '"的数据项？').then(function() {
    return delMember(delIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('jxc/member/export', {
    ...queryParams.value
  }, `jxc_member_${new Date().getTime()}.xlsx`)
}

getList()
</script>
