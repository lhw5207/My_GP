package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysNotice;

/**
 * 公告 服务层
 * 
 * @author ruoyi
 */
public interface ISysNoticeService
{
    /**
     * 查询公告信息
     * 
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     * 
     * @param notice 公告信息
     * @return 公告集合
     */
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int insertNotice(SysNotice notice);

    /**
     * 修改公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int updateNotice(SysNotice notice);

    /**
     * 删除公告信息
     * 
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteNoticeById(Long noticeId);
    
    /**
     * 批量删除公告信息
     * 
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    public int deleteNoticeByIds(Long[] noticeIds);

    // ==================== 公告已读（适配 Vue3 前端的消息通知功能） ====================

    /**
     * 查询用户已读的公告ID列表
     *
     * @param userId 用户ID
     * @return 已读公告ID集合
     */
    public java.util.List<Long> selectReadNoticeIdsByUserId(Long userId);

    /**
     * 标记公告已读（幂等）
     *
     * @param noticeId 公告ID
     * @param userId 用户ID
     * @return 结果
     */
    public int markNoticeRead(Long noticeId, Long userId);

    /**
     * 批量标记公告已读（ids 逗号分隔字符串）
     *
     * @param ids 公告ID串（如 "1,2,3"）
     * @param userId 用户ID
     * @return 成功标记数
     */
    public int markNoticeReadAll(String ids, Long userId);

    /**
     * 查询公告已读用户列表
     *
     * @param noticeId 公告ID
     * @param searchValue 搜索值
     * @return 已读用户集合
     */
    public java.util.List<java.util.Map<String, Object>> selectNoticeReadUsers(Long noticeId, String searchValue);
}
