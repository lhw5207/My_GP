package com.ruoyi.jxc.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.jxc.domain.JxcPurchaseOrder;
import com.ruoyi.jxc.service.IJxcPurchaseOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 采购订单Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/purchase_order")
public class JxcPurchaseOrderController extends BaseController
{
    @Autowired
    private IJxcPurchaseOrderService jxcPurchaseOrderService;

    /**
     * 查询采购订单列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_order:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcPurchaseOrder jxcPurchaseOrder)
    {
        startPage();
        List<JxcPurchaseOrder> list = jxcPurchaseOrderService.selectJxcPurchaseOrderList(jxcPurchaseOrder);
        return getDataTable(list);
    }

    /**
     * 导出采购订单列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_order:export')")
    @Log(title = "采购订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcPurchaseOrder jxcPurchaseOrder)
    {
        List<JxcPurchaseOrder> list = jxcPurchaseOrderService.selectJxcPurchaseOrderList(jxcPurchaseOrder);
        ExcelUtil<JxcPurchaseOrder> util = new ExcelUtil<JxcPurchaseOrder>(JxcPurchaseOrder.class);
        util.exportExcel(response, list, "采购订单数据");
    }

    /**
     * 获取采购订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcPurchaseOrderService.selectJxcPurchaseOrderById(id));
    }

    /**
     * 新增采购订单
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_order:add')")
    @Log(title = "采购订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcPurchaseOrder jxcPurchaseOrder)
    {
        return toAjax(jxcPurchaseOrderService.insertJxcPurchaseOrder(jxcPurchaseOrder));
    }

    /**
     * 修改采购订单
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_order:edit')")
    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcPurchaseOrder jxcPurchaseOrder)
    {
        return toAjax(jxcPurchaseOrderService.updateJxcPurchaseOrder(jxcPurchaseOrder));
    }

    /**
     * 删除采购订单
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_order:remove')")
    @Log(title = "采购订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcPurchaseOrderService.deleteJxcPurchaseOrderByIds(ids));
    }
}
