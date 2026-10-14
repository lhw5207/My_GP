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

    /**
     * 审核采购入库单（审核即执行）
     * 同事务完成：现存量增加（批次 upsert）→ 库存流水（批次结存快照）→ 应付台账生成 → 单据状态更新
     * 仅草稿态可审核；已审核/已作废单重复审核将被拒绝。
     *
     * @param id 采购入库单主键
     * @param operName 操作人
     * @return 结果
     */
    public int auditJxcStockIn(Long id, String operName);
}
