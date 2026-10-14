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
 * 采购订单对象 jxc_purchase_order
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcPurchaseOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 采购订单ID */
    private Long id;

    /** 采购订单号（PO+yyyyMMdd+3位流水） */
    @Excel(name = "采购订单号", readConverterExp = "P=O+yyyyMMdd+3位流水")
    private String orderNo;

    /** 供应商ID（逻辑关联jxc_supplier.id） */
    @Excel(name = "供应商ID", readConverterExp = "逻=辑关联jxc_supplier.id")
    private Long supplierId;

    /** 单据日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "单据日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date orderDate;

    /** 订单总金额 */
    @Excel(name = "订单总金额")
    private BigDecimal totalAmount;

    /** 状态（0草稿 1已审核 2部分执行 3已执行 4已作废，字典jxc_purchase_order_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已审核,2=部分执行,3=已执行,4=已作废，字典jxc_purchase_order_status")
    private String status;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date auditTime;

    /** 采购订单明细信息 */
    private List<JxcPurchaseOrderItem> jxcPurchaseOrderItemList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setOrderDate(Date orderDate) 
    {
        this.orderDate = orderDate;
    }

    public Date getOrderDate() 
    {
        return orderDate;
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

    public List<JxcPurchaseOrderItem> getJxcPurchaseOrderItemList()
    {
        return jxcPurchaseOrderItemList;
    }

    public void setJxcPurchaseOrderItemList(List<JxcPurchaseOrderItem> jxcPurchaseOrderItemList)
    {
        this.jxcPurchaseOrderItemList = jxcPurchaseOrderItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderNo", getOrderNo())
            .append("supplierId", getSupplierId())
            .append("orderDate", getOrderDate())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("jxcPurchaseOrderItemList", getJxcPurchaseOrderItemList())
            .toString();
    }
}
