package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcSaleOrder;

/**
 * 销售单Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcSaleOrderService 
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
     * 批量删除销售单
     * 
     * @param ids 需要删除的销售单主键集合
     * @return 结果
     */
    public int deleteJxcSaleOrderByIds(Long[] ids);

    /**
     * 删除销售单信息
     * 
     * @param id 销售单主键
     * @return 结果
     */
    public int deleteJxcSaleOrderById(Long id);
}
