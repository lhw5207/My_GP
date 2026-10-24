package com.ruoyi.jxc.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 支付到账流水对象 jxc_pay_notify
 *
 * 通道说明：ALIPAY=支付宝当面付沙箱（真实异步回调验签）；WECHAT=微信适配层模拟（个人无沙箱，接入正式商户号只需替换通道实现）
 *
 * @author vivi
 * @date 2026-10-24
 */
public class JxcPayNotify extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 支付到账ID */
    private Long id;

    /** 商户挂单号（本地生成，幂等唯一键） */
    @Excel(name = "商户挂单号")
    private String outTradeNo;

    /** 到账通道（ALIPAY/WECHAT） */
    @Excel(name = "到账通道")
    private String channel;

    /** 平台交易号 */
    @Excel(name = "平台交易号")
    private String tradeNo;

    /** 到账金额 */
    @Excel(name = "到账金额")
    private BigDecimal amount;

    /** 付款方账号 */
    @Excel(name = "付款方账号")
    private String buyerId;

    /** 状态（1 到账） */
    @Excel(name = "状态")
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setOutTradeNo(String outTradeNo)
    {
        this.outTradeNo = outTradeNo;
    }

    public String getOutTradeNo()
    {
        return outTradeNo;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setTradeNo(String tradeNo)
    {
        this.tradeNo = tradeNo;
    }

    public String getTradeNo()
    {
        return tradeNo;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setBuyerId(String buyerId)
    {
        this.buyerId = buyerId;
    }

    public String getBuyerId()
    {
        return buyerId;
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
            .append("outTradeNo", getOutTradeNo())
            .append("channel", getChannel())
            .append("tradeNo", getTradeNo())
            .append("amount", getAmount())
            .append("buyerId", getBuyerId())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
