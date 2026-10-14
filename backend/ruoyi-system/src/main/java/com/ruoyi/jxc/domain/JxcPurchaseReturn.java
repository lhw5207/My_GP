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
 * 采购退货对象 jxc_purchase_return
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcPurchaseReturn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 采购退货单ID */
    private Long id;

    /** 退货单号（PR+yyyyMMdd+3位流水） */
    @Excel(name = "退货单号", readConverterExp = "P=R+yyyyMMdd+3位流水")
    private String returnNo;

    /** 供应商ID（逻辑关联jxc_supplier.id） */
    @Excel(name = "供应商ID", readConverterExp = "逻=辑关联jxc_supplier.id")
    private Long supplierId;

    /** 原入库单号（可空） */
    @Excel(name = "原入库单号", readConverterExp = "可=空")
    private String sourceInNo;

    /** 退货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退货日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date returnDate;

    /** 退货总金额 */
    @Excel(name = "退货总金额")
    private BigDecimal totalAmount;

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

    /** 采购退货明细信息 */
    private List<JxcPurchaseReturnItem> jxcPurchaseReturnItemList;

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

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setSourceInNo(String sourceInNo) 
    {
        this.sourceInNo = sourceInNo;
    }

    public String getSourceInNo() 
    {
        return sourceInNo;
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

    public List<JxcPurchaseReturnItem> getJxcPurchaseReturnItemList()
    {
        return jxcPurchaseReturnItemList;
    }

    public void setJxcPurchaseReturnItemList(List<JxcPurchaseReturnItem> jxcPurchaseReturnItemList)
    {
        this.jxcPurchaseReturnItemList = jxcPurchaseReturnItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("returnNo", getReturnNo())
            .append("supplierId", getSupplierId())
            .append("sourceInNo", getSourceInNo())
            .append("returnDate", getReturnDate())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("jxcPurchaseReturnItemList", getJxcPurchaseReturnItemList())
            .toString();
    }
}
