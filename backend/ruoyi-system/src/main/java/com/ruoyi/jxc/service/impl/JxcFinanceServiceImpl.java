package com.ruoyi.jxc.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcPayment;
import com.ruoyi.jxc.domain.JxcPaymentItem;
import com.ruoyi.jxc.mapper.JxcFinanceMapper;
import com.ruoyi.jxc.service.IJxcFinanceService;

/**
 * 往来核销 Service 实现
 * 核销核心：原子 UPDATE（balance >= 核销额才生效）+ 台账余额递减 + 状态联动（0未核销/1部分/2已核销）
 *
 * @author vivi
 */
@Service
public class JxcFinanceServiceImpl implements IJxcFinanceService
{
    @Autowired
    private JxcFinanceMapper jxcFinanceMapper;

    @Transactional
    @Override
    public int insertPayment(JxcPayment payment, String operName)
    {
        if (payment.getTargetType() == null || payment.getTargetId() == null)
        {
            throw new ServiceException("请选择往来单位（供应商/会员）");
        }
        List<JxcPaymentItem> items = payment.getJxcPaymentItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请至少选择一张待核销的单据");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (JxcPaymentItem item : items)
        {
            if (item.getWriteoffAmount() == null || item.getWriteoffAmount().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("核销金额必须大于 0");
            }
            sum = sum.add(item.getWriteoffAmount());
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            payment.setAmount(sum);
        }
        if (sum.compareTo(payment.getAmount()) > 0)
        {
            throw new ServiceException("核销金额合计（" + sum + "）不能超过收付款总额（" + payment.getAmount() + "）");
        }

        // 逐笔核销（原子更新，任一失败整单回滚）
        for (JxcPaymentItem item : items)
        {
            if ("1".equals(item.getBillType()))
            {
                Map<String, Object> bill = jxcFinanceMapper.selectPayableById(item.getBillId());
                if (bill == null)
                {
                    throw new ServiceException("应付单据不存在（ID:" + item.getBillId() + "）");
                }
                int affected = jxcFinanceMapper.writeoffPayable(item.getBillId(), item.getWriteoffAmount(), operName);
                if (affected == 0)
                {
                    throw new ServiceException("单据[" + bill.get("sourceNo") + "]可核销余额不足（余额 " + bill.get("balance") + "，本次核销 " + item.getWriteoffAmount() + "）");
                }
            }
            else if ("2".equals(item.getBillType()))
            {
                Map<String, Object> bill = jxcFinanceMapper.selectReceivableById(item.getBillId());
                if (bill == null)
                {
                    throw new ServiceException("应收单据不存在（ID:" + item.getBillId() + "）");
                }
                int affected = jxcFinanceMapper.writeoffReceivable(item.getBillId(), item.getWriteoffAmount(), operName);
                if (affected == 0)
                {
                    throw new ServiceException("单据[" + bill.get("sourceNo") + "]可核销余额不足（余额 " + bill.get("balance") + "，本次核销 " + item.getWriteoffAmount() + "）");
                }
            }
            else
            {
                throw new ServiceException("核销单据类型不合法");
            }
        }

        // 保存收付款单（保存即生效）
        payment.setStatus("1");
        payment.setCreateBy(operName);
        jxcFinanceMapper.insertPayment(payment);
        int rowNo = 1;
        for (JxcPaymentItem item : items)
        {
            item.setPaymentId(payment.getId());
            item.setRowNo(rowNo++);
            item.setCreateBy(operName);
            jxcFinanceMapper.insertPaymentItem(item);
        }
        return 1;
    }

    @Override
    public JxcPayment selectPaymentById(Long id)
    {
        JxcPayment payment = jxcFinanceMapper.selectPaymentById(id);
        if (payment != null)
        {
            payment.setJxcPaymentItemList(jxcFinanceMapper.selectPaymentItemsByPaymentId(id));
        }
        return payment;
    }

    @Override
    public List<Map<String, Object>> selectPaymentList(String payNo, String targetType, Long targetId)
    {
        return jxcFinanceMapper.selectPaymentList(payNo, targetType, targetId);
    }

    @Override
    public List<Map<String, Object>> selectPayableList(Long supplierId, String status)
    {
        return jxcFinanceMapper.selectPayableList(supplierId, status);
    }

    @Override
    public List<Map<String, Object>> selectReceivableList(Long memberId, String status)
    {
        return jxcFinanceMapper.selectReceivableList(memberId, status);
    }

    @Override
    public List<Map<String, Object>> selectUnsettled(String targetType, Long targetId)
    {
        if ("1".equals(targetType))
        {
            return jxcFinanceMapper.selectUnsettledPayable(targetId);
        }
        else if ("2".equals(targetType))
        {
            return jxcFinanceMapper.selectUnsettledReceivable(targetId);
        }
        throw new ServiceException("往来单位类型不合法");
    }
}
