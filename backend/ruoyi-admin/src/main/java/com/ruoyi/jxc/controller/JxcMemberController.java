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
import com.ruoyi.jxc.domain.JxcMember;
import com.ruoyi.jxc.service.IJxcMemberService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 会员管理Controller
 * 
 * @author vivi
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/jxc/member")
public class JxcMemberController extends BaseController
{
    @Autowired
    private IJxcMemberService jxcMemberService;

    /**
     * 查询会员管理列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(JxcMember jxcMember)
    {
        startPage();
        List<JxcMember> list = jxcMemberService.selectJxcMemberList(jxcMember);
        return getDataTable(list);
    }

    /**
     * 导出会员管理列表
     */
    @PreAuthorize("@ss.hasPermi('jxc:member:export')")
    @Log(title = "会员管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JxcMember jxcMember)
    {
        List<JxcMember> list = jxcMemberService.selectJxcMemberList(jxcMember);
        ExcelUtil<JxcMember> util = new ExcelUtil<JxcMember>(JxcMember.class);
        util.exportExcel(response, list, "会员管理数据");
    }

    /**
     * 获取会员管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('jxc:member:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jxcMemberService.selectJxcMemberById(id));
    }

    /**
     * 新增会员管理
     */
    @PreAuthorize("@ss.hasPermi('jxc:member:add')")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JxcMember jxcMember)
    {
        return toAjax(jxcMemberService.insertJxcMember(jxcMember));
    }

    /**
     * 修改会员管理
     */
    @PreAuthorize("@ss.hasPermi('jxc:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JxcMember jxcMember)
    {
        return toAjax(jxcMemberService.updateJxcMember(jxcMember));
    }

    /**
     * 删除会员管理
     */
    @PreAuthorize("@ss.hasPermi('jxc:member:remove')")
    @Log(title = "会员管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jxcMemberService.deleteJxcMemberByIds(ids));
    }
}
