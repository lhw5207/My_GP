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
import com.ruoyi.jxc.domain.JxcSaleReturn;
import com.ruoyi.jxc.service.IJxcSaleReturnService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 销售退货Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/sale_return")
public class JxcSaleReturnController extends BaseController
{
    @Autowired
    private IJxcSaleReturnService jxcSaleReturnService;

    /**
     * 查询销售退货列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcSaleReturn jxcSaleReturn)
    {
        startPage();
        List<JxcSaleReturn> list = jxcSaleReturnService.selectJxcSaleReturnList(jxcSaleReturn);
        return getDataTable(list);
    }

    /**
     * 导出销售退货列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:export')")
    @Log(title = "销售退货", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcSaleReturn jxcSaleReturn)
    {
        List<JxcSaleReturn> list = jxcSaleReturnService.selectJxcSaleReturnList(jxcSaleReturn);
        ExcelUtil<JxcSaleReturn> util = new ExcelUtil<JxcSaleReturn>(JxcSaleReturn.class);
        util.exportExcel(response, list, "销售退货数据");
    }

    /**
     * 获取销售退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcSaleReturnService.selectJxcSaleReturnById(id));
    }

    /**
     * 新增销售退货
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:add')")
    @Log(title = "销售退货", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcSaleReturn jxcSaleReturn)
    {
        return toAjax(jxcSaleReturnService.insertJxcSaleReturn(jxcSaleReturn));
    }

    /**
     * 修改销售退货
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:edit')")
    @Log(title = "销售退货", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcSaleReturn jxcSaleReturn)
    {
        return toAjax(jxcSaleReturnService.updateJxcSaleReturn(jxcSaleReturn));
    }

    /**
     * 删除销售退货
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:remove')")
    @Log(title = "销售退货", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcSaleReturnService.deleteJxcSaleReturnByIds(ids));
    }

    /**
     * 审核销售退货单（审核即执行：原批次回增库存 + 流水 + 应收红冲，同事务）
     */
    @PreAuthorize("@ss.hasPermi('jxc:sale_return:audit')")
    @Log(title = "销售退货", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}")
    public AjaxResult audit(@PathVariable("id") Long id)
    {
        return toAjax(jxcSaleReturnService.auditJxcSaleReturn(id, getUsername()));
    }
}
