package com.ruoyi.jxc.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.jxc.domain.JxcStockIn;
import com.ruoyi.jxc.domain.JxcStockInItem;

/**
 * 采购入库单Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcStockInMapper 
{
    /**
     * 查询采购入库单
     * 
     * @param id 采购入库单主键
     * @return 采购入库单
     */
    public JxcStockIn selectJxcStockInById(Long id);

    /**
     * 查询采购入库单列表
     * 
     * @param jxcStockIn 采购入库单
     * @return 采购入库单集合
     */
    public List<JxcStockIn> selectJxcStockInList(JxcStockIn jxcStockIn);

    /**
     * 新增采购入库单
     * 
     * @param jxcStockIn 采购入库单
     * @return 结果
     */
    public int insertJxcStockIn(JxcStockIn jxcStockIn);

    /**
     * 修改采购入库单
     * 
     * @param jxcStockIn 采购入库单
     * @return 结果
     */
    public int updateJxcStockIn(JxcStockIn jxcStockIn);

    /**
     * 删除采购入库单
     * 
     * @param id 采购入库单主键
     * @return 结果
     */
    public int deleteJxcStockInById(Long id);

    /**
     * 批量删除采购入库单
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcStockInByIds(Long[] ids);

    /**
     * 批量删除入库单明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcStockInItemByInIds(Long[] ids);
    
    /**
     * 批量新增入库单明细
     * 
     * @param jxcStockInItemList 入库单明细列表
     * @return 结果
     */
    public int batchJxcStockInItem(List<JxcStockInItem> jxcStockInItemList);
    

    /**
     * 通过采购入库单主键删除入库单明细信息
     * 
     * @param id 采购入库单ID
     * @return 结果
     */
    public int deleteJxcStockInItemByInId(Long id);

    // ==================== 采购订单回写（入库单审核时联动） ====================

    /**
     * 按订单号查询采购订单ID
     */
    public Long selectPurchaseOrderIdByNo(@Param("orderNo") String orderNo);

    /**
     * 回写订单明细已入库数量（received_qty += 本次入库量）
     */
    public int updateOrderItemReceivedQty(@Param("orderId") Long orderId,
                                          @Param("productId") Long productId,
                                          @Param("qty") java.math.BigDecimal qty);

    /**
     * 统计订单未完成明细行数（received_qty &lt; quantity 的行数）
     */
    public int countOrderUnfinishedItems(@Param("orderId") Long orderId);

    /**
     * 更新采购订单状态（2 部分执行 / 3 已执行）
     */
    public int updatePurchaseOrderStatus(@Param("orderId") Long orderId,
                                         @Param("status") String status);
}
