package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 盘点明细对象 jxc_stock_check_item
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcStockCheckItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 盘点单ID（逻辑关联jxc_stock_check.id） */
    @Excel(name = "盘点单ID", readConverterExp = "逻=辑关联jxc_stock_check.id")
    private Long checkId;

    /** 行号 */
    @Excel(name = "行号")
    private Long rowNo;

    /** 商品ID（逻辑关联jxc_product.id） */
    @Excel(name = "商品ID", readConverterExp = "逻=辑关联jxc_product.id")
    private Long productId;

    /** 批次号（非批次商品空串） */
    @Excel(name = "批次号", readConverterExp = "非=批次商品空串")
    private String batchNo;

    /** 账面数量（盘点时jxc_stock快照） */
    @Excel(name = "账面数量", readConverterExp = "盘=点时jxc_stock快照")
    private BigDecimal bookQty;

    /** 实盘数量 */
    @Excel(name = "实盘数量")
    private BigDecimal actualQty;

    /** 盈亏数量（=actual_qty-book_qty，正为盈负为亏） */
    @Excel(name = "盈亏数量", readConverterExp = "==actual_qty-book_qty，正为盈负为亏")
    private BigDecimal diffQty;

    /** 生成的盘盈入库单号（审核后回写） */
    @Excel(name = "生成的盘盈入库单号", readConverterExp = "审=核后回写")
    private String genInNo;

    /** 生成的盘亏出库单号（审核后回写） */
    @Excel(name = "生成的盘亏出库单号", readConverterExp = "审=核后回写")
    private String genOutNo;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setCheckId(Long checkId) 
    {
        this.checkId = checkId;
    }

    public Long getCheckId() 
    {
        return checkId;
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
    public void setBookQty(BigDecimal bookQty) 
    {
        this.bookQty = bookQty;
    }

    public BigDecimal getBookQty() 
    {
        return bookQty;
    }
    public void setActualQty(BigDecimal actualQty) 
    {
        this.actualQty = actualQty;
    }

    public BigDecimal getActualQty() 
    {
        return actualQty;
    }
    public void setDiffQty(BigDecimal diffQty) 
    {
        this.diffQty = diffQty;
    }

    public BigDecimal getDiffQty() 
    {
        return diffQty;
    }
    public void setGenInNo(String genInNo) 
    {
        this.genInNo = genInNo;
    }

    public String getGenInNo() 
    {
        return genInNo;
    }
    public void setGenOutNo(String genOutNo) 
    {
        this.genOutNo = genOutNo;
    }

    public String getGenOutNo() 
    {
        return genOutNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("checkId", getCheckId())
            .append("rowNo", getRowNo())
            .append("productId", getProductId())
            .append("batchNo", getBatchNo())
            .append("bookQty", getBookQty())
            .append("actualQty", getActualQty())
            .append("diffQty", getDiffQty())
            .append("genInNo", getGenInNo())
            .append("genOutNo", getGenOutNo())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
