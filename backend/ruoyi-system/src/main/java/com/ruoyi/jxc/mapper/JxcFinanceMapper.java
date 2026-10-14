package com.ruoyi.jxc.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.jxc.domain.JxcPayment;
import com.ruoyi.jxc.domain.JxcPaymentItem;

/**
 * 往来核销 Mapper（收付款登记 / 一对多核销 / 往来台账查询）
 *
 * @author vivi
 */
public interface JxcFinanceMapper
{
    // ==================== 收付款单 ====================
    int insertPayment(JxcPayment payment);
    int insertPaymentItem(JxcPaymentItem item);
    List<Map<String, Object>> selectPaymentList(@Param("payNo") String payNo,
                                                @Param("targetType") String targetType,
                                                @Param("targetId") Long targetId);
    JxcPayment selectPaymentById(@Param("id") Long id);
    List<JxcPaymentItem> selectPaymentItemsByPaymentId(@Param("paymentId") Long paymentId);

    // ==================== 往来台账 ====================
    Map<String, Object> selectPayableById(@Param("id") Long id);
    Map<String, Object> selectReceivableById(@Param("id") Long id);

    /** 应付核销：原子更新（余额不足则 0 行受影响） */
    int writeoffPayable(@Param("id") Long id,
                        @Param("writeoffAmount") BigDecimal writeoffAmount,
                        @Param("operName") String operName);

    /** 应收核销：原子更新 */
    int writeoffReceivable(@Param("id") Long id,
                           @Param("writeoffAmount") BigDecimal writeoffAmount,
                           @Param("operName") String operName);

    /** 应付台账列表（联表供应商） */
    List<Map<String, Object>> selectPayableList(@Param("supplierId") Long supplierId,
                                                @Param("status") String status);

    /** 应收台账列表（联表会员） */
    List<Map<String, Object>> selectReceivableList(@Param("memberId") Long memberId,
                                                   @Param("status") String status);

    /** 某供应商未核销应付单据（balance > 0 且未核销完成） */
    List<Map<String, Object>> selectUnsettledPayable(@Param("supplierId") Long supplierId);

    /** 某会员未核销应收单据 */
    List<Map<String, Object>> selectUnsettledReceivable(@Param("memberId") Long memberId);
}
