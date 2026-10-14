package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcPurchaseReturn;

/**
 * 采购退货Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcPurchaseReturnService 
{
    /**
     * 查询采购退货
     * 
     * @param id 采购退货主键
     * @return 采购退货
     */
    public JxcPurchaseReturn selectJxcPurchaseReturnById(Long id);

    /**
     * 查询采购退货列表
     * 
     * @param jxcPurchaseReturn 采购退货
     * @return 采购退货集合
     */
    public List<JxcPurchaseReturn> selectJxcPurchaseReturnList(JxcPurchaseReturn jxcPurchaseReturn);

    /**
     * 新增采购退货
     * 
     * @param jxcPurchaseReturn 采购退货
     * @return 结果
     */
    public int insertJxcPurchaseReturn(JxcPurchaseReturn jxcPurchaseReturn);

    /**
     * 修改采购退货
     * 
     * @param jxcPurchaseReturn 采购退货
     * @return 结果
     */
    public int updateJxcPurchaseReturn(JxcPurchaseReturn jxcPurchaseReturn);

    /**
     * 批量删除采购退货
     * 
     * @param ids 需要删除的采购退货主键集合
     * @return 结果
     */
    public int deleteJxcPurchaseReturnByIds(Long[] ids);

    /**
     * 删除采购退货信息
     * 
     * @param id 采购退货主键
     * @return 结果
     */
    public int deleteJxcPurchaseReturnById(Long id);

    /**
     * 审核采购退货单（审核即执行：库存扣减 + 流水 + 应付红冲 + 状态更新，同事务）
     *
     * @param id 采购退货单主键
     * @param operName 操作人
     * @return 结果
     */
    public int auditJxcPurchaseReturn(Long id, String operName);
}
