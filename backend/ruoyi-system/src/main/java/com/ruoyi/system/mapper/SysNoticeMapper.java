package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysNotice;

/**
 * 通知公告表 数据层
 * 
 * @author ruoyi
 */
public interface SysNoticeMapper
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
     * 批量删除公告
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
    List<Long> selectReadNoticeIdsByUserId(@Param("userId") Long userId);

    /**
     * 标记公告已读（同一用户重复标记幂等，不产生重复记录）
     *
     * @param noticeId 公告ID
     * @param userId 用户ID
     * @return 结果
     */
    int insertNoticeRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    /**
     * 查询公告已读用户列表（联表用户与部门）
     *
     * @param noticeId 公告ID
     * @param searchValue 搜索值（登录名/昵称/手机号）
     * @return 已读用户集合
     */
    List<Map<String, Object>> selectNoticeReadUsers(@Param("noticeId") Long noticeId,
                                                    @Param("searchValue") String searchValue);
}
