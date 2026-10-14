package com.ruoyi.jxc.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 商品信息对象 jxc_product
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long id;

    /** 分类ID（逻辑关联jxc_product_category.id） */
    @Excel(name = "分类ID", readConverterExp = "逻=辑关联jxc_product_category.id")
    private Long categoryId;

    /** 商品编码（唯一） */
    @Excel(name = "商品编码", readConverterExp = "唯=一")
    private String productCode;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String productName;

    /** 商品条码（扫码录入用） */
    @Excel(name = "商品条码", readConverterExp = "扫=码录入用")
    private String barcode;

    /** 计量单位（字典jxc_product_unit） */
    @Excel(name = "计量单位", readConverterExp = "字=典jxc_product_unit")
    private String unit;

    /** 规格（如330ml/瓶） */
    @Excel(name = "规格", readConverterExp = "如=330ml/瓶")
    private String spec;

    /** 参考进价（最近/默认进价，实际成本以批次为准） */
    @Excel(name = "参考进价", readConverterExp = "最=近/默认进价，实际成本以批次为准")
    private BigDecimal purchasePrice;

    /** 标准售价 */
    @Excel(name = "标准售价")
    private BigDecimal salePrice;

    /** 库存预警下限（quantity<=stock_min 触发预警） */
    @Excel(name = "库存预警下限", readConverterExp = "q=uantity<=stock_min,触=发预警")
    private BigDecimal stockMin;

    /** 库存预警上限（可空） */
    @Excel(name = "库存预警上限", readConverterExp = "可=空")
    private BigDecimal stockMax;

    /** 保质期天数（NULL表示不做效期管理） */
    @Excel(name = "保质期天数", readConverterExp = "N=ULL表示不做效期管理")
    private Long shelfLifeDays;

    /** 是否批次管理（0否 1是，字典sys_yes_no） */
    @Excel(name = "是否批次管理", readConverterExp = "0=否,1=是，字典sys_yes_no")
    private String enableBatch;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setProductCode(String productCode) 
    {
        this.productCode = productCode;
    }

    public String getProductCode() 
    {
        return productCode;
    }

    public void setProductName(String productName) 
    {
        this.productName = productName;
    }

    public String getProductName() 
    {
        return productName;
    }

    public void setBarcode(String barcode) 
    {
        this.barcode = barcode;
    }

    public String getBarcode() 
    {
        return barcode;
    }

    public void setUnit(String unit) 
    {
        this.unit = unit;
    }

    public String getUnit() 
    {
        return unit;
    }

    public void setSpec(String spec) 
    {
        this.spec = spec;
    }

    public String getSpec() 
    {
        return spec;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) 
    {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getPurchasePrice() 
    {
        return purchasePrice;
    }

    public void setSalePrice(BigDecimal salePrice) 
    {
        this.salePrice = salePrice;
    }

    public BigDecimal getSalePrice() 
    {
        return salePrice;
    }

    public void setStockMin(BigDecimal stockMin) 
    {
        this.stockMin = stockMin;
    }

    public BigDecimal getStockMin() 
    {
        return stockMin;
    }

    public void setStockMax(BigDecimal stockMax) 
    {
        this.stockMax = stockMax;
    }

    public BigDecimal getStockMax() 
    {
        return stockMax;
    }

    public void setShelfLifeDays(Long shelfLifeDays) 
    {
        this.shelfLifeDays = shelfLifeDays;
    }

    public Long getShelfLifeDays() 
    {
        return shelfLifeDays;
    }

    public void setEnableBatch(String enableBatch) 
    {
        this.enableBatch = enableBatch;
    }

    public String getEnableBatch() 
    {
        return enableBatch;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("categoryId", getCategoryId())
            .append("productCode", getProductCode())
            .append("productName", getProductName())
            .append("barcode", getBarcode())
            .append("unit", getUnit())
            .append("spec", getSpec())
            .append("purchasePrice", getPurchasePrice())
            .append("salePrice", getSalePrice())
            .append("stockMin", getStockMin())
            .append("stockMax", getStockMax())
            .append("shelfLifeDays", getShelfLifeDays())
            .append("enableBatch", getEnableBatch())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
