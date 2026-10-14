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
 * 采购入库单对象 jxc_stock_in
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcStockIn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 入库单ID */
    private Long id;

    /** 入库单号（SI+yyyyMMdd+3位流水） */
    @Excel(name = "入库单号", readConverterExp = "S=I+yyyyMMdd+3位流水")
    private String inNo;

    /** 来源类型（PURCHASE采购入库 CHECK盘点盘盈，字典jxc_stock_in_source_type） */
    @Excel(name = "来源类型", readConverterExp = "P=URCHASE采购入库,C=HECK盘点盘盈，字典jxc_stock_in_source_type")
    private String sourceType;

    /** 来源单号（采购订单号/盘点单号，可空） */
    @Excel(name = "来源单号", readConverterExp = "采=购订单号/盘点单号，可空")
    private String sourceNo;

    /** 供应商ID（采购入库必填，盘盈入库可空） */
    @Excel(name = "供应商ID", readConverterExp = "采=购入库必填，盘盈入库可空")
    private Long supplierId;

    /** 入库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入库日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date inDate;

    /** 入库总金额 */
    @Excel(name = "入库总金额")
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

    /** 入库单明细信息 */
    private List<JxcStockInItem> jxcStockInItemList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setInNo(String inNo) 
    {
        this.inNo = inNo;
    }

    public String getInNo() 
    {
        return inNo;
    }

    public void setSourceType(String sourceType) 
    {
        this.sourceType = sourceType;
    }

    public String getSourceType() 
    {
        return sourceType;
    }

    public void setSourceNo(String sourceNo) 
    {
        this.sourceNo = sourceNo;
    }

    public String getSourceNo() 
    {
        return sourceNo;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setInDate(Date inDate) 
    {
        this.inDate = inDate;
    }

    public Date getInDate() 
    {
        return inDate;
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

    public List<JxcStockInItem> getJxcStockInItemList()
    {
        return jxcStockInItemList;
    }

    public void setJxcStockInItemList(List<JxcStockInItem> jxcStockInItemList)
    {
        this.jxcStockInItemList = jxcStockInItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("inNo", getInNo())
            .append("sourceType", getSourceType())
            .append("sourceNo", getSourceNo())
            .append("supplierId", getSupplierId())
            .append("inDate", getInDate())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("jxcStockInItemList", getJxcStockInItemList())
            .toString();
    }
}
