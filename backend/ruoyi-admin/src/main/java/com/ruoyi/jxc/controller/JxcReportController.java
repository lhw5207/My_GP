package com.ruoyi.jxc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.jxc.mapper.JxcReportMapper;

/**
 * 统计报表（只读）：销售日报（含毛利）/ 商品销售排行 / 低库存预警 / 临期预警
 *
 * @author vivi
 */
@RestController
@RequestMapping("/jxc/report")
public class JxcReportController extends BaseController
{
    @Autowired
    private JxcReportMapper jxcReportMapper;

    /** 销售日报（含毛利；毛利 = 销售额 - 成本快照） */
    @PreAuthorize("@ss.hasPermi('jxc:report:sale')")
    @GetMapping("/sale/daily")
    public TableDataInfo saleDaily(String beginDate, String endDate)
    {
        startPage();
        List<Map<String, Object>> list = jxcReportMapper.selectSaleDaily(beginDate, endDate);
        return getDataTable(list);
    }

    /** 商品销售排行（畅销榜） */
    @PreAuthorize("@ss.hasPermi('jxc:report:sale')")
    @GetMapping("/sale/by-product")
    public TableDataInfo saleByProduct(String beginDate, String endDate)
    {
        startPage();
        List<Map<String, Object>> list = jxcReportMapper.selectSaleByProduct(beginDate, endDate);
        return getDataTable(list);
    }

    /** 销售汇总（合计卡片：销售额/成本/毛利） */
    @PreAuthorize("@ss.hasPermi('jxc:report:sale')")
    @GetMapping("/sale/summary")
    public AjaxResult saleSummary(String beginDate, String endDate)
    {
        List<Map<String, Object>> daily = jxcReportMapper.selectSaleDaily(beginDate, endDate);
        java.math.BigDecimal saleAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal costAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal grossProfit = java.math.BigDecimal.ZERO;
        long saleCount = 0L;
        for (Map<String, Object> row : daily)
        {
            saleAmount = saleAmount.add(toDecimal(row.get("saleAmount")));
            costAmount = costAmount.add(toDecimal(row.get("costAmount")));
            grossProfit = grossProfit.add(toDecimal(row.get("grossProfit")));
            saleCount += toLong(row.get("saleCount"));
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("saleAmount", saleAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        result.put("costAmount", costAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        result.put("grossProfit", grossProfit.setScale(2, java.math.RoundingMode.HALF_UP));
        result.put("saleCount", saleCount);
        return success(result);
    }

    /** 低库存预警（按商品聚合） */
    @PreAuthorize("@ss.hasPermi('jxc:report:stock')")
    @GetMapping("/stock/low")
    public TableDataInfo lowStock()
    {
        startPage();
        List<Map<String, Object>> list = jxcReportMapper.selectLowStock();
        return getDataTable(list);
    }

    /** 临期预警（批次维度，days 默认 30） */
    @PreAuthorize("@ss.hasPermi('jxc:report:stock')")
    @GetMapping("/stock/near-expiry")
    public TableDataInfo nearExpiry(Integer days)
    {
        if (days == null)
        {
            days = 30;
        }
        startPage();
        List<Map<String, Object>> list = jxcReportMapper.selectNearExpiry(days);
        return getDataTable(list);
    }

    private java.math.BigDecimal toDecimal(Object v)
    {
        if (v == null)
        {
            return java.math.BigDecimal.ZERO;
        }
        return new java.math.BigDecimal(String.valueOf(v));
    }

    private long toLong(Object v)
    {
        if (v == null)
        {
            return 0L;
        }
        return Long.parseLong(String.valueOf(v));
    }
}
