package com.ruoyi.jxc.service;

import java.math.BigDecimal;
import java.util.Map;
import com.ruoyi.jxc.domain.JxcPayNotify;

/**
 * 扫码收款与到账通知Service接口
 *
 * 通道设计（适配器思路）：ALIPAY=支付宝当面付沙箱（真实异步回调 + RSA2 验签）；
 * WECHAT=微信适配层模拟（个人无资质签约正式商户，接入正式商户号只需新增通道实现与配置）
 *
 * @author vivi
 * @date 2026-10-24
 */
public interface IJxcPayService
{
    /**
     * 生成收款挂单（支付宝沙箱 precreate 返回二维码串；微信模拟通道返回本地占位码）
     *
     * @param channel ALIPAY / WECHAT
     * @param amount  收款金额
     * @return outTradeNo / channel / qrCode / notifyReady
     */
    Map<String, Object> createQrCode(String channel, BigDecimal amount);

    /**
     * 处理支付宝异步通知：RSA2 验签 → 交易状态校验 → 幂等落库
     *
     * @param params 通知表单参数
     * @return 到账记录；验签失败抛 ServiceException，非成功状态或重复通知返回 null
     */
    JxcPayNotify handleAlipayNotify(Map<String, String> params);

    /**
     * 微信适配层模拟到账（幂等落库）
     *
     * @param outTradeNo 挂单号
     * @param amount     到账金额
     * @return 到账记录；重复触发抛 ServiceException
     */
    JxcPayNotify handleMockNotify(String outTradeNo, BigDecimal amount);
}
