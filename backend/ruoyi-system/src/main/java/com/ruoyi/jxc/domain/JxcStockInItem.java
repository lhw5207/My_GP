package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 入库单明细对象 jxc_stock_in_item
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcStockInItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 入库单ID（逻辑关联jxc_stock_in.id） */
    @Excel(name = "入库单ID", readConverterExp = "逻=辑关联jxc_stock_in.id")
    private Long inId;

    /** 行号 */
    @Excel(name = "行号")
    private Long rowNo;

    /** 商品ID（逻辑关联jxc_product.id） */
    @Excel(name = "商品ID", readConverterExp = "逻=辑关联jxc_product.id")
    private Long productId;

    /** 批次号（非批次商品统一空串） */
    @Excel(name = "批次号", readConverterExp = "非=批次商品统一空串")
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expiryDate;

    /** 入库数量 */
    @Excel(name = "入库数量")
    private BigDecimal quantity;

    /** 入库单价（批次成本，盘盈取商品档案进价快照） */
    @Excel(name = "入库单价", readConverterExp = "批=次成本，盘盈取商品档案进价快照")
    private BigDecimal price;

    /** 金额（quantity*price） */
    @Excel(name = "金额", readConverterExp = "q=uantity*price")
    private BigDecimal amount;

    /** 来源采购订单ID（可空） */
    @Excel(name = "来源采购订单ID", readConverterExp = "可=空")
    private Long sourceOrderId;

    /** 来源采购订单明细ID（可空，审核时回写received_qty） */
    @Excel(name = "来源采购订单明细ID", readConverterExp = "可=空，审核时回写received_qty")
    private Long sourceOrderItemId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setInId(Long inId) 
    {
        this.inId = inId;
    }

    public Long getInId() 
    {
        return inId;
    }
    public void setRowNo(Long rowNo) 
    {
        this.rowNo = rowNo;
    }

    public Long getRowNo() 
    {
        return rowNo;
    }
    public void setProductId(Long productId) 
    {
        this.productId = productId;
    }

    public Long getProductId() 
    {
        return productId;
    }
    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }
    public void setProductionDate(Date productionDate) 
    {
        this.productionDate = productionDate;
    }

    public Date getProductionDate() 
    {
        return productionDate;
    }
    public void setExpiryDate(Date expiryDate) 
    {
        this.expiryDate = expiryDate;
    }

    public Date getExpiryDate() 
    {
        return expiryDate;
    }
    public void setQuantity(BigDecimal quantity) 
    {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity() 
    {
        return quantity;
    }
    public void setPrice(BigDecimal price) 
    {
        this.price = price;
    }

    public BigDecimal getPrice() 
    {
        return price;
    }
    public void setAmount(BigDecimal amount) 
    {
        this.amount = amount;
    }

    public BigDecimal getAmount() 
    {
        return amount;
    }
    public void setSourceOrderId(Long sourceOrderId) 
    {
        this.sourceOrderId = sourceOrderId;
    }

    public Long getSourceOrderId() 
    {
        return sourceOrderId;
    }
    public void setSourceOrderItemId(Long sourceOrderItemId) 
    {
        this.sourceOrderItemId = sourceOrderItemId;
    }

    public Long getSourceOrderItemId() 
    {
        return sourceOrderItemId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("inId", getInId())
            .append("rowNo", getRowNo())
            .append("productId", getProductId())
            .append("batchNo", getBatchNo())
            .append("productionDate", getProductionDate())
            .append("expiryDate", getExpiryDate())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("sourceOrderId", getSourceOrderId())
            .append("sourceOrderItemId", getSourceOrderItemId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
