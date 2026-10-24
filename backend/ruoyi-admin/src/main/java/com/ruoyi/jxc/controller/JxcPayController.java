package com.ruoyi.jxc.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.jxc.domain.JxcPayNotify;
import com.ruoyi.jxc.pay.JxcPayBroadcaster;
import com.ruoyi.jxc.service.IJxcPayService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 扫码收款与到账广播Controller
 *
 * 链路：收银台生码（挂单）→ 顾客扫码支付 → 支付宝沙箱异步通知 / 微信适配层模拟
 *   → 验签幂等落库 → SSE 广播 → 前端语音播报 + 自动结算。
 * 回调与订阅接口使用基座 @Anonymous 免登录（广播内容仅为到账金额，等同店内播报盒子）。
 *
 * @author vivi
 * @date 2026-10-24
 */
@RestController
@RequestMapping("/jxc/pay")
public class JxcPayController extends BaseController
{
    @Autowired
    private IJxcPayService jxcPayService;

    @Autowired
    private JxcPayBroadcaster payBroadcaster;

    /**
     * 生成收款码（挂单）：ALIPAY 走沙箱 precreate；WECHAT 为适配层模拟通道
     */
    @PreAuthorize("@ss.hasPermi('jxc:cashier:add')")
    @Log(title = "扫码收款", businessType = BusinessType.INSERT)
    @PostMapping("/qrcode")
    public AjaxResult qrcode(@RequestBody Map<String, Object> body)
    {
        String channel = body.get("channel") == null ? "ALIPAY" : String.valueOf(body.get("channel"));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        return success(jxcPayService.createQrCode(channel, amount));
    }

    /**
     * 到账广播订阅（SSE 长连接，免登录）
     */
    @Anonymous
    @GetMapping("/subscribe")
    public SseEmitter subscribe()
    {
        return payBroadcaster.subscribe();
    }

    /**
     * 支付宝异步通知（沙箱回调，免登录；按官方规范返回纯文本 success/failure）
     */
    @Anonymous
    @PostMapping(value = "/notify/alipay", produces = "text/plain;charset=UTF-8")
    public String alipayNotify(HttpServletRequest request)
    {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> raw = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : raw.entrySet())
        {
            params.put(entry.getKey(), entry.getValue() != null && entry.getValue().length > 0 ? entry.getValue()[0] : "");
        }
        try
        {
            JxcPayNotify record = jxcPayService.handleAlipayNotify(params);
            if (record != null)
            {
                payBroadcaster.broadcast(record.getChannel(), record.getAmount(), record.getOutTradeNo(), record.getTradeNo());
            }
            // 验签通过的任何通知（含重复/非到账状态）都确认接收，阻止平台重发
            return "success";
        }
        catch (ServiceException e)
        {
            System.out.println("[jxcPay] 支付宝通知处理失败：" + e.getMessage());
            return "failure";
        }
    }

    /**
     * 微信适配层模拟到账（演示触发器；接正式商户号后替换为真实的 /notify/wechat 回调）
     */
    @PreAuthorize("@ss.hasPermi('jxc:cashier:add')")
    @Log(title = "模拟到账", businessType = BusinessType.INSERT)
    @PostMapping("/notify/mock")
    public AjaxResult mockNotify(@RequestBody Map<String, Object> body)
    {
        String outTradeNo = String.valueOf(body.get("outTradeNo"));
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        JxcPayNotify record = jxcPayService.handleMockNotify(outTradeNo, amount);
        if (record == null)
        {
            return error("该挂单已到账，请勿重复触发");
        }
        int delivered = payBroadcaster.broadcast(record.getChannel(), record.getAmount(), record.getOutTradeNo(), record.getTradeNo());
        return success("已广播到 " + delivered + " 个在线收银端");
    }
}
