package com.ruoyi.jxc.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.jxc.domain.JxcPayment;

/**
 * 往来核销 Service 接口
 *
 * @author vivi
 */
public interface IJxcFinanceService
{
    /**
     * 登记收付款单（保存即生效：同事务完成一对多核销）
     * 支持一笔款核销多张往来单 / 同一单据多次部分核销；核销金额合计不得超过单据总额（差额视为预收预付）。
     *
     * @param payment 收付款单（含核销明细）
     * @param operName 操作人
     * @return 结果
     */
    public int insertPayment(JxcPayment payment, String operName);

    public JxcPayment selectPaymentById(Long id);

    public List<Map<String, Object>> selectPaymentList(String payNo, String targetType, Long targetId);

    public List<Map<String, Object>> selectPayableList(Long supplierId, String status);

    public List<Map<String, Object>> selectReceivableList(Long memberId, String status);

    public List<Map<String, Object>> selectUnsettled(String targetType, Long targetId);
}
