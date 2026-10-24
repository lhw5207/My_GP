-- =============================================
-- 扫码收款与到账广播：到账流水表 + 沙箱配置参数
-- 说明：在 ry_jxc 库执行；幂等（表 IF NOT EXISTS / 参数仅首次插入）
-- 链路：支付宝沙箱异步通知 / 微信适配层模拟 → 验签幂等 → jxc_pay_notify 落库 → SSE 广播 → 收银台语音播报+自动结算
-- =============================================

-- 1. 到账流水表（回调幂等依据：out_trade_no 唯一键）
CREATE TABLE IF NOT EXISTS jxc_pay_notify (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '支付到账ID',
  out_trade_no  VARCHAR(64)   NOT NULL                COMMENT '商户挂单号（本地生成，PAY+时间戳+随机）',
  channel       VARCHAR(16)   NOT NULL                COMMENT '到账通道（ALIPAY 支付宝沙箱 / WECHAT 微信适配层模拟）',
  trade_no      VARCHAR(64)   DEFAULT NULL            COMMENT '平台交易号（沙箱返回；模拟通道为 MOCK 前缀）',
  amount        DECIMAL(18,2) NOT NULL                COMMENT '到账金额',
  buyer_id      VARCHAR(64)   DEFAULT NULL            COMMENT '付款方账号（沙箱买家）',
  status        CHAR(1)       DEFAULT '1'             COMMENT '状态（1 到账）',
  create_by     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time   DATETIME      DEFAULT NULL            COMMENT '创建时间',
  update_by     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time   DATETIME      DEFAULT NULL            COMMENT '更新时间',
  remark        VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_out_trade_no (out_trade_no),
  KEY idx_channel_time (channel, create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付到账流水表';

-- 2. 沙箱配置（参数设置界面维护，改配置无需重打包；个人无资质签约正式商户，沙箱联调 + 微信适配层模拟）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('支付-沙箱应用ID', 'jxc.pay.alipay.appId', '', 'Y', 'admin', sysdate(), '支付宝开放平台沙箱应用 APPID（open.alipay.com 控制台-沙箱）'),
('支付-沙箱网关', 'jxc.pay.alipay.gateway', 'https://openapi.alipaydev.com/gateway.do', 'Y', 'admin', sysdate(), '支付宝沙箱网关地址，正式环境换 openapi.alipay.com'),
('支付-应用私钥', 'jxc.pay.alipay.privateKey', '', 'Y', 'admin', sysdate(), '沙箱应用私钥（PKCS8，密钥工具生成），用于请求签名'),
('支付-支付宝公钥', 'jxc.pay.alipay.publicKey', '', 'Y', 'admin', sysdate(), '支付宝公钥（非应用公钥），用于异步通知验签'),
('支付-回调外网地址', 'jxc.pay.notify.baseUrl', '', 'Y', 'admin', sysdate(), '内网穿透公网地址（如 https://xxx.cpolar.io），支付宝异步通知 POST 到 {该地址}/jxc/pay/notify/alipay');

-- 3. 核验
SHOW COLUMNS FROM jxc_pay_notify;
SELECT config_key, config_value FROM sys_config WHERE config_key LIKE 'jxc.pay.%';
