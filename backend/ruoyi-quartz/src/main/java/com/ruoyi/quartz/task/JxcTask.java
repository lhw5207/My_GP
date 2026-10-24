package com.ruoyi.quartz.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.jxc.mapper.JxcReportMapper;
import com.ruoyi.jxc.util.JxcAiClient;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.mapper.SysNoticeMapper;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 进销存业务定时任务
 * 说明：方法均为无返回值、可带参（参数由 sys_job.invoke_target 配置传入，字符串形式）。
 *
 * @author vivi
 */
@Component("jxcTask")
public class JxcTask
{
    /** AI 日报取数窗口：近 7 日（不含今天） */
    private static final int REPORT_WINDOW_DAYS = 7;

    /** AI 日报单列表条数上限（公告长度控制） */
    private static final int LIST_LIMIT = 8;

    @Autowired
    private JxcReportMapper jxcReportMapper;

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    @Autowired
    private ISysConfigService configService;

    /**
     * 库存临期预警（建议 cron：每天 8 点执行）
     * 扫描 days 天内到期的批次（含已过期），生成站内公告（消息铃铛可见）。
     * 防重复：同一天已生成过【临期预警】公告则跳过；无临期批次时不打扰。
     *
     * @param days 临期天数阈值（字符串，默认 30）
     */
    public void nearExpiryWarn(String days)
    {
        int threshold = 30;
        if (StringUtils.isNotEmpty(days))
        {
            try
            {
                threshold = Integer.parseInt(days.trim());
            }
            catch (NumberFormatException e)
            {
                // 参数非法时使用默认阈值
            }
        }
        // 防重复：今天已生成则跳过
        if (jxcReportMapper.countTodayNoticeByTitlePrefix("【临期预警】") > 0)
        {
            System.out.println("[jxcTask] 今日临期预警公告已存在，跳过生成");
            return;
        }
        List<Map<String, Object>> list = jxcReportMapper.selectNearExpiry(threshold);
        if (list == null || list.isEmpty())
        {
            System.out.println("[jxcTask] 未发现 " + threshold + " 天内临期批次，无需预警");
            return;
        }
        StringBuilder content = new StringBuilder();
        content.append("系统扫描到 ").append(list.size()).append(" 个批次将在 ").append(threshold)
               .append(" 天内到期（含已过期），请及时处理（促销/退换）：\n");
        for (Map<String, Object> row : list)
        {
            Object daysToExpiry = row.get("daysToExpiry");
            long d = daysToExpiry == null ? 0 : Long.parseLong(String.valueOf(daysToExpiry));
            content.append("· ").append(row.get("productName"))
                   .append("（").append(row.get("productCode")).append("）")
                   .append(" 批次 ").append(row.get("batchNo").toString().isEmpty() ? "非批次" : row.get("batchNo"))
                   .append("，到期日 ").append(row.get("expiryDate"))
                   .append("，").append(d < 0 ? "已过期 " + Math.abs(d) + " 天" : "剩余 " + d + " 天")
                   .append("，库存 ").append(row.get("quantity")).append(" ").append(row.get("unit"))
                   .append("\n");
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("【临期预警】" + today + " 发现 " + list.size() + " 个临期批次");
        notice.setNoticeType("1");
        notice.setNoticeContent(content.toString());
        notice.setStatus("0");
        notice.setCreateBy("system");
        sysNoticeMapper.insertNotice(notice);
        System.out.println("[jxcTask] 临期预警公告已生成，共 " + list.size() + " 个批次");
    }

    /**
     * AI 经营日报（建议 cron：每天 8:30，在临期预警之后）
     * 取数（近 7 日销售含毛利/畅销排行/低库存/临期批次，全部复用报表 Mapper）→
     * 已配置 API Key 时调用大模型（OpenAI 兼容协议，智谱 GLM）生成分析；
     * 未配置 Key 或调用失败时降级为统计模板，保证任务可用 →
     * 生成站内公告【AI日报】（消息铃铛可见）。
     * 防重复：同一天已生成过【AI日报】公告则跳过；方法内部吞掉异常，保证调度永不中断。
     */
    public void aiDailyReport()
    {
        try
        {
            // 防重复：今天已生成则跳过
            if (jxcReportMapper.countTodayNoticeByTitlePrefix("【AI日报】") > 0)
            {
                System.out.println("[jxcTask] 今日 AI 日报已存在，跳过生成");
                return;
            }

            // ① 取数（近 7 日，不含今天；报表口径与销售统计页一致）
            LocalDate today = LocalDate.now();
            String yesterday = today.minusDays(1).toString();
            String beginDate = today.minusDays(REPORT_WINDOW_DAYS).toString();
            List<Map<String, Object>> daily = jxcReportMapper.selectSaleDaily(beginDate, yesterday);
            List<Map<String, Object>> topProducts = jxcReportMapper.selectSaleByProduct(beginDate, yesterday);
            List<Map<String, Object>> lowStock = jxcReportMapper.selectLowStock();
            List<Map<String, Object>> nearExpiry = jxcReportMapper.selectNearExpiry(30);

            // ② 生成内容：AI 优先，异常降级模板
            String content;
            String source;
            String enabled = configService.selectConfigByKey("jxc.ai.enabled");
            String apiKey = configService.selectConfigByKey("jxc.ai.apiKey");
            if (isAiEnabled(enabled) && StringUtils.isNotEmpty(apiKey))
            {
                String baseUrl = configService.selectConfigByKey("jxc.ai.baseUrl");
                String model = configService.selectConfigByKey("jxc.ai.model");
                try
                {
                    content = callLlm(baseUrl, apiKey, model, daily, topProducts, lowStock, nearExpiry, yesterday);
                    source = "AI（" + StringUtils.nvl(model, "glm-4-flash") + "）生成";
                }
                catch (Exception e)
                {
                    System.out.println("[jxcTask] AI 分析失败，自动降级为统计模板：" + e.getMessage());
                    content = buildTemplateHtml(daily, topProducts, lowStock, nearExpiry, yesterday);
                    source = "统计模板生成（AI 不可用自动降级）";
                }
            }
            else
            {
                content = buildTemplateHtml(daily, topProducts, lowStock, nearExpiry, yesterday);
                source = "统计模板生成（未配置 AI Key，参数设置中填入 jxc.ai.apiKey 即启用）";
            }

            // ③ 生成公告（幂等由①保证）
            SysNotice notice = new SysNotice();
            notice.setNoticeTitle("【AI日报】" + today + " 经营分析");
            notice.setNoticeType("1");
            notice.setNoticeContent(content + "<p style='color:#909399;font-size:12px;'>—— " + source + "</p>");
            notice.setStatus("0");
            notice.setCreateBy("system");
            sysNoticeMapper.insertNotice(notice);
            System.out.println("[jxcTask] AI 日报公告已生成（" + source + "）");
        }
        catch (Exception e)
        {
            // 定时任务兜底：任何异常只记日志，不向调度抛出
            System.out.println("[jxcTask] AI 日报生成异常（不影响调度）：" + e.getMessage());
        }
    }

    /** 开关判定：参数缺失时默认开启（Key 为空仍会走模板分支） */
    private boolean isAiEnabled(String enabled)
    {
        return StringUtils.isEmpty(enabled) || "true".equalsIgnoreCase(enabled.trim());
    }

    /**
     * 调用大模型（OpenAI 兼容 chat/completions）：角色=商超经营分析师，输出简短 HTML
     */
    private String callLlm(String baseUrl, String apiKey, String model,
            List<Map<String, Object>> daily, List<Map<String, Object>> topProducts,
            List<Map<String, Object>> lowStock, List<Map<String, Object>> nearExpiry, String yesterday)
    {
        String url = StringUtils.isNotEmpty(baseUrl) ? baseUrl : "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        String useModel = StringUtils.isNotEmpty(model) ? model : "glm-4-flash";
        String systemPrompt = "你是一名中小型商超的经营分析师，根据销售与库存数据给出简明、可执行的结论。"
                + "只输出简体中文 HTML 片段，仅允许使用 h4/p/ul/li/b 标签，禁止 markdown 与代码块，总字数不超过 350 字。";
        String userPrompt = buildStatsText(daily, topProducts, lowStock, nearExpiry, yesterday)
                + "\n请按以下结构输出分析：经营概要、毛利分析、畅销与滞销、库存风险、今日建议。";

        JSONObject body = new JSONObject();
        body.put("model", useModel);
        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);
        JSONObject usr = new JSONObject();
        usr.put("role", "user");
        usr.put("content", userPrompt);
        messages.add(usr);
        body.put("messages", messages);
        body.put("temperature", 0.4);

        String resp = JxcAiClient.postJson(url, apiKey, body.toJSONString());
        JSONObject choice = JSONObject.parseObject(resp).getJSONArray("choices").getJSONObject(0);
        return choice.getJSONObject("message").getString("content");
    }

    /**
     * 统计数据文本（喂给大模型的紧凑版）
     */
    private String buildStatsText(List<Map<String, Object>> daily, List<Map<String, Object>> topProducts,
            List<Map<String, Object>> lowStock, List<Map<String, Object>> nearExpiry, String yesterday)
    {
        StringBuilder sb = new StringBuilder();
        BigDecimal weekAmount = BigDecimal.ZERO, weekCost = BigDecimal.ZERO, weekProfit = BigDecimal.ZERO;
        long weekCount = 0;
        String ydLine = "昨日无销售";
        for (Map<String, Object> row : daily)
        {
            long c = toLong(row.get("saleCount"));
            BigDecimal amt = toDecimal(row.get("saleAmount"));
            weekCount += c;
            weekAmount = weekAmount.add(amt);
            weekCost = weekCost.add(toDecimal(row.get("costAmount")));
            weekProfit = weekProfit.add(toDecimal(row.get("grossProfit")));
            if (yesterday.equals(String.valueOf(row.get("saleDate"))))
            {
                ydLine = "昨日销售 " + c + " 单，销售额 " + strip(amt) + " 元，毛利 "
                        + strip(toDecimal(row.get("grossProfit"))) + " 元";
            }
        }
        sb.append("【近7日】").append(weekCount).append(" 单，销售额 ").append(strip(weekAmount))
          .append(" 元，成本 ").append(strip(weekCost)).append(" 元，毛利 ").append(strip(weekProfit)).append(" 元。\n");
        sb.append("【昨日】").append(ydLine).append("。\n");

        sb.append("【畅销TOP5】");
        if (topProducts == null || topProducts.isEmpty())
        {
            sb.append("近7日无销售。\n");
        }
        else
        {
            for (int i = 0; i < topProducts.size() && i < 5; i++)
            {
                Map<String, Object> p = topProducts.get(i);
                sb.append(p.get("productName")).append("（销量 ").append(strip(toDecimal(p.get("saleQty"))))
                  .append(" ").append(StringUtils.nvl(p.get("unit"), "")).append("，销售额 ")
                  .append(strip(toDecimal(p.get("saleAmount")))).append(" 元）");
                sb.append(i < Math.min(topProducts.size(), 5) - 1 ? "；" : "。\n");
            }
        }

        sb.append("【低库存】");
        if (lowStock == null || lowStock.isEmpty())
        {
            sb.append("无。\n");
        }
        else
        {
            sb.append(lowStock.size()).append(" 项：");
            for (int i = 0; i < lowStock.size() && i < LIST_LIMIT; i++)
            {
                Map<String, Object> p = lowStock.get(i);
                sb.append(p.get("productName")).append("(").append(p.get("warnType")).append("，库存 ")
                  .append(strip(toDecimal(p.get("stockQty")))).append("/下限 ")
                  .append(strip(toDecimal(p.get("stockMin")))).append(")");
                sb.append(i < Math.min(lowStock.size(), LIST_LIMIT) - 1 ? "；" : "。\n");
            }
        }

        sb.append("【临期30天】");
        if (nearExpiry == null || nearExpiry.isEmpty())
        {
            sb.append("无。");
        }
        else
        {
            sb.append(nearExpiry.size()).append(" 项：");
            for (int i = 0; i < nearExpiry.size() && i < LIST_LIMIT; i++)
            {
                Map<String, Object> p = nearExpiry.get(i);
                sb.append(p.get("productName")).append("(到期 ").append(p.get("expiryDate")).append("，剩余 ")
                  .append(toLong(p.get("daysToExpiry"))).append(" 天，库存 ")
                  .append(strip(toDecimal(p.get("quantity")))).append(")");
                sb.append(i < Math.min(nearExpiry.size(), LIST_LIMIT) - 1 ? "；" : "。");
            }
        }
        return sb.toString();
    }

    /**
     * 统计模板 HTML（无 AI / AI 失败时的降级产物，与公告富文本渲染兼容）
     */
    private String buildTemplateHtml(List<Map<String, Object>> daily, List<Map<String, Object>> topProducts,
            List<Map<String, Object>> lowStock, List<Map<String, Object>> nearExpiry, String yesterday)
    {
        BigDecimal weekAmount = BigDecimal.ZERO, weekCost = BigDecimal.ZERO, weekProfit = BigDecimal.ZERO;
        long weekCount = 0;
        String ydLine = "昨日无销售";
        for (Map<String, Object> row : daily)
        {
            long c = toLong(row.get("saleCount"));
            BigDecimal amt = toDecimal(row.get("saleAmount"));
            weekCount += c;
            weekAmount = weekAmount.add(amt);
            weekCost = weekCost.add(toDecimal(row.get("costAmount")));
            weekProfit = weekProfit.add(toDecimal(row.get("grossProfit")));
            if (yesterday.equals(String.valueOf(row.get("saleDate"))))
            {
                ydLine = "昨日销售 <b>" + c + "</b> 单，销售额 <b>" + strip(amt) + "</b> 元，毛利 <b>"
                        + strip(toDecimal(row.get("grossProfit"))) + "</b> 元";
            }
        }
        String rate = weekAmount.compareTo(BigDecimal.ZERO) > 0
                ? weekProfit.multiply(new BigDecimal("100")).divide(weekAmount, 1, RoundingMode.HALF_UP) + "%" : "—";

        StringBuilder html = new StringBuilder();
        html.append("<h4>📊 经营概要（近7日）</h4>");
        html.append("<p>共销售 <b>").append(weekCount).append("</b> 单，销售额 <b>").append(strip(weekAmount))
            .append("</b> 元，成本 ").append(strip(weekCost)).append(" 元，毛利 <b>").append(strip(weekProfit))
            .append("</b> 元（毛利率 ").append(rate).append("）。</p>");
        html.append("<p>").append(ydLine).append("。</p>");

        html.append("<h4>🏆 畅销排行 TOP5</h4>");
        if (topProducts == null || topProducts.isEmpty())
        {
            html.append("<p>近 7 日无销售数据。</p>");
        }
        else
        {
            html.append("<ul>");
            for (int i = 0; i < topProducts.size() && i < 5; i++)
            {
                Map<String, Object> p = topProducts.get(i);
                html.append("<li>").append(p.get("productName")).append("：销量 ").append(strip(toDecimal(p.get("saleQty"))))
                    .append(" ").append(StringUtils.nvl(p.get("unit"), "")).append("，销售额 ")
                    .append(strip(toDecimal(p.get("saleAmount")))).append(" 元，毛利 ")
                    .append(strip(toDecimal(p.get("grossProfit")))).append(" 元</li>");
            }
            html.append("</ul>");
        }

        html.append("<h4>⚠️ 库存风险</h4>");
        if ((lowStock == null || lowStock.isEmpty()) && (nearExpiry == null || nearExpiry.isEmpty()))
        {
            html.append("<p>低库存与临期批次均无风险项。</p>");
        }
        else
        {
            html.append("<ul>");
            if (lowStock != null && !lowStock.isEmpty())
            {
                List<String> items = new ArrayList<String>();
                for (int i = 0; i < lowStock.size() && i < LIST_LIMIT; i++)
                {
                    Map<String, Object> p = lowStock.get(i);
                    items.add(p.get("productName") + "（" + p.get("warnType") + "，库存 "
                            + strip(toDecimal(p.get("stockQty"))) + " / 下限 " + strip(toDecimal(p.get("stockMin"))) + "）");
                }
                html.append("<li>低库存 <b>").append(lowStock.size()).append("</b> 项：")
                    .append(String.join("；", items)).append("</li>");
            }
            if (nearExpiry != null && !nearExpiry.isEmpty())
            {
                List<String> items = new ArrayList<String>();
                for (int i = 0; i < nearExpiry.size() && i < LIST_LIMIT; i++)
                {
                    Map<String, Object> p = nearExpiry.get(i);
                    items.add(p.get("productName") + "（到期 " + p.get("expiryDate") + "，剩余 "
                            + toLong(p.get("daysToExpiry")) + " 天，库存 " + strip(toDecimal(p.get("quantity"))) + "）");
                }
                html.append("<li>临期 30 天内 <b>").append(nearExpiry.size()).append("</b> 项：")
                    .append(String.join("；", items)).append("</li>");
            }
            html.append("</ul>");
        }

        html.append("<h4>📋 今日建议（模板）</h4>");
        html.append("<ul>");
        html.append("<li>优先促销临期批次（FEFO 出库已自动优先近效期，必要时降价加速周转）。</li>");
        if (lowStock != null && !lowStock.isEmpty())
        {
            html.append("<li>对 ").append(lowStock.size()).append(" 项低库存/缺货商品尽快补货。</li>");
        }
        if (topProducts != null && !topProducts.isEmpty())
        {
            html.append("<li>保障畅销品「").append(topProducts.get(0).get("productName")).append("」货源与陈列。</li>");
        }
        html.append("<li>配置 AI Key 后可自动升级为大模型分析结论。</li>");
        html.append("</ul>");
        return html.toString();
    }

    /** Map 取数值：兼容 BigDecimal/Long/Integer */
    private BigDecimal toDecimal(Object v)
    {
        if (v == null)
        {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal)
        {
            return (BigDecimal) v;
        }
        return new BigDecimal(String.valueOf(v));
    }

    private long toLong(Object v)
    {
        if (v == null)
        {
            return 0L;
        }
        if (v instanceof Number)
        {
            return ((Number) v).longValue();
        }
        return Long.parseLong(String.valueOf(v));
    }

    /** 数值展示：去掉多余小数位 */
    private String strip(BigDecimal v)
    {
        if (v == null)
        {
            return "0";
        }
        BigDecimal n = v.stripTrailingZeros();
        return (n.scale() < 0 ? n.setScale(0, RoundingMode.HALF_UP) : n).toPlainString();
    }
}
