package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 会员管理对象 jxc_member
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会员ID */
    private Long id;

    /** 会员号（唯一） */
    @Excel(name = "会员号", readConverterExp = "唯=一")
    private String memberNo;

    /** 会员姓名 */
    @Excel(name = "会员姓名")
    private String memberName;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 会员等级（字典jxc_member_level） */
    @Excel(name = "会员等级", readConverterExp = "字=典jxc_member_level")
    private String level;

    /** 等级折扣（百分比，如95.00表示95折） */
    @Excel(name = "等级折扣", readConverterExp = "百=分比，如95.00表示95折")
    private BigDecimal discount;

    /** 积分（简化策略） */
    @Excel(name = "积分", readConverterExp = "简=化策略")
    private BigDecimal points;

    /** 储值余额（若不涉储值保持为0） */
    @Excel(name = "储值余额", readConverterExp = "若=不涉储值保持为0")
    private BigDecimal balance;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setMemberNo(String memberNo) 
    {
        this.memberNo = memberNo;
    }

    public String getMemberNo() 
    {
        return memberNo;
    }

    public void setMemberName(String memberName) 
    {
        this.memberName = memberName;
    }

    public String getMemberName() 
    {
        return memberName;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setLevel(String level) 
    {
        this.level = level;
    }

    public String getLevel() 
    {
        return level;
    }

    public void setDiscount(BigDecimal discount) 
    {
        this.discount = discount;
    }

    public BigDecimal getDiscount() 
    {
        return discount;
    }

    public void setPoints(BigDecimal points) 
    {
        this.points = points;
    }

    public BigDecimal getPoints() 
    {
        return points;
    }

    public void setBalance(BigDecimal balance) 
    {
        this.balance = balance;
    }

    public BigDecimal getBalance() 
    {
        return balance;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("memberNo", getMemberNo())
            .append("memberName", getMemberName())
            .append("phone", getPhone())
            .append("level", getLevel())
            .append("discount", getDiscount())
            .append("points", getPoints())
            .append("balance", getBalance())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
