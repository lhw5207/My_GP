package com.ruoyi.jxc.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.jxc.domain.JxcPayNotify;
import com.ruoyi.jxc.mapper.JxcPayNotifyMapper;
import com.ruoyi.jxc.service.IJxcPayService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 扫码收款与到账通知Service业务层处理
 *
 * 配置全部走 sys_config（参数设置界面维护），每次调用实时读取：
 * 改沙箱密钥无需重启后端。沙箱未配置时支付宝通道给出明确指引，微信模拟通道始终可用。
 *
 * @author vivi
 * @date 2026-10-24
 */
@Service
public class JxcPayServiceImpl implements IJxcPayService
{
    private static final String KEY_APP_ID = "jxc.pay.alipay.appId";
    private static final String KEY_GATEWAY = "jxc.pay.alipay.gateway";
    private static final String KEY_PRIVATE_KEY = "jxc.pay.alipay.privateKey";
    private static final String KEY_PUBLIC_KEY = "jxc.pay.alipay.publicKey";
    private static final String KEY_NOTIFY_BASE = "jxc.pay.notify.baseUrl";

    @Autowired
    private JxcPayNotifyMapper jxcPayNotifyMapper;

    @Autowired
    private ISysConfigService configService;

    @Override
    public Map<String, Object> createQrCode(String channel, BigDecimal amount)
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("收款金额必须大于 0");
        }
        String outTradeNo = "PAY" + DateUtils.dateTimeNow("yyyyMMddHHmmss")
                + String.format("%03d", (int) (Math.random() * 1000));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("outTradeNo", outTradeNo);
        result.put("channel", channel);

        if ("ALIPAY".equals(channel))
        {
            String appId = configService.selectConfigByKey(KEY_APP_ID);
            String privateKey = configService.selectConfigByKey(KEY_PRIVATE_KEY);
            String publicKey = configService.selectConfigByKey(KEY_PUBLIC_KEY);
            String gateway = configService.selectConfigByKey(KEY_GATEWAY);
            String notifyBase = configService.selectConfigByKey(KEY_NOTIFY_BASE);
            if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(privateKey) || StringUtils.isEmpty(publicKey))
            {
                throw new ServiceException("支付宝沙箱未配置：请在「系统设置→参数设置」填写 jxc.pay.alipay.appId / privateKey / publicKey（沙箱控制台 + 密钥工具获取），或先用微信模拟通道演示");
            }
            try
            {
                AlipayClient client = new DefaultAlipayClient(
                        StringUtils.isNotEmpty(gateway) ? gateway : "https://openapi.alipaydev.com/gateway.do",
                        appId, privateKey, "json", "UTF-8", publicKey, "RSA2");
                AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
                JSONObject biz = new JSONObject();
                biz.put("out_trade_no", outTradeNo);
                biz.put("total_amount", amount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                biz.put("subject", "商超收银-扫码收款");
                request.setBizContent(biz.toJSONString());
                if (StringUtils.isNotEmpty(notifyBase))
                {
                    request.setNotifyUrl(notifyBase.replaceAll("/+$", "") + "/jxc/pay/notify/alipay");
                }
                AlipayTradePrecreateResponse response = client.execute(request);
                if (!response.isSuccess())
                {
                    throw new ServiceException("支付宝预下单失败：" + StringUtils.nvl(response.getSubMsg(), response.getMsg()));
                }
                result.put("qrCode", response.getQrCode());
                // 回调地址未配置时提前告知（二维码仍可生成，但到账通知收不到）
                result.put("notifyReady", StringUtils.isNotEmpty(notifyBase));
            }
            catch (ServiceException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new ServiceException("调用支付宝沙箱失败：" + e.getMessage());
            }
        }
        else if ("WECHAT".equals(channel))
        {
            // 微信适配层（模拟通道）：占位码由前端渲染；到账由「模拟到账」触发同一广播管线
            result.put("qrCode", "weixin://wxpay/mock/" + outTradeNo);
            result.put("notifyReady", true);
        }
        else
        {
            throw new ServiceException("不支持的收款通道：" + channel);
        }
        return result;
    }

    @Override
    public JxcPayNotify handleAlipayNotify(Map<String, String> params)
    {
        String publicKey = configService.selectConfigByKey(KEY_PUBLIC_KEY);
        try
        {
            if (!AlipaySignature.rsaCheckV1(params, publicKey, "UTF-8", "RSA2"))
            {
                throw new ServiceException("支付宝异步通知验签失败");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("支付宝异步通知验签异常：" + e.getMessage());
        }
        // 应用一致性校验（防串单）
        String appId = configService.selectConfigByKey(KEY_APP_ID);
        if (StringUtils.isNotEmpty(appId) && !appId.equals(params.get("app_id")))
        {
            throw new ServiceException("异步通知 app_id 与本应用不符");
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus))
        {
            // 非到账状态（如 WAIT_BUYER_PAY）：确认收到即可，不落库不广播
            return null;
        }
        JxcPayNotify record = new JxcPayNotify();
        record.setOutTradeNo(params.get("out_trade_no"));
        record.setChannel("ALIPAY");
        record.setTradeNo(params.get("trade_no"));
        record.setAmount(new BigDecimal(params.get("total_amount")));
        record.setBuyerId(params.get("buyer_id"));
        record.setStatus("1");
        record.setCreateBy("alipay");
        record.setCreateTime(DateUtils.getNowDate());
        record.setRemark("支付宝沙箱异步通知");
        return insertIfAbsent(record);
    }

    @Override
    public JxcPayNotify handleMockNotify(String outTradeNo, BigDecimal amount)
    {
        if (StringUtils.isEmpty(outTradeNo) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("模拟到账参数不完整");
        }
        JxcPayNotify record = new JxcPayNotify();
        record.setOutTradeNo(outTradeNo);
        record.setChannel("WECHAT");
        record.setTradeNo("MOCK" + DateUtils.dateTimeNow());
        record.setAmount(amount);
        record.setStatus("1");
        record.setCreateBy("mock");
        record.setCreateTime(DateUtils.getNowDate());
        record.setRemark("微信适配层模拟到账");
        return insertIfAbsent(record);
    }

    /** 幂等落库：out_trade_no 唯一键，重复通知返回 null（上层按已处理确认） */
    private JxcPayNotify insertIfAbsent(JxcPayNotify record)
    {
        try
        {
            jxcPayNotifyMapper.insertJxcPayNotify(record);
            return record;
        }
        catch (DuplicateKeyException e)
        {
            return null;
        }
    }
}
