package com.ruoyi.jxc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.jxc.mapper.JxcMemberMapper;
import com.ruoyi.jxc.domain.JxcMember;
import com.ruoyi.jxc.service.IJxcMemberService;

/**
 * 会员管理Service业务层处理
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcMemberServiceImpl implements IJxcMemberService 
{
    @Autowired
    private JxcMemberMapper jxcMemberMapper;

    /**
     * 查询会员管理
     * 
     * @param id 会员管理主键
     * @return 会员管理
     */
    @Override
    public JxcMember selectJxcMemberById(Long id)
    {
        return jxcMemberMapper.selectJxcMemberById(id);
    }

    /**
     * 查询会员管理列表
     * 
     * @param jxcMember 会员管理
     * @return 会员管理
     */
    @Override
    public List<JxcMember> selectJxcMemberList(JxcMember jxcMember)
    {
        return jxcMemberMapper.selectJxcMemberList(jxcMember);
    }

    /**
     * 新增会员管理
     * 
     * @param jxcMember 会员管理
     * @return 结果
     */
    @Override
    public int insertJxcMember(JxcMember jxcMember)
    {
        jxcMember.setCreateTime(DateUtils.getNowDate());
        return jxcMemberMapper.insertJxcMember(jxcMember);
    }

    /**
     * 修改会员管理
     * 
     * @param jxcMember 会员管理
     * @return 结果
     */
    @Override
    public int updateJxcMember(JxcMember jxcMember)
    {
        jxcMember.setUpdateTime(DateUtils.getNowDate());
        return jxcMemberMapper.updateJxcMember(jxcMember);
    }

    /**
     * 批量删除会员管理
     * 
     * @param ids 需要删除的会员管理主键
     * @return 结果
     */
    @Override
    public int deleteJxcMemberByIds(Long[] ids)
    {
        return jxcMemberMapper.deleteJxcMemberByIds(ids);
    }

    /**
     * 删除会员管理信息
     * 
     * @param id 会员管理主键
     * @return 结果
     */
    @Override
    public int deleteJxcMemberById(Long id)
    {
        return jxcMemberMapper.deleteJxcMemberById(id);
    }
}
