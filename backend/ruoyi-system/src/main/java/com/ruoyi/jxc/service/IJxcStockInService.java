package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcStockIn;

/**
 * 采购入库单Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcStockInService 
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
     * 批量删除采购入库单
     * 
     * @param ids 需要删除的采购入库单主键集合
     * @return 结果
     */
    public int deleteJxcStockInByIds(Long[] ids);

    /**
     * 删除采购入库单信息
     * 
     * @param id 采购入库单主键
     * @return 结果
     */
    public int deleteJxcStockInById(Long id);
}
