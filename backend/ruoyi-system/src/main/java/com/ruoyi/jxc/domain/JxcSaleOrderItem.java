package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 销售单明细对象 jxc_sale_order_item
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcSaleOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 销售单ID（逻辑关联jxc_sale_order.id） */
    @Excel(name = "销售单ID", readConverterExp = "逻=辑关联jxc_sale_order.id")
    private Long saleId;

    /** 行号 */
    @Excel(name = "行号")
    private Long rowNo;

    /** 商品ID（逻辑关联jxc_product.id） */
    @Excel(name = "商品ID", readConverterExp = "逻=辑关联jxc_product.id")
    private Long productId;

    /** 批次号（FEFO拆批出库后的实际扣减批次，非批次商品空串） */
    @Excel(name = "批次号", readConverterExp = "F=EFO拆批出库后的实际扣减批次，非批次商品空串")
    private String batchNo;

    /** 销售数量 */
    @Excel(name = "销售数量")
    private BigDecimal quantity;

    /** 销售单价 */
    @Excel(name = "销售单价")
    private BigDecimal price;

    /** 销售金额（quantity*price） */
    @Excel(name = "销售金额", readConverterExp = "q=uantity*price")
    private BigDecimal amount;

    /** 成本单价快照（出库时按所扣批次进价写入，此后不可变） */
    @Excel(name = "成本单价快照", readConverterExp = "出=库时按所扣批次进价写入，此后不可变")
    private BigDecimal costPrice;

    /** 成本金额快照（quantity*cost_price，毛利=amount-cost_amount） */
    @Excel(name = "成本金额快照", readConverterExp = "q=uantity*cost_price，毛利=amount-cost_amount")
    private BigDecimal costAmount;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setSaleId(Long saleId) 
    {
        this.saleId = saleId;
    }

    public Long getSaleId() 
    {
        return saleId;
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
    public void setCostPrice(BigDecimal costPrice) 
    {
        this.costPrice = costPrice;
    }

    public BigDecimal getCostPrice() 
    {
        return costPrice;
    }
    public void setCostAmount(BigDecimal costAmount) 
    {
        this.costAmount = costAmount;
    }

    public BigDecimal getCostAmount() 
    {
        return costAmount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("saleId", getSaleId())
            .append("rowNo", getRowNo())
            .append("productId", getProductId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("costPrice", getCostPrice())
            .append("costAmount", getCostAmount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
