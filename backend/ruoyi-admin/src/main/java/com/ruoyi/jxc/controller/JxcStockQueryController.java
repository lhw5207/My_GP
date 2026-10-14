package com.ruoyi.jxc.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.jxc.mapper.JxcStockQueryMapper;

/**
 * 库存查询（只读）：现货存量 / 流水台账
 * 说明：库存仅由单据驱动（采购入库/销售出库等），本模块只提供查询追溯，不提供任何写操作。
 *
 * @author vivi
 */
@RestController
@RequestMapping("/jxc")
public class JxcStockQueryController extends BaseController
{
    @Autowired
    private JxcStockQueryMapper jxcStockQueryMapper;

    /**
     * 现货存量查询（联表商品，含临期天数与库存金额）
     *
     * @param productId 商品ID（可空）
     * @param batchNo 批次号（可空，模糊）
     * @param nearExpiryDays 临期天数阈值（可空，如 30 表示仅看 30 天内到期）
     * @param includeZero 是否包含零库存（1 包含）
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock:list')")
    @GetMapping("/stock/list")
    public TableDataInfo stockList(Long productId, String batchNo, Integer nearExpiryDays, String includeZero)
    {
        startPage();
        List<Map<String, Object>> list = jxcStockQueryMapper.selectStockList(productId, batchNo, nearExpiryDays, includeZero);
        return getDataTable(list);
    }

    /**
     * 库存流水台账查询（联表商品）
     *
     * @param productId 商品ID（可空）
     * @param batchNo 批次号（可空，模糊）
     * @param bizType 业务类型（可空，字典 jxc_biz_type）
     * @param bizNo 来源单号（可空，模糊）
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_flow:list')")
    @GetMapping("/stock_flow/list")
    public TableDataInfo stockFlowList(Long productId, String batchNo, String bizType, String bizNo)
    {
        startPage();
        List<Map<String, Object>> list = jxcStockQueryMapper.selectStockFlowList(productId, batchNo, bizType, bizNo);
        return getDataTable(list);
    }
}
