# 代码生成链路报告（jxc_product 商品模块）

> 日期：2026-10-14 ｜ 目标：跑通"建表 → 生成 → 配权限"全链路（毕设 M1 里程碑）
> 结果：**全链路验证通过**（后端 CRUD API ✅ / 菜单路由 ✅ / 前端 Vue3 页面编译 ✅）

---

## 一、链路执行记录

| 步骤 | 操作 | 结果 |
|---|---|---|
| 1 | 表准备 | `jxc_product`（ry_jxc 库，含 5 条演示数据） |
| 2 | 生成器导入 | `POST /tool/gen/importTable?tables=jxc_product`（tableId=1） |
| 3 | 生成配置 | `gen_table` 更新：package=`com.ruoyi.jxc`、module=`jxc`、business=`product`、class=`JxcProduct`、function=`商品信息`、author=`vivi` |
| 4 | 生成代码 | `GET /tool/gen/batchGenCode?tables=jxc_product` → zip（9 个文件） |
| 5 | 后端落位 | controller → `ruoyi-admin/.../com/ruoyi/jxc/controller/`；domain/mapper/service → `ruoyi-system/.../com/ruoyi/jxc/`；mapper.xml → `ruoyi-system/src/main/resources/mapper/jxc/` |
| 6 | 菜单与授权 | `sql/jxc_menu_product.sql`：新建"进销存管理"目录(2000) + 商品信息菜单(2001) + 5 按钮(2002-2006)，授权 admin 角色 |
| 7 | 后端重建 | `mvn clean package -DskipTests` → BUILD SUCCESS → JDK8 启动 |
| 8 | API 验证 | `/jxc/product/list` 返回 total=5；**完整 CRUD 测试全通**（新增→查询→修改→删除→复查 0 条） |
| 9 | 菜单路由 | `/getRouters` 含 `jxc` 与 `jxc/product/index` |
| 10 | 前端页面 | RuoYi-Vue3：`src/views/jxc/product/index.vue`（Vue3 手工移植）+ `src/api/jxc/product.js`；vite 编译 200 无错误 |

## 二、关键说明

### 2.1 生成器模板为 Vue2，前端页面需移植
RuoYi-Vue（后端）自带的 velocity 模板生成 Vue2 + Element UI 页面；本项目前端为 **RuoYi-Vue3（Vue3 + Element Plus）**，因此前端页面做了手工移植，主要改动：
- `@keyup.enter.native` → `@keyup.enter`；`slot-scope` → `#default`；`:visible.sync` → `v-model`；`slot="footer"` → `#footer`
- 图标名 `el-icon-search` → `Search` 形式；表格操作列 `type="text"` → `link type="primary"`
- 脚本改用 `<script setup name="JxcProduct">` + `proxy.$modal / proxy.download`
- 表单增强：数字字段用 `el-input-number`（精度：数量4位/金额2位）、批次管理与状态用单选、状态下拉用 `sys_normal_disable` 字典

### 2.2 后续生成其他模块的流程（SOP）
1. `POST /tool/gen/importTable?tables=jxc_xxx`
2. SQL 更新 `gen_table`（package/module/business/function 等）
3. `GET /tool/gen/batchGenCode?tables=jxc_xxx` 下载 zip
4. 后端文件按第 5 步落位；菜单 SQL 参照 `jxc_menu_product.sql` 改造（挂到"进销存管理"目录 2000 下）
5. 前端：**将生成的 Vue2 页面按 2.1 规则移植到 Vue3**（建议做成移植清单，逐步积累模板）
6. 重建后端 → 重启 → 验证

### 2.3 已知待办
- 商品表单的"商品分类"目前为数字输入（`categoryId`），待生成分类模块后替换为下拉选择器
- 生成器配置已更新（`ruoyi-generator/generator.yml`：author=vivi、packageName=com.ruoyi.jxc），后续导入自动带默认值
- 登录验证码已临时关闭（`sys_config: sys.account.captchaEnabled=false`），答辩前恢复为 true

## 四、批次二：基础资料三模块（商品分类/供应商/会员）

> 2026-10-14 下午追加 ｜ 状态：代码落位 + 编译验证全部通过（运行验证待下次启动环境）

| 模块 | 接口前缀 | 菜单ID | 页面 | 验证 |
|---|---|---|---|---|
| 商品分类 JxcProductCategory | `/jxc/category` | 2007-2012 | `views/jxc/category/index.vue` | 后端编译 ✅ / vite 构建 ✅ |
| 供应商 JxcSupplier | `/jxc/supplier` | 2013-2018 | `views/jxc/supplier/index.vue` | 后端编译 ✅ / vite 构建 ✅ |
| 会员 JxcMember | `/jxc/member` | 2019-2024 | `views/jxc/member/index.vue` | 后端编译 ✅ / vite 构建 ✅ |

**本次改进点**：
1. 商品表单"商品分类"由数字输入升级为**下拉选择器**（对接 `GET /jxc/category/list`，打开弹窗时懒加载）
2. 会员模块使用字典 `jxc_member_level`（普通/银卡/金卡）做等级展示与选择
3. 所有页面补齐初始 `getList()` 调用（RuoYi-Vue3 `<script setup>` 规范：脚本末尾需显式调用 `getList()`，否则列表不加载——**重要经验，后续移植必查**）
4. 菜单 SQL：`sql/jxc_menu_base.sql`（三模块菜单 + 按钮 + admin 授权）

**验证记录**：
- `mvn package -DskipTests` → BUILD SUCCESS（19.7s，7 模块全部编译通过）
- `npm run build:prod`（Vite 生产构建）→ 成功产出 dist，4 个业务页面编译无错误

**遗留说明**：本次按用户要求"先停环境"，三个新模块的**运行态验证**（接口 CRUD 实测、页面渲染）留待下次启动环境后执行（后端 8080 / 前端 80 / Redis 6379）。

---

## 五、批次三：采购订单（主子表）★ 关键突破

> 2026-10-14 下午追加 ｜ 状态：**全链路验证通过**（接口增/改/删级联 + 页面编译）

### 5.1 重大效率突破：内置 Vue3 模板（element-plus）

发现 RuoYi-Vue 3.9.x 自带 **`vm/vue/v3` 模板**（对应 Vue3 + Element Plus 前端），通过 `gen_table.tpl_web_type='element-plus'` 即可**直接生成 Vue3 页面（含主子表编辑块）**，无需再手工移植！

- 模板选择逻辑：`VelocityUtils.getTemplateList` — `tplWebType == "element-plus"` → `vm/vue/v3`
- v3 模板输出的页面特征：`<script setup name="Xxx">`、`ref="queryRef"`、`#default` 插槽、`v-model:page` 分页、操作列 `link` 按钮
- **主子表（tpl_category='sub'）同样支持**，生成含子表编辑区（添加/删除行、选中行管理）的完整页面
- ⚠️ 结论：**后续所有模块导入时统一设置 `tpl_web_type='element-plus'`**，此前手工移植（product/category/supplier/member）是最终一致但更费力的旧路线

### 5.2 采购订单模块记录

| 项 | 内容 |
|---|---|
| 表 | `jxc_purchase_order` + `jxc_purchase_order_item`（主子表，FK=order_id） |
| 生成配置 | tpl_category='sub'、tpl_web_type='element-plus'、business_name='purchase_order' |
| 后端 | 10 个文件（含 sub-domain + 级联 ServiceImpl：新增级联插入、修改先删后插、删除级联清理） |
| 前端页面 | `views/jxc/purchase_order/index.vue`（Vue3 主子表）+ API |
| 菜单 | `sql/jxc_menu_purchase.sql`：新建"采购管理"目录(2025) + 采购订单菜单(2026) + 5 按钮，授权 admin |
| 页面增强 | ① 供应商/商品改为下拉选择器 ② **金额联动**：选商品带出参考进价、数量×单价自动算行金额、表头自动汇总总金额 ③ 状态用字典 `jxc_purchase_order_status` 展示 ④ 新建默认单据日期=今天、默认带一行明细 ⑤ 提交前校验（至少一行、商品必选、数量>0） ⑥ 审核字段移出表单（工作流管理） |
| 验证 | 新增（2行明细）✅ / 列表 ✅ / 详情含明细 ✅ / 修改（明细 1→2 行替换，旧行清理）✅ / 删除（明细级联清零）✅ / 前端编译 200 ✅ |

### 5.3 后端已有的批量能力（备忘）

生成的 `JxcPurchaseOrderServiceImpl` 已含：`insertJxcPurchaseOrder`（主表插入 + 子表批量插入）、`updateJxcPurchaseOrder`（先删子表再插）、`deleteJxcPurchaseOrderByIds`（级联删子表）。**下一批次做采购入库时，需要在此基础上追加自定义逻辑**：审核时库存联动（stock +、flow +、应付 +）、状态机流转、FEFO 等——这是毕设"定制开发"的主体。

---

## 六、模块清单（已落地代码）

| 文件 | 位置 |
|---|---|
| JxcProductController.java | backend/ruoyi-admin/src/main/java/com/ruoyi/jxc/controller/ |
| JxcProduct / JxcProductMapper / IJxcProductService / JxcProductServiceImpl | backend/ruoyi-system/src/main/java/com/ruoyi/jxc/{domain,mapper,service} |
| JxcProductMapper.xml | backend/ruoyi-system/src/main/resources/mapper/jxc/ |
| 商品菜单 SQL | sql/jxc_menu_product.sql |
| 前端页面/API | frontend/src/views/jxc/product/index.vue、frontend/src/api/jxc/product.js |
