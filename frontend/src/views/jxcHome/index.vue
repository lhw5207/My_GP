<template>
  <div class="app-container jxc-home">
    <!-- 欢迎区 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-inner">
        <div class="welcome-text">
          <h2 class="welcome-title">欢迎回来，{{ nickName }}</h2>
          <p class="welcome-desc">
            商超进销存管理系统 · 覆盖采购 → 库存 → 销售 → 往来 → 报表全链路
          </p>
        </div>
        <div class="welcome-meta">
          <span class="meta-label">今天是</span>
          <span class="meta-value">{{ today }}</span>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口 -->
    <el-row :gutter="16" class="entry-row">
      <el-col v-for="item in entries" :key="item.path" :xs="24" :sm="12" :md="6">
        <el-card class="entry-card" shadow="hover" @click="go(item.path)">
          <div class="entry-head">
            <span class="entry-icon">
              <svg-icon :icon-class="item.icon" />
            </span>
            <span class="entry-title">{{ item.title }}</span>
          </div>
          <p class="entry-desc">{{ item.desc }}</p>
          <div class="entry-foot">
            <el-button type="primary" link @click.stop="go(item.path)">进入</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统要点 -->
    <el-card class="chain-card" shadow="never">
      <template #header>
        <span class="chain-title">核心业务链</span>
      </template>
      <div class="chain-list">
        <el-tag v-for="chain in chains" :key="chain" class="chain-tag" type="info" effect="plain">
          {{ chain }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup name="JxcHome">
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const nickName = computed(() => userStore.nickName || userStore.name || '管理员')
const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const entries = [
  { title: '前台收银', desc: '扫码/搜索加购，自动单号，找零结算即扣库存', icon: 'skill', path: '/sale/cashier' },
  { title: '采购入库', desc: '审核即执行：现存量、库存流水、应付台账同事务联动', icon: 'shopping', path: '/purchase/stock_in' },
  { title: '销售开单', desc: 'FEFO 近效期先出，自动拆批并快照成本', icon: 'money', path: '/sale/sale_order' },
  { title: '库存盘点', desc: '盘盈生成入库单、盘亏生成出库单，差异可追溯', icon: 'table', path: '/stock/stock_check' },
  { title: '往来核销', desc: '一对多核销，余额原子扣减，状态自动流转', icon: 'clipboard', path: '/finance/payment' },
  { title: '销售报表', desc: '销售额、毛利与畅销排行，基于成本快照计算', icon: 'chart', path: '/report/sale_report' },
  { title: '期初导入', desc: 'Excel 模板校验后导入，完成建账数据迁移', icon: 'upload', path: '/stock/init' }
]

const chains = [
  '入库联动',
  '销售 FEFO',
  '退货红冲',
  '盘点调整',
  '往来核销',
  '期初建账',
  '临期预警',
  '流水追溯'
]

function go(path) {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.jxc-home {
  .welcome-card {
    margin-bottom: 16px;
  }

  .welcome-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px 24px;
  }

  .welcome-title {
    margin: 0 0 6px;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    color: var(--el-text-color-primary);
  }

  .welcome-desc {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
  }

  .welcome-meta {
    font-size: 13px;
    color: var(--el-text-color-secondary);

    .meta-label {
      margin-right: 6px;
    }

    .meta-value {
      color: var(--el-text-color-regular);
    }
  }

  .entry-row {
    margin-bottom: 0;
  }

  .entry-card {
    margin-bottom: 16px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }
  }

  .entry-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
  }

  .entry-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 8px;
    font-size: 17px;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    flex: none;
  }

  .entry-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .entry-desc {
    margin: 0;
    min-height: 40px;
    font-size: 13px;
    line-height: 1.55;
    color: var(--el-text-color-secondary);
  }

  .entry-foot {
    margin-top: 6px;
    text-align: right;
  }

  .chain-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .chain-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
