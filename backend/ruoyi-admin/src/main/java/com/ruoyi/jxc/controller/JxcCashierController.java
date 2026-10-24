package com.ruoyi.jxc.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.jxc.domain.JxcProduct;
import com.ruoyi.jxc.domain.JxcSaleOrder;
import com.ruoyi.jxc.service.IJxcProductService;
import com.ruoyi.jxc.service.IJxcSaleOrderService;

/**
 * 前台收银Controller（零售快收场景：扫码/搜索加购 + 找零结算）
 * 事务逻辑完全复用销售单「保存即生效」链路（FEFO 拆批 / 原子扣减 / 成本快照 / 应收），
 * 本 Controller 只做商品定位与权限收口，使收银员角色仅需 jxc:cashier:* 最小权限。
 *
 * @author vivi
 * @date 2026-10-24
 */
@RestController
@RequestMapping("/jxc/cashier")
public class JxcCashierController extends BaseController
{
    @Autowired
    private IJxcProductService jxcProductService;

    @Autowired
    private IJxcSaleOrderService jxcSaleOrderService;

    /**
     * 商品定位：barcode 精确匹配优先（扫码枪），否则按名称模糊（收银员手输）
     */
    @PreAuthorize("@ss.hasPermi('jxc:cashier:query')")
    @GetMapping("/product")
    public AjaxResult product(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "barcode", required = false) String barcode)
    {
        JxcProduct query = new JxcProduct();
        if (StringUtils.isNotEmpty(barcode))
        {
            query.setBarcode(barcode.trim());
        }
        else if (StringUtils.isNotEmpty(keyword))
        {
            query.setProductName(keyword.trim());
        }
        else
        {
            return error("请输入条码或商品名称");
        }
        query.setStatus("0");
        List<JxcProduct> list = jxcProductService.selectJxcProductList(query);
        return success(list);
    }

    /**
     * 收银结算（委托销售单保存即生效事务链；单号留空由后端自动生成）
     */
    @PreAuthorize("@ss.hasPermi('jxc:cashier:add')")
    @Log(title = "前台收银", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody JxcSaleOrder jxcSaleOrder)
    {
        int rows = jxcSaleOrderService.insertJxcSaleOrder(jxcSaleOrder);
        return rows > 0 ? AjaxResult.success("收银结算成功", jxcSaleOrder.getSaleNo()) : toAjax(rows);
    }
}
