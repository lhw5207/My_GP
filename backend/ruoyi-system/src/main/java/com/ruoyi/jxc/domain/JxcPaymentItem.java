package com.ruoyi.jxc.domain;

import java.math.BigDecimal;

/**
 * 收付款核销明细对象 jxc_payment_item
 * 支持一笔收付款单核销多张往来单（一对多），也支持同一往来单多次部分核销。
 *
 * @author vivi
 */
public class JxcPaymentItem
{
    private Long id;
    private Long paymentId;
    private Integer rowNo;
    /** 单据类型：1应付 2应收 */
    private String billType;
    /** 往来单ID（jxc_payable.id / jxc_receivable.id） */
    private Long billId;
    /** 本次核销金额 */
    private BigDecimal writeoffAmount;
    private String createBy;
    private String remark;

    /** 展示字段（联表回显，非持久化） */
    private String billNo;
    private BigDecimal billAmount;
    private BigDecimal billBalance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Integer getRowNo() { return rowNo; }
    public void setRowNo(Integer rowNo) { this.rowNo = rowNo; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public BigDecimal getWriteoffAmount() { return writeoffAmount; }
    public void setWriteoffAmount(BigDecimal writeoffAmount) { this.writeoffAmount = writeoffAmount; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public BigDecimal getBillAmount() { return billAmount; }
    public void setBillAmount(BigDecimal billAmount) { this.billAmount = billAmount; }
    public BigDecimal getBillBalance() { return billBalance; }
    public void setBillBalance(BigDecimal billBalance) { this.billBalance = billBalance; }
}
