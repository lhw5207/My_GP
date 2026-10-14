package com.ruoyi.jxc.mapper;

import java.util.List;
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
}
