package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.mapper.SysNoticeMapper;
import com.ruoyi.system.service.ISysNoticeService;

/**
 * 公告 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SysNoticeServiceImpl implements ISysNoticeService
{
    @Autowired
    private SysNoticeMapper noticeMapper;

    /**
     * 查询公告信息
     * 
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNotice selectNoticeById(Long noticeId)
    {
        return noticeMapper.selectNoticeById(noticeId);
    }

    /**
     * 查询公告列表
     * 
     * @param notice 公告信息
     * @return 公告集合
     */
    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice)
    {
        return noticeMapper.selectNoticeList(notice);
    }

    /**
     * 新增公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    @Override
    public int insertNotice(SysNotice notice)
    {
        return noticeMapper.insertNotice(notice);
    }

    /**
     * 修改公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    @Override
    public int updateNotice(SysNotice notice)
    {
        return noticeMapper.updateNotice(notice);
    }

    /**
     * 删除公告对象
     * 
     * @param noticeId 公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeById(Long noticeId)
    {
        return noticeMapper.deleteNoticeById(noticeId);
    }

    /**
     * 批量删除公告信息
     * 
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds)
    {
        return noticeMapper.deleteNoticeByIds(noticeIds);
    }

    // ==================== 公告已读（适配 Vue3 前端的消息通知功能） ====================

    /**
     * 查询用户已读的公告ID列表
     */
    @Override
    public List<Long> selectReadNoticeIdsByUserId(Long userId)
    {
        return noticeMapper.selectReadNoticeIdsByUserId(userId);
    }

    /**
     * 标记公告已读（幂等）
     */
    @Override
    public int markNoticeRead(Long noticeId, Long userId)
    {
        return noticeMapper.insertNoticeRead(noticeId, userId);
    }

    /**
     * 批量标记公告已读（ids 逗号分隔字符串）
     */
    @Override
    public int markNoticeReadAll(String ids, Long userId)
    {
        if (ids == null || ids.trim().isEmpty())
        {
            return 0;
        }
        int count = 0;
        for (String id : ids.split(","))
        {
            if (id == null || id.trim().isEmpty())
            {
                continue;
            }
            count += noticeMapper.insertNoticeRead(Long.parseLong(id.trim()), userId);
        }
        return count;
    }

    /**
     * 查询公告已读用户列表
     */
    @Override
    public java.util.List<java.util.Map<String, Object>> selectNoticeReadUsers(Long noticeId, String searchValue)
    {
        return noticeMapper.selectNoticeReadUsers(noticeId, searchValue);
    }
}
