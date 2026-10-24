-- =============================================
-- AI 经营日报：定时任务注册（sys_job）
-- 说明：在 ry_jxc 库执行；初始为「暂停」状态，演示时在 定时任务页面手动「执行一次」
--       （PUT /monitor/job/run，body {"jobId":<id>,"jobGroup":"DEFAULT"}）
-- 注意：RuoYi 新增任务后需在界面点击「恢复」或改 status='0' 才会按 cron 自动调度
-- 幂等性：重复执行会重复插入，仅首次部署执行
-- =============================================

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('AI经营日报', 'DEFAULT', 'jxcTask.aiDailyReport()', '0 30 8 * * ?', '3', '1', '1', 'admin', sysdate(), '近7日销售/库存数据 → 大模型分析 → 站内公告；无 Key 或调用失败自动降级统计模板');

-- 核验
SELECT job_id, job_name, invoke_target, cron_expression, status FROM sys_job WHERE invoke_target LIKE 'jxcTask%';
