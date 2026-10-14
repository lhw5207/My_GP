package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 销售单对象 jxc_sale_order
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcSaleOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 销售单ID */
    private Long id;

    /** 销售单号（XS+yyyyMMdd+3位流水） */
    @Excel(name = "销售单号", readConverterExp = "X=S+yyyyMMdd+3位流水")
    private String saleNo;

    /** 会员ID（可空，散客为空；逻辑关联jxc_member.id） */
    @Excel(name = "会员ID", readConverterExp = "可=空，散客为空；逻辑关联jxc_member.id")
    private Long memberId;

    /** 销售日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "销售日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date saleDate;

    /** 商品总额（优惠前） */
    @Excel(name = "商品总额", readConverterExp = "优=惠前")
    private BigDecimal totalAmount;

    /** 优惠金额（会员折扣等） */
    @Excel(name = "优惠金额", readConverterExp = "会=员折扣等")
    private BigDecimal discountAmount;

    /** 应收金额（=total_amount-discount_amount） */
    @Excel(name = "应收金额", readConverterExp = "==total_amount-discount_amount")
    private BigDecimal actualAmount;

    /** 实收金额（现款全额，赊销为0或部分） */
    @Excel(name = "实收金额", readConverterExp = "现=款全额，赊销为0或部分")
    private BigDecimal receivedAmount;

    /** 支付方式（CASH现金 WECHAT微信 ALIPAY支付宝 CARD银行卡 CREDIT挂账，字典jxc_pay_type） */
    @Excel(name = "支付方式", readConverterExp = "C=ASH现金,W=ECHAT微信,A=LIPAY支付宝,C=ARD银行卡,C=REDIT挂账，字典jxc_pay_type")
    private String payType;

    /** 收款状态（0未收 1部分 2已收，字典jxc_writeoff_status） */
    @Excel(name = "收款状态", readConverterExp = "0=未收,1=部分,2=已收，字典jxc_writeoff_status")
    private String payStatus;

    /** 状态（0草稿/挂单 1已审核 2已作废，POS保存即已审核，字典jxc_sale_order_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿/挂单,1=已审核,2=已作废，POS保存即已审核，字典jxc_sale_order_status")
    private String status;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date auditTime;

    /** 销售单明细信息 */
    private List<JxcSaleOrderItem> jxcSaleOrderItemList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setSaleNo(String saleNo) 
    {
        this.saleNo = saleNo;
    }

    public String getSaleNo() 
    {
        return saleNo;
    }

    public void setMemberId(Long memberId) 
    {
        this.memberId = memberId;
    }

    public Long getMemberId() 
    {
        return memberId;
    }

    public void setSaleDate(Date saleDate) 
    {
        this.saleDate = saleDate;
    }

    public Date getSaleDate() 
    {
        return saleDate;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) 
    {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountAmount() 
    {
        return discountAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) 
    {
        this.actualAmount = actualAmount;
    }

    public BigDecimal getActualAmount() 
    {
        return actualAmount;
    }

    public void setReceivedAmount(BigDecimal receivedAmount) 
    {
        this.receivedAmount = receivedAmount;
    }

    public BigDecimal getReceivedAmount() 
    {
        return receivedAmount;
    }

    public void setPayType(String payType) 
    {
        this.payType = payType;
    }

    public String getPayType() 
    {
        return payType;
    }

    public void setPayStatus(String payStatus) 
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus() 
    {
        return payStatus;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setAuditBy(String auditBy) 
    {
        this.auditBy = auditBy;
    }

    public String getAuditBy() 
    {
        return auditBy;
    }

    public void setAuditTime(Date auditTime) 
    {
        this.auditTime = auditTime;
    }

    public Date getAuditTime() 
    {
        return auditTime;
    }

    public List<JxcSaleOrderItem> getJxcSaleOrderItemList()
    {
        return jxcSaleOrderItemList;
    }

    public void setJxcSaleOrderItemList(List<JxcSaleOrderItem> jxcSaleOrderItemList)
    {
        this.jxcSaleOrderItemList = jxcSaleOrderItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("saleNo", getSaleNo())
            .append("memberId", getMemberId())
            .append("saleDate", getSaleDate())
            .append("totalAmount", getTotalAmount())
            .append("discountAmount", getDiscountAmount())
            .append("actualAmount", getActualAmount())
            .append("receivedAmount", getReceivedAmount())
            .append("payType", getPayType())
            .append("payStatus", getPayStatus())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("jxcSaleOrderItemList", getJxcSaleOrderItemList())
            .toString();
    }
}
