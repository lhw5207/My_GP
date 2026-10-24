package com.ruoyi.jxc.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

/**
 * 大模型 API 客户端（OpenAI 兼容 chat/completions 协议，智谱 GLM 等）
 *
 * 说明：基座 HttpUtils 不支持自定义 Authorization 请求头，故基于 JDK8 原生
 * HttpsURLConnection 实现本工具；不引入任何新依赖，JSON 序列化由调用方用 fastjson2 完成。
 * 任何网络/状态码异常均抛出 RuntimeException，由调用方负责降级（保证定时任务不中断）。
 *
 * @author vivi
 * @date 2026-10-24
 */
public class JxcAiClient
{
    private static final int CONNECT_TIMEOUT_MS = 5000;

    private static final int READ_TIMEOUT_MS = 60000;

    /**
     * POST JSON（携带 Bearer 鉴权头）
     *
     * @param url      接口地址（https）
     * @param apiKey   API Key（Bearer）
     * @param jsonBody 请求体 JSON 字符串
     * @return 响应体字符串（UTF-8）
     */
    public static String postJson(String url, String apiKey, String jsonBody)
    {
        HttpsURLConnection conn = null;
        try
        {
            conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(body.length));
            try (OutputStream os = conn.getOutputStream())
            {
                os.write(body);
                os.flush();
            }
            int code = conn.getResponseCode();
            String resp = readAll(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
            if (code < 200 || code >= 300)
            {
                throw new RuntimeException("AI 接口返回 HTTP " + code + "：" + abbreviate(resp));
            }
            return resp;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("AI 接口调用失败：" + e.getMessage(), e);
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    private static String readAll(InputStream in) throws Exception
    {
        if (in == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /** 错误信息截断，避免异常消息过长 */
    private static String abbreviate(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
