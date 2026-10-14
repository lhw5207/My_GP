package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcSaleReturn;

/**
 * 销售退货Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcSaleReturnService 
{
    /**
     * 查询销售退货
     * 
     * @param id 销售退货主键
     * @return 销售退货
     */
    public JxcSaleReturn selectJxcSaleReturnById(Long id);

    /**
     * 查询销售退货列表
     * 
     * @param jxcSaleReturn 销售退货
     * @return 销售退货集合
     */
    public List<JxcSaleReturn> selectJxcSaleReturnList(JxcSaleReturn jxcSaleReturn);

    /**
     * 新增销售退货
     * 
     * @param jxcSaleReturn 销售退货
     * @return 结果
     */
    public int insertJxcSaleReturn(JxcSaleReturn jxcSaleReturn);

    /**
     * 修改销售退货
     * 
     * @param jxcSaleReturn 销售退货
     * @return 结果
     */
    public int updateJxcSaleReturn(JxcSaleReturn jxcSaleReturn);

    /**
     * 批量删除销售退货
     * 
     * @param ids 需要删除的销售退货主键集合
     * @return 结果
     */
    public int deleteJxcSaleReturnByIds(Long[] ids);

    /**
     * 删除销售退货信息
     * 
     * @param id 销售退货主键
     * @return 结果
     */
    public int deleteJxcSaleReturnById(Long id);

    /**
     * 审核销售退货单（审核即执行：原批次回增库存 + 流水 + 应收红冲 + 状态更新，同事务）
     *
     * @param id 销售退货单主键
     * @param operName 操作人
     * @return 结果
     */
    public int auditJxcSaleReturn(Long id, String operName);
}
