package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcSaleOrder;
import com.ruoyi.jxc.domain.JxcSaleOrderItem;

/**
 * 销售单Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcSaleOrderMapper 
{
    /**
     * 查询销售单
     * 
     * @param id 销售单主键
     * @return 销售单
     */
    public JxcSaleOrder selectJxcSaleOrderById(Long id);

    /**
     * 查询销售单列表
     * 
     * @param jxcSaleOrder 销售单
     * @return 销售单集合
     */
    public List<JxcSaleOrder> selectJxcSaleOrderList(JxcSaleOrder jxcSaleOrder);

    /**
     * 新增销售单
     * 
     * @param jxcSaleOrder 销售单
     * @return 结果
     */
    public int insertJxcSaleOrder(JxcSaleOrder jxcSaleOrder);

    /**
     * 修改销售单
     * 
     * @param jxcSaleOrder 销售单
     * @return 结果
     */
    public int updateJxcSaleOrder(JxcSaleOrder jxcSaleOrder);

    /**
     * 删除销售单
     * 
     * @param id 销售单主键
     * @return 结果
     */
    public int deleteJxcSaleOrderById(Long id);

    /**
     * 批量删除销售单
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcSaleOrderByIds(Long[] ids);

    /**
     * 批量删除销售单明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcSaleOrderItemBySaleIds(Long[] ids);
    
    /**
     * 批量新增销售单明细
     * 
     * @param jxcSaleOrderItemList 销售单明细列表
     * @return 结果
     */
    public int batchJxcSaleOrderItem(List<JxcSaleOrderItem> jxcSaleOrderItemList);
    

    /**
     * 通过销售单主键删除销售单明细信息
     * 
     * @param id 销售单ID
     * @return 结果
     */
    public int deleteJxcSaleOrderItemBySaleId(Long id);
}
