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
import com.ruoyi.jxc.domain.JxcStockCheck;
import com.ruoyi.jxc.service.IJxcStockCheckService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 库存盘点Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/stock_check")
public class JxcStockCheckController extends BaseController
{
    @Autowired
    private IJxcStockCheckService jxcStockCheckService;

    /**
     * 查询库存盘点列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcStockCheck jxcStockCheck)
    {
        startPage();
        List<JxcStockCheck> list = jxcStockCheckService.selectJxcStockCheckList(jxcStockCheck);
        return getDataTable(list);
    }

    /**
     * 导出库存盘点列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:export')")
    @Log(title = "库存盘点", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcStockCheck jxcStockCheck)
    {
        List<JxcStockCheck> list = jxcStockCheckService.selectJxcStockCheckList(jxcStockCheck);
        ExcelUtil<JxcStockCheck> util = new ExcelUtil<JxcStockCheck>(JxcStockCheck.class);
        util.exportExcel(response, list, "库存盘点数据");
    }

    /**
     * 获取库存盘点详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcStockCheckService.selectJxcStockCheckById(id));
    }

    /**
     * 新增库存盘点
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:add')")
    @Log(title = "库存盘点", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcStockCheck jxcStockCheck)
    {
        return toAjax(jxcStockCheckService.insertJxcStockCheck(jxcStockCheck));
    }

    /**
     * 修改库存盘点
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:edit')")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcStockCheck jxcStockCheck)
    {
        return toAjax(jxcStockCheckService.updateJxcStockCheck(jxcStockCheck));
    }

    /**
     * 删除库存盘点
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:remove')")
    @Log(title = "库存盘点", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcStockCheckService.deleteJxcStockCheckByIds(ids));
    }

    /**
     * 审核盘点单（审核即执行：盘盈生成入库单 / 盘亏生成出库单，联动库存+流水+回写调整单号）
     */
    @PreAuthorize("@ss.hasPermi('jxc:stock_check:audit')")
    @Log(title = "库存盘点", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}")
    public AjaxResult audit(@PathVariable("id") Long id)
    {
        return toAjax(jxcStockCheckService.auditJxcStockCheck(id, getUsername()));
    }
}
