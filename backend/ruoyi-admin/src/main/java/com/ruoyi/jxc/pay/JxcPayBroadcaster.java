package com.ruoyi.jxc.pay;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.alibaba.fastjson2.JSONObject;

/**
 * 支付到账 SSE 广播器
 *
 * 选型说明：到账广播为纯服务端单向推送，采用 Spring MVC 自带 SseEmitter（零新增依赖、
 * 浏览器 EventSource 自带断线重连），避免 WebSocket 的 Vite 代理升级问题。
 * 所有订阅端（收银台/首页）收到同一广播：语音播报「支付宝/微信到账 X 元」。
 *
 * @author vivi
 * @date 2026-10-24
 */
@Component
public class JxcPayBroadcaster
{
    /** 订阅端注册表（key=会话ID；断线/超时/完成自动移除） */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<String, SseEmitter>();

    /**
     * 新增订阅（长连接，不设超时）
     */
    public SseEmitter subscribe()
    {
        SseEmitter emitter = new SseEmitter(0L);
        String id = UUID.randomUUID().toString();
        emitters.put(id, emitter);
        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> emitters.remove(id));
        emitter.onError(e -> emitters.remove(id));
        try
        {
            emitter.send(SseEmitter.event().name("ready").data("{\"ok\":true}"));
        }
        catch (IOException e)
        {
            emitters.remove(id);
        }
        return emitter;
    }

    /**
     * 广播到账事件，返回送达的订阅端数量
     */
    public int broadcast(String channel, Object amount, String outTradeNo, String tradeNo)
    {
        JSONObject msg = new JSONObject();
        msg.put("type", "PAY_RECEIVED");
        msg.put("channel", channel);
        msg.put("amount", amount);
        msg.put("outTradeNo", outTradeNo);
        msg.put("tradeNo", tradeNo);
        int delivered = 0;
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet())
        {
            try
            {
                entry.getValue().send(SseEmitter.event().name("pay").data(msg.toJSONString()));
                delivered++;
            }
            catch (Exception e)
            {
                // 失效连接直接移除
                emitters.remove(entry.getKey());
            }
        }
        return delivered;
    }

    public int onlineCount()
    {
        return emitters.size();
    }
}
