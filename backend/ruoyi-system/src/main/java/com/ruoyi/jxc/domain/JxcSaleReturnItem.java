package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 销售退货明细对象 jxc_sale_return_item
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcSaleReturnItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 销售退货单ID（逻辑关联jxc_sale_return.id） */
    @Excel(name = "销售退货单ID", readConverterExp = "逻=辑关联jxc_sale_return.id")
    private Long returnId;

    /** 行号 */
    @Excel(name = "行号")
    private Long rowNo;

    /** 商品ID（逻辑关联jxc_product.id） */
    @Excel(name = "商品ID", readConverterExp = "逻=辑关联jxc_product.id")
    private Long productId;

    /** 批次号（按原销售批次回增库存，非批次商品空串） */
    @Excel(name = "批次号", readConverterExp = "按=原销售批次回增库存，非批次商品空串")
    private String batchNo;

    /** 退货数量 */
    @Excel(name = "退货数量")
    private BigDecimal quantity;

    /** 退货单价（按原销售价） */
    @Excel(name = "退货单价", readConverterExp = "按=原销售价")
    private BigDecimal price;

    /** 退货金额（quantity*price） */
    @Excel(name = "退货金额", readConverterExp = "q=uantity*price")
    private BigDecimal amount;

    /** 成本单价快照（还原原销售明细成本，用于毛利红冲） */
    @Excel(name = "成本单价快照", readConverterExp = "还=原原销售明细成本，用于毛利红冲")
    private BigDecimal costPrice;

    /** 成本金额快照（quantity*cost_price） */
    @Excel(name = "成本金额快照", readConverterExp = "q=uantity*cost_price")
    private BigDecimal costAmount;

    /** 原销售单明细ID（还原成本快照依据，可空） */
    @Excel(name = "原销售单明细ID", readConverterExp = "还=原成本快照依据，可空")
    private Long sourceSaleItemId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setReturnId(Long returnId) 
    {
        this.returnId = returnId;
    }

    public Long getReturnId() 
    {
        return returnId;
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
    public void setSourceSaleItemId(Long sourceSaleItemId) 
    {
        this.sourceSaleItemId = sourceSaleItemId;
    }

    public Long getSourceSaleItemId() 
    {
        return sourceSaleItemId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("returnId", getReturnId())
            .append("rowNo", getRowNo())
            .append("productId", getProductId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("costPrice", getCostPrice())
            .append("costAmount", getCostAmount())
            .append("sourceSaleItemId", getSourceSaleItemId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
