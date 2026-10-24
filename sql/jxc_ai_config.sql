-- =============================================
-- AI 经营日报：参数配置（sys_config，界面「参数设置」可维护，改 Key 无需重打包）
-- 说明：在 ry_jxc 库执行；参数读取走 ISysConfigService.selectConfigByKey
-- 注意：新增键首次访问即缓存；如已缓存后直接改库，需清理 Redis 对应 sys_config 缓存
-- =============================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('AI日报开关', 'jxc.ai.enabled', 'true', 'Y', 'admin', sysdate(), 'AI 经营日报定时任务总开关，true/false'),
('AI日报接口地址', 'jxc.ai.baseUrl', 'https://open.bigmodel.cn/api/paas/v4/chat/completions', 'Y', 'admin', sysdate(), 'OpenAI 兼容 chat/completions 端点（智谱 GLM）'),
('AI日报模型', 'jxc.ai.model', 'glm-4-flash', 'Y', 'admin', sysdate(), '用于经营分析的模型名称，可按账号可用模型调整'),
('AI日报密钥', 'jxc.ai.apiKey', '', 'Y', 'admin', sysdate(), '大模型 API Key；为空时日报自动降级为统计模板生成');

-- 核验
SELECT config_id, config_name, config_key, config_value FROM sys_config WHERE config_key LIKE 'jxc.ai.%';
