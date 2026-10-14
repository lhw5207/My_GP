package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购退货明细对象 jxc_purchase_return_item
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcPurchaseReturnItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 采购退货单ID（逻辑关联jxc_purchase_return.id） */
    @Excel(name = "采购退货单ID", readConverterExp = "逻=辑关联jxc_purchase_return.id")
    private Long returnId;

    /** 行号 */
    @Excel(name = "行号")
    private Long rowNo;

    /** 商品ID（逻辑关联jxc_product.id） */
    @Excel(name = "商品ID", readConverterExp = "逻=辑关联jxc_product.id")
    private Long productId;

    /** 批次号（按原入库批次退货，非批次商品空串） */
    @Excel(name = "批次号", readConverterExp = "按=原入库批次退货，非批次商品空串")
    private String batchNo;

    /** 退货数量 */
    @Excel(name = "退货数量")
    private BigDecimal quantity;

    /** 退货单价（按原入库进价） */
    @Excel(name = "退货单价", readConverterExp = "按=原入库进价")
    private BigDecimal price;

    /** 金额（quantity*price） */
    @Excel(name = "金额", readConverterExp = "q=uantity*price")
    private BigDecimal amount;

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
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
