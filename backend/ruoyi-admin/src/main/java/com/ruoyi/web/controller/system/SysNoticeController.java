package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;

/**
 * 公告 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController
{
    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice)
    {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId)
    {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice)
    {
        notice.setCreateBy(getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice)
    {
        notice.setUpdateBy(getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    /**
     * 首页顶部公告列表（适配 Vue3 前端消息铃铛）
     * 返回：前 3 条未关闭公告 + 当前用户已读状态 + 未读总数
     */
    @GetMapping("/listTop")
    public AjaxResult listTop()
    {
        SysNotice query = new SysNotice();
        query.setStatus("0");
        List<SysNotice> list = noticeService.selectNoticeList(query);
        List<Long> readIds = noticeService.selectReadNoticeIdsByUserId(getUserId());
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        int unreadCount = 0;
        for (SysNotice notice : list)
        {
            boolean isRead = readIds.contains(notice.getNoticeId());
            if (!isRead)
            {
                unreadCount++;
            }
            if (result.size() < 3)
            {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("noticeId", notice.getNoticeId());
                item.put("noticeTitle", notice.getNoticeTitle());
                item.put("noticeType", notice.getNoticeType());
                item.put("createTime", notice.getCreateTime());
                item.put("isRead", isRead);
                result.add(item);
            }
        }
        AjaxResult ajax = AjaxResult.success(result);
        ajax.put("unreadCount", unreadCount);
        return ajax;
    }

    /**
     * 标记公告为已读（幂等）
     */
    @PostMapping("/markRead")
    public AjaxResult markRead(Long noticeId)
    {
        if (noticeId == null)
        {
            return success();
        }
        noticeService.markNoticeRead(noticeId, getUserId());
        return success();
    }

    /**
     * 批量标记公告已读（ids 为逗号分隔字符串）
     */
    @PostMapping("/markReadAll")
    public AjaxResult markReadAll(String ids)
    {
        noticeService.markNoticeReadAll(ids, getUserId());
        return success();
    }

    /**
     * 查询公告已读用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/readUsers/list")
    public TableDataInfo readUsers(Long noticeId, String searchValue)
    {
        startPage();
        List<Map<String, Object>> list = noticeService.selectNoticeReadUsers(noticeId, searchValue);
        return getDataTable(list);
    }
}
