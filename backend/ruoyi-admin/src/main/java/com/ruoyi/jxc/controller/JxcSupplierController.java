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
import com.ruoyi.jxc.domain.JxcSupplier;
import com.ruoyi.jxc.service.IJxcSupplierService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 供应商管理Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/supplier")
public class JxcSupplierController extends BaseController
{
    @Autowired
    private IJxcSupplierService jxcSupplierService;

    /**
     * 查询供应商管理列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:supplier:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcSupplier jxcSupplier)
    {
        startPage();
        List<JxcSupplier> list = jxcSupplierService.selectJxcSupplierList(jxcSupplier);
        return getDataTable(list);
    }

    /**
     * 导出供应商管理列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:supplier:export')")
    @Log(title = "供应商管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcSupplier jxcSupplier)
    {
        List<JxcSupplier> list = jxcSupplierService.selectJxcSupplierList(jxcSupplier);
        ExcelUtil<JxcSupplier> util = new ExcelUtil<JxcSupplier>(JxcSupplier.class);
        util.exportExcel(response, list, "供应商管理数据");
    }

    /**
     * 获取供应商管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:supplier:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcSupplierService.selectJxcSupplierById(id));
    }

    /**
     * 新增供应商管理
     */
    @PreAuthorize("@ss.hasPermi('jxc:supplier:add')")
    @Log(title = "供应商管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcSupplier jxcSupplier)
    {
        return toAjax(jxcSupplierService.insertJxcSupplier(jxcSupplier));
    }

    /**
     * 修改供应商管理
     */
    @PreAuthorize("@ss.hasPermi('jxc:supplier:edit')")
    @Log(title = "供应商管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcSupplier jxcSupplier)
    {
        return toAjax(jxcSupplierService.updateJxcSupplier(jxcSupplier));
    }

    /**
     * 删除供应商管理
     */
    @PreAuthorize("@ss.hasPermi('jxc:supplier:remove')")
    @Log(title = "供应商管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcSupplierService.deleteJxcSupplierByIds(ids));
    }
}
