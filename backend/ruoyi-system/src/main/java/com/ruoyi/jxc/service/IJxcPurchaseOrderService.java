package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcPurchaseOrder;

/**
 * 采购订单Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcPurchaseOrderService 
{
    /**
     * 查询采购订单
     * 
     * @param id 采购订单主键
     * @return 采购订单
     */
    public JxcPurchaseOrder selectJxcPurchaseOrderById(Long id);

    /**
     * 查询采购订单列表
     * 
     * @param jxcPurchaseOrder 采购订单
     * @return 采购订单集合
     */
    public List<JxcPurchaseOrder> selectJxcPurchaseOrderList(JxcPurchaseOrder jxcPurchaseOrder);

    /**
     * 新增采购订单
     * 
     * @param jxcPurchaseOrder 采购订单
     * @return 结果
     */
    public int insertJxcPurchaseOrder(JxcPurchaseOrder jxcPurchaseOrder);

    /**
     * 修改采购订单
     * 
     * @param jxcPurchaseOrder 采购订单
     * @return 结果
     */
    public int updateJxcPurchaseOrder(JxcPurchaseOrder jxcPurchaseOrder);

    /**
     * 批量删除采购订单
     * 
     * @param ids 需要删除的采购订单主键集合
     * @return 结果
     */
    public int deleteJxcPurchaseOrderByIds(Long[] ids);

    /**
     * 删除采购订单信息
     * 
     * @param id 采购订单主键
     * @return 结果
     */
    public int deleteJxcPurchaseOrderById(Long id);
}
