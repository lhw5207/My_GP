package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购订单明细对象 jxc_purchase_order_item
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcPurchaseOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 采购订单ID（逻辑关联jxc_purchase_order.id） */
    @Excel(name = "采购订单ID", readConverterExp = "逻=辑关联jxc_purchase_order.id")
    private Long orderId;

    /** 行号 */
    @Excel(name = "行号")
    private Long rowNo;

    /** 商品ID（逻辑关联jxc_product.id） */
    @Excel(name = "商品ID", readConverterExp = "逻=辑关联jxc_product.id")
    private Long productId;

    /** 采购数量 */
    @Excel(name = "采购数量")
    private BigDecimal quantity;

    /** 采购单价 */
    @Excel(name = "采购单价")
    private BigDecimal price;

    /** 金额（quantity*price） */
    @Excel(name = "金额", readConverterExp = "q=uantity*price")
    private BigDecimal amount;

    /** 已入库数量（分批到货对账，驱动部分执行/已执行状态） */
    @Excel(name = "已入库数量", readConverterExp = "分=批到货对账，驱动部分执行/已执行状态")
    private BigDecimal receivedQty;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
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
    public void setReceivedQty(BigDecimal receivedQty) 
    {
        this.receivedQty = receivedQty;
    }

    public BigDecimal getReceivedQty() 
    {
        return receivedQty;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("rowNo", getRowNo())
            .append("productId", getProductId())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("receivedQty", getReceivedQty())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
