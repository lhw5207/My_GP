package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcStockCheck;

/**
 * 库存盘点Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcStockCheckService 
{
    /**
     * 查询库存盘点
     * 
     * @param id 库存盘点主键
     * @return 库存盘点
     */
    public JxcStockCheck selectJxcStockCheckById(Long id);

    /**
     * 查询库存盘点列表
     * 
     * @param jxcStockCheck 库存盘点
     * @return 库存盘点集合
     */
    public List<JxcStockCheck> selectJxcStockCheckList(JxcStockCheck jxcStockCheck);

    /**
     * 新增库存盘点
     * 
     * @param jxcStockCheck 库存盘点
     * @return 结果
     */
    public int insertJxcStockCheck(JxcStockCheck jxcStockCheck);

    /**
     * 修改库存盘点
     * 
     * @param jxcStockCheck 库存盘点
     * @return 结果
     */
    public int updateJxcStockCheck(JxcStockCheck jxcStockCheck);

    /**
     * 批量删除库存盘点
     * 
     * @param ids 需要删除的库存盘点主键集合
     * @return 结果
     */
    public int deleteJxcStockCheckByIds(Long[] ids);

    /**
     * 删除库存盘点信息
     * 
     * @param id 库存盘点主键
     * @return 结果
     */
    public int deleteJxcStockCheckById(Long id);

    /**
     * 审核盘点单（审核即执行：盘盈生成入库单 / 盘亏生成出库单，联动库存与流水，回写调整单号）
     *
     * @param id 盘点单主键
     * @param operName 操作人
     * @return 结果
     */
    public int auditJxcStockCheck(Long id, String operName);
}
