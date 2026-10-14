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
import com.ruoyi.jxc.domain.JxcProduct;
import com.ruoyi.jxc.service.IJxcProductService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 商品信息Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/product")
public class JxcProductController extends BaseController
{
    @Autowired
    private IJxcProductService jxcProductService;

    /**
     * 查询商品信息列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcProduct jxcProduct)
    {
        startPage();
        List<JxcProduct> list = jxcProductService.selectJxcProductList(jxcProduct);
        return getDataTable(list);
    }

    /**
     * 导出商品信息列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:product:export')")
    @Log(title = "商品信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcProduct jxcProduct)
    {
        List<JxcProduct> list = jxcProductService.selectJxcProductList(jxcProduct);
        ExcelUtil<JxcProduct> util = new ExcelUtil<JxcProduct>(JxcProduct.class);
        util.exportExcel(response, list, "商品信息数据");
    }

    /**
     * 获取商品信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcProductService.selectJxcProductById(id));
    }

    /**
     * 新增商品信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:product:add')")
    @Log(title = "商品信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcProduct jxcProduct)
    {
        return toAjax(jxcProductService.insertJxcProduct(jxcProduct));
    }

    /**
     * 修改商品信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:product:edit')")
    @Log(title = "商品信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcProduct jxcProduct)
    {
        return toAjax(jxcProductService.updateJxcProduct(jxcProduct));
    }

    /**
     * 删除商品信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:product:remove')")
    @Log(title = "商品信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcProductService.deleteJxcProductByIds(ids));
    }
}
