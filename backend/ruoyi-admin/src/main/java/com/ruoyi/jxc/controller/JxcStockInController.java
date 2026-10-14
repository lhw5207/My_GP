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
import com.ruoyi.jxc.domain.JxcStockIn;
import com.ruoyi.jxc.service.IJxcStockInService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 采购入库单Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/stock_in")
public class JxcStockInController extends BaseController
{
    @Autowired
    private IJxcStockInService jxcStockInService;

    /**
     * 查询采购入库单列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcStockIn jxcStockIn)
    {
        startPage();
        List<JxcStockIn> list = jxcStockInService.selectJxcStockInList(jxcStockIn);
        return getDataTable(list);
    }

    /**
     * 导出采购入库单列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:export')")
    @Log(title = "采购入库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcStockIn jxcStockIn)
    {
        List<JxcStockIn> list = jxcStockInService.selectJxcStockInList(jxcStockIn);
        ExcelUtil<JxcStockIn> util = new ExcelUtil<JxcStockIn>(JxcStockIn.class);
        util.exportExcel(response, list, "采购入库单数据");
    }

    /**
     * 获取采购入库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcStockInService.selectJxcStockInById(id));
    }

    /**
     * 新增采购入库单
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:add')")
    @Log(title = "采购入库单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcStockIn jxcStockIn)
    {
        return toAjax(jxcStockInService.insertJxcStockIn(jxcStockIn));
    }

    /**
     * 修改采购入库单
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:edit')")
    @Log(title = "采购入库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcStockIn jxcStockIn)
    {
        return toAjax(jxcStockInService.updateJxcStockIn(jxcStockIn));
    }

    /**
     * 删除采购入库单
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:remove')")
    @Log(title = "采购入库单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcStockInService.deleteJxcStockInByIds(ids));
    }

    /**
     * 审核采购入库单（审核即执行：库存增加 + 流水 + 应付 + 状态更新，同事务）
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_in:audit')")
    @Log(title = "采购入库单", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}")
    public AjaxResult audit(@PathVariable("id") Long id)
    {
        return toAjax(jxcStockInService.auditJxcStockIn(id, getUsername()));
    }
}
