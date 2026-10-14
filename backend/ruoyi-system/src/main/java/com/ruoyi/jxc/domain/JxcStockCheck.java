package com.ruoyi.jxc.domain;

import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存盘点对象 jxc_stock_check
 * 
 * @author vivi
 * @date 2026-10-14
 */
public class JxcStockCheck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 盘点单ID */
    private Long id;

    /** 盘点单号（PD+yyyyMMdd+3位流水） */
    @Excel(name = "盘点单号", readConverterExp = "P=D+yyyyMMdd+3位流水")
    private String checkNo;

    /** 盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "盘点日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date checkDate;

    /** 状态（0草稿 1已审核 2已作废，仅草稿可作废；审核即生成调整单，字典jxc_bill_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已审核,2=已作废，仅草稿可作废；审核即生成调整单，字典jxc_bill_status")
    private String status;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date auditTime;

    /** 盘点明细信息 */
    private List<JxcStockCheckItem> jxcStockCheckItemList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCheckNo(String checkNo) 
    {
        this.checkNo = checkNo;
    }

    public String getCheckNo() 
    {
        return checkNo;
    }

    public void setCheckDate(Date checkDate) 
    {
        this.checkDate = checkDate;
    }

    public Date getCheckDate() 
    {
        return checkDate;
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

    public List<JxcStockCheckItem> getJxcStockCheckItemList()
    {
        return jxcStockCheckItemList;
    }

    public void setJxcStockCheckItemList(List<JxcStockCheckItem> jxcStockCheckItemList)
    {
        this.jxcStockCheckItemList = jxcStockCheckItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("checkNo", getCheckNo())
            .append("checkDate", getCheckDate())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("jxcStockCheckItemList", getJxcStockCheckItemList())
            .toString();
    }
}
