package com.ruoyi.jxc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcPurchaseOrderItem;
import com.ruoyi.jxc.mapper.JxcPurchaseOrderMapper;
import com.ruoyi.jxc.domain.JxcPurchaseOrder;
import com.ruoyi.jxc.service.IJxcPurchaseOrderService;

/**
 * 采购订单Service业务层处理
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcPurchaseOrderServiceImpl implements IJxcPurchaseOrderService 
{
    @Autowired
    private JxcPurchaseOrderMapper jxcPurchaseOrderMapper;

    /**
     * 查询采购订单
     * 
     * @param id 采购订单主键
     * @return 采购订单
     */
    @Override
    public JxcPurchaseOrder selectJxcPurchaseOrderById(Long id)
    {
        return jxcPurchaseOrderMapper.selectJxcPurchaseOrderById(id);
    }

    /**
     * 查询采购订单列表
     * 
     * @param jxcPurchaseOrder 采购订单
     * @return 采购订单
     */
    @Override
    public List<JxcPurchaseOrder> selectJxcPurchaseOrderList(JxcPurchaseOrder jxcPurchaseOrder)
    {
        return jxcPurchaseOrderMapper.selectJxcPurchaseOrderList(jxcPurchaseOrder);
    }

    /**
     * 新增采购订单
     * 
     * @param jxcPurchaseOrder 采购订单
     * @return 结果
     */
    @Transactional
    @Override
    public int insertJxcPurchaseOrder(JxcPurchaseOrder jxcPurchaseOrder)
    {
        jxcPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        int rows = jxcPurchaseOrderMapper.insertJxcPurchaseOrder(jxcPurchaseOrder);
        insertJxcPurchaseOrderItem(jxcPurchaseOrder);
        return rows;
    }

    /**
     * 修改采购订单
     * 
     * @param jxcPurchaseOrder 采购订单
     * @return 结果
     */
    @Transactional
    @Override
    public int updateJxcPurchaseOrder(JxcPurchaseOrder jxcPurchaseOrder)
    {
        jxcPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
        jxcPurchaseOrderMapper.deleteJxcPurchaseOrderItemByOrderId(jxcPurchaseOrder.getId());
        insertJxcPurchaseOrderItem(jxcPurchaseOrder);
        return jxcPurchaseOrderMapper.updateJxcPurchaseOrder(jxcPurchaseOrder);
    }

    /**
     * 批量删除采购订单
     * 
     * @param ids 需要删除的采购订单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteJxcPurchaseOrderByIds(Long[] ids)
    {
        jxcPurchaseOrderMapper.deleteJxcPurchaseOrderItemByOrderIds(ids);
        return jxcPurchaseOrderMapper.deleteJxcPurchaseOrderByIds(ids);
    }

    /**
     * 删除采购订单信息
     * 
     * @param id 采购订单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteJxcPurchaseOrderById(Long id)
    {
        jxcPurchaseOrderMapper.deleteJxcPurchaseOrderItemByOrderId(id);
        return jxcPurchaseOrderMapper.deleteJxcPurchaseOrderById(id);
    }

    /**
     * 新增采购订单明细信息
     * 
     * @param jxcPurchaseOrder 采购订单对象
     */
    public void insertJxcPurchaseOrderItem(JxcPurchaseOrder jxcPurchaseOrder)
    {
        List<JxcPurchaseOrderItem> jxcPurchaseOrderItemList = jxcPurchaseOrder.getJxcPurchaseOrderItemList();
        Long id = jxcPurchaseOrder.getId();
        if (StringUtils.isNotNull(jxcPurchaseOrderItemList))
        {
            List<JxcPurchaseOrderItem> list = new ArrayList<JxcPurchaseOrderItem>();
            for (JxcPurchaseOrderItem jxcPurchaseOrderItem : jxcPurchaseOrderItemList)
            {
                jxcPurchaseOrderItem.setOrderId(id);
                list.add(jxcPurchaseOrderItem);
            }
            if (list.size() > 0)
            {
                jxcPurchaseOrderMapper.batchJxcPurchaseOrderItem(list);
            }
        }
    }
}
