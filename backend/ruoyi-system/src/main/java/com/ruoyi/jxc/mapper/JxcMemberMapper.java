package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcMember;

/**
 * 会员管理Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcMemberMapper 
{
    /**
     * 查询会员管理
     * 
     * @param id 会员管理主键
     * @return 会员管理
     */
    public JxcMember selectJxcMemberById(Long id);

    /**
     * 查询会员管理列表
     * 
     * @param jxcMember 会员管理
     * @return 会员管理集合
     */
    public List<JxcMember> selectJxcMemberList(JxcMember jxcMember);

    /**
     * 新增会员管理
     * 
     * @param jxcMember 会员管理
     * @return 结果
     */
    public int insertJxcMember(JxcMember jxcMember);

    /**
     * 修改会员管理
     * 
     * @param jxcMember 会员管理
     * @return 结果
     */
    public int updateJxcMember(JxcMember jxcMember);

    /**
     * 删除会员管理
     * 
     * @param id 会员管理主键
     * @return 结果
     */
    public int deleteJxcMemberById(Long id);

    /**
     * 批量删除会员管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcMemberByIds(Long[] ids);
}
