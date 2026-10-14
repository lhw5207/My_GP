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
 * 销售退货对象 jxc_sale_return
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcSaleReturn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 销售退货单ID */
    private Long id;

    /** 退货单号（SR+yyyyMMdd+3位流水） */
    @Excel(name = "退货单号", readConverterExp = "S=R+yyyyMMdd+3位流水")
    private String returnNo;

    /** 原销售单号（可空） */
    @Excel(name = "原销售单号", readConverterExp = "可=空")
    private String sourceSaleNo;

    /** 会员ID（可空，逻辑关联jxc_member.id） */
    @Excel(name = "会员ID", readConverterExp = "可=空，逻辑关联jxc_member.id")
    private Long memberId;

    /** 退货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退货日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date returnDate;

    /** 退货总金额 */
    @Excel(name = "退货总金额")
    private BigDecimal totalAmount;

    /** 退款金额（现退部分；赊销则冲应收） */
    @Excel(name = "退款金额", readConverterExp = "现=退部分；赊销则冲应收")
    private BigDecimal refundAmount;

    /** 状态（0草稿 1已审核 2已作废，仅草稿可作废，字典jxc_bill_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已审核,2=已作废，仅草稿可作废，字典jxc_bill_status")
    private String status;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date auditTime;

    /** 销售退货明细信息 */
    private List<JxcSaleReturnItem> jxcSaleReturnItemList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setReturnNo(String returnNo) 
    {
        this.returnNo = returnNo;
    }

    public String getReturnNo() 
    {
        return returnNo;
    }

    public void setSourceSaleNo(String sourceSaleNo) 
    {
        this.sourceSaleNo = sourceSaleNo;
    }

    public String getSourceSaleNo() 
    {
        return sourceSaleNo;
    }

    public void setMemberId(Long memberId) 
    {
        this.memberId = memberId;
    }

    public Long getMemberId() 
    {
        return memberId;
    }

    public void setReturnDate(Date returnDate) 
    {
        this.returnDate = returnDate;
    }

    public Date getReturnDate() 
    {
        return returnDate;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) 
    {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getRefundAmount() 
    {
        return refundAmount;
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

    public List<JxcSaleReturnItem> getJxcSaleReturnItemList()
    {
        return jxcSaleReturnItemList;
    }

    public void setJxcSaleReturnItemList(List<JxcSaleReturnItem> jxcSaleReturnItemList)
    {
        this.jxcSaleReturnItemList = jxcSaleReturnItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("returnNo", getReturnNo())
            .append("sourceSaleNo", getSourceSaleNo())
            .append("memberId", getMemberId())
            .append("returnDate", getReturnDate())
            .append("totalAmount", getTotalAmount())
            .append("refundAmount", getRefundAmount())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("jxcSaleReturnItemList", getJxcSaleReturnItemList())
            .toString();
    }
}
