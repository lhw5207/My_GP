package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcStockCheck;
import com.ruoyi.jxc.domain.JxcStockCheckItem;

/**
 * 库存盘点Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcStockCheckMapper 
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
     * 删除库存盘点
     * 
     * @param id 库存盘点主键
     * @return 结果
     */
    public int deleteJxcStockCheckById(Long id);

    /**
     * 批量删除库存盘点
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcStockCheckByIds(Long[] ids);

    /**
     * 批量删除盘点明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcStockCheckItemByCheckIds(Long[] ids);
    
    /**
     * 批量新增盘点明细
     * 
     * @param jxcStockCheckItemList 盘点明细列表
     * @return 结果
     */
    public int batchJxcStockCheckItem(List<JxcStockCheckItem> jxcStockCheckItemList);
    

    /**
     * 通过库存盘点主键删除盘点明细信息
     * 
     * @param id 库存盘点ID
     * @return 结果
     */
    public int deleteJxcStockCheckItemByCheckId(Long id);

    // ==================== 盘点调整单（审核时自动生成盘盈入库单/盘亏出库单） ====================

    /**
     * 查询商品档案参考进价（盘盈盘亏成本快照）
     */
    java.math.BigDecimal selectProductPurchasePrice(@org.apache.ibatis.annotations.Param("productId") Long productId);

    /**
     * 生成盘盈入库单（source_type=CHECK，直接已审核状态）
     */
    int insertAdjustInHead(@org.apache.ibatis.annotations.Param("inNo") String inNo,
                           @org.apache.ibatis.annotations.Param("sourceNo") String sourceNo,
                           @org.apache.ibatis.annotations.Param("totalAmount") java.math.BigDecimal totalAmount,
                           @org.apache.ibatis.annotations.Param("operName") String operName);

    Long selectAdjustInIdByNo(@org.apache.ibatis.annotations.Param("inNo") String inNo);

    int insertAdjustInItem(@org.apache.ibatis.annotations.Param("inId") Long inId,
                           @org.apache.ibatis.annotations.Param("rowNo") Integer rowNo,
                           @org.apache.ibatis.annotations.Param("productId") Long productId,
                           @org.apache.ibatis.annotations.Param("batchNo") String batchNo,
                           @org.apache.ibatis.annotations.Param("quantity") java.math.BigDecimal quantity,
                           @org.apache.ibatis.annotations.Param("price") java.math.BigDecimal price,
                           @org.apache.ibatis.annotations.Param("amount") java.math.BigDecimal amount,
                           @org.apache.ibatis.annotations.Param("operName") String operName);

    /**
     * 生成盘亏出库单（source_type=CHECK，直接已审核状态）
     */
    int insertAdjustOutHead(@org.apache.ibatis.annotations.Param("outNo") String outNo,
                            @org.apache.ibatis.annotations.Param("sourceNo") String sourceNo,
                            @org.apache.ibatis.annotations.Param("totalAmount") java.math.BigDecimal totalAmount,
                            @org.apache.ibatis.annotations.Param("operName") String operName);

    Long selectAdjustOutIdByNo(@org.apache.ibatis.annotations.Param("outNo") String outNo);

    int insertAdjustOutItem(@org.apache.ibatis.annotations.Param("outId") Long outId,
                            @org.apache.ibatis.annotations.Param("rowNo") Integer rowNo,
                            @org.apache.ibatis.annotations.Param("productId") Long productId,
                            @org.apache.ibatis.annotations.Param("batchNo") String batchNo,
                            @org.apache.ibatis.annotations.Param("quantity") java.math.BigDecimal quantity,
                            @org.apache.ibatis.annotations.Param("price") java.math.BigDecimal price,
                            @org.apache.ibatis.annotations.Param("amount") java.math.BigDecimal amount,
                            @org.apache.ibatis.annotations.Param("operName") String operName);

    /**
     * 回写盘点明细的调整单号（gen_in_no / gen_out_no）
     */
    int updateCheckItemGenNos(@org.apache.ibatis.annotations.Param("itemId") Long itemId,
                              @org.apache.ibatis.annotations.Param("genInNo") String genInNo,
                              @org.apache.ibatis.annotations.Param("genOutNo") String genOutNo);
}
