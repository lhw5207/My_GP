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
import com.ruoyi.jxc.domain.JxcPurchaseReturn;
import com.ruoyi.jxc.service.IJxcPurchaseReturnService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 采购退货Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/purchase_return")
public class JxcPurchaseReturnController extends BaseController
{
    @Autowired
    private IJxcPurchaseReturnService jxcPurchaseReturnService;

    /**
     * 查询采购退货列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcPurchaseReturn jxcPurchaseReturn)
    {
        startPage();
        List<JxcPurchaseReturn> list = jxcPurchaseReturnService.selectJxcPurchaseReturnList(jxcPurchaseReturn);
        return getDataTable(list);
    }

    /**
     * 导出采购退货列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:export')")
    @Log(title = "采购退货", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcPurchaseReturn jxcPurchaseReturn)
    {
        List<JxcPurchaseReturn> list = jxcPurchaseReturnService.selectJxcPurchaseReturnList(jxcPurchaseReturn);
        ExcelUtil<JxcPurchaseReturn> util = new ExcelUtil<JxcPurchaseReturn>(JxcPurchaseReturn.class);
        util.exportExcel(response, list, "采购退货数据");
    }

    /**
     * 获取采购退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcPurchaseReturnService.selectJxcPurchaseReturnById(id));
    }

    /**
     * 新增采购退货
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:add')")
    @Log(title = "采购退货", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcPurchaseReturn jxcPurchaseReturn)
    {
        return toAjax(jxcPurchaseReturnService.insertJxcPurchaseReturn(jxcPurchaseReturn));
    }

    /**
     * 修改采购退货
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:edit')")
    @Log(title = "采购退货", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcPurchaseReturn jxcPurchaseReturn)
    {
        return toAjax(jxcPurchaseReturnService.updateJxcPurchaseReturn(jxcPurchaseReturn));
    }

    /**
     * 删除采购退货
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:remove')")
    @Log(title = "采购退货", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcPurchaseReturnService.deleteJxcPurchaseReturnByIds(ids));
    }

    /**
     * 审核采购退货单（审核即执行：库存扣减 + 流水 + 应付红冲，同事务）
     */
    @PreAuthorize("@ss.hasPermi('jxc:purchase_return:audit')")
    @Log(title = "采购退货", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}")
    public AjaxResult audit(@PathVariable("id") Long id)
    {
        return toAjax(jxcPurchaseReturnService.auditJxcPurchaseReturn(id, getUsername()));
    }
}
