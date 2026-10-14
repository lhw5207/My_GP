package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcPurchaseOrder;
import com.ruoyi.jxc.domain.JxcPurchaseOrderItem;

/**
 * 采购订单Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcPurchaseOrderMapper 
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
     * 删除采购订单
     * 
     * @param id 采购订单主键
     * @return 结果
     */
    public int deleteJxcPurchaseOrderById(Long id);

    /**
     * 批量删除采购订单
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcPurchaseOrderByIds(Long[] ids);

    /**
     * 批量删除采购订单明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcPurchaseOrderItemByOrderIds(Long[] ids);
    
    /**
     * 批量新增采购订单明细
     * 
     * @param jxcPurchaseOrderItemList 采购订单明细列表
     * @return 结果
     */
    public int batchJxcPurchaseOrderItem(List<JxcPurchaseOrderItem> jxcPurchaseOrderItemList);
    

    /**
     * 通过采购订单主键删除采购订单明细信息
     * 
     * @param id 采购订单ID
     * @return 结果
     */
    public int deleteJxcPurchaseOrderItemByOrderId(Long id);
}
