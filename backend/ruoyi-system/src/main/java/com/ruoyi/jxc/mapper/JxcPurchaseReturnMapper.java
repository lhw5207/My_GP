package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcPurchaseReturn;
import com.ruoyi.jxc.domain.JxcPurchaseReturnItem;

/**
 * 采购退货Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcPurchaseReturnMapper 
{
    /**
     * 查询采购退货
     * 
     * @param id 采购退货主键
     * @return 采购退货
     */
    public JxcPurchaseReturn selectJxcPurchaseReturnById(Long id);

    /**
     * 查询采购退货列表
     * 
     * @param jxcPurchaseReturn 采购退货
     * @return 采购退货集合
     */
    public List<JxcPurchaseReturn> selectJxcPurchaseReturnList(JxcPurchaseReturn jxcPurchaseReturn);

    /**
     * 新增采购退货
     * 
     * @param jxcPurchaseReturn 采购退货
     * @return 结果
     */
    public int insertJxcPurchaseReturn(JxcPurchaseReturn jxcPurchaseReturn);

    /**
     * 修改采购退货
     * 
     * @param jxcPurchaseReturn 采购退货
     * @return 结果
     */
    public int updateJxcPurchaseReturn(JxcPurchaseReturn jxcPurchaseReturn);

    /**
     * 删除采购退货
     * 
     * @param id 采购退货主键
     * @return 结果
     */
    public int deleteJxcPurchaseReturnById(Long id);

    /**
     * 批量删除采购退货
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcPurchaseReturnByIds(Long[] ids);

    /**
     * 批量删除采购退货明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcPurchaseReturnItemByReturnIds(Long[] ids);
    
    /**
     * 批量新增采购退货明细
     * 
     * @param jxcPurchaseReturnItemList 采购退货明细列表
     * @return 结果
     */
    public int batchJxcPurchaseReturnItem(List<JxcPurchaseReturnItem> jxcPurchaseReturnItemList);
    

    /**
     * 通过采购退货主键删除采购退货明细信息
     * 
     * @param id 采购退货ID
     * @return 结果
     */
    public int deleteJxcPurchaseReturnItemByReturnId(Long id);
}
