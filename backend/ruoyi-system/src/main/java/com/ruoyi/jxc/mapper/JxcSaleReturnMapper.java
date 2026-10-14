package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcSaleReturn;
import com.ruoyi.jxc.domain.JxcSaleReturnItem;

/**
 * 销售退货Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcSaleReturnMapper 
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
     * 删除销售退货
     * 
     * @param id 销售退货主键
     * @return 结果
     */
    public int deleteJxcSaleReturnById(Long id);

    /**
     * 批量删除销售退货
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcSaleReturnByIds(Long[] ids);

    /**
     * 批量删除销售退货明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcSaleReturnItemByReturnIds(Long[] ids);
    
    /**
     * 批量新增销售退货明细
     * 
     * @param jxcSaleReturnItemList 销售退货明细列表
     * @return 结果
     */
    public int batchJxcSaleReturnItem(List<JxcSaleReturnItem> jxcSaleReturnItemList);
    

    /**
     * 通过销售退货主键删除销售退货明细信息
     * 
     * @param id 销售退货ID
     * @return 结果
     */
    public int deleteJxcSaleReturnItemByReturnId(Long id);
}
