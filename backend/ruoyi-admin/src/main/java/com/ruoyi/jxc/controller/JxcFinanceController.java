package com.ruoyi.jxc.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.jxc.domain.JxcPayment;
import com.ruoyi.jxc.service.IJxcFinanceService;

/**
 * 往来核销：收付款登记（一对多核销） / 应收应付台账查询
 *
 * @author vivi
 */
@RestController
@RequestMapping("/jxc/finance")
public class JxcFinanceController extends BaseController
{
    @Autowired
    private IJxcFinanceService jxcFinanceService;

    /** 收付款单列表 */
    @PreAuthorize("@ss.hasPermi('jxc:fin:list')")
    @GetMapping("/payment/list")
    public TableDataInfo paymentList(String payNo, String targetType, Long targetId)
    {
        startPage();
        List<Map<String, Object>> list = jxcFinanceService.selectPaymentList(payNo, targetType, targetId);
        return getDataTable(list);
    }

    /** 收付款单详情（含核销明细） */
    @PreAuthorize("@ss.hasPermi('jxc:fin:list')")
    @GetMapping("/payment/{id}")
    public AjaxResult paymentInfo(@PathVariable Long id)
    {
        return success(jxcFinanceService.selectPaymentById(id));
    }

    /** 登记收付款单（保存即生效，同事务一对多核销） */
    @PreAuthorize("@ss.hasPermi('jxc:fin:add')")
    @Log(title = "收付款登记", businessType = BusinessType.INSERT)
    @PostMapping("/payment")
    public AjaxResult addPayment(@RequestBody JxcPayment payment)
    {
        return toAjax(jxcFinanceService.insertPayment(payment, getUsername()));
    }

    /** 应付台账列表 */
    @PreAuthorize("@ss.hasPermi('jxc:payable:list')")
    @GetMapping("/payable/list")
    public TableDataInfo payableList(Long supplierId, String status)
    {
        startPage();
        List<Map<String, Object>> list = jxcFinanceService.selectPayableList(supplierId, status);
        return getDataTable(list);
    }

    /** 应收台账列表 */
    @PreAuthorize("@ss.hasPermi('jxc:receivable:list')")
    @GetMapping("/receivable/list")
    public TableDataInfo receivableList(Long memberId, String status)
    {
        startPage();
        List<Map<String, Object>> list = jxcFinanceService.selectReceivableList(memberId, status);
        return getDataTable(list);
    }

    /** 未核销单据（登记页选择用）：targetType 1供应商付款 2会员收款 */
    @PreAuthorize("@ss.hasPermi('jxc:fin:list')")
    @GetMapping("/unsettled")
    public AjaxResult unsettled(String targetType, Long targetId)
    {
        return success(jxcFinanceService.selectUnsettled(targetType, targetId));
    }
}
