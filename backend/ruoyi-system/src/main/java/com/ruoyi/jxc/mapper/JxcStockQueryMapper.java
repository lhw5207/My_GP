package com.ruoyi.jxc.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 库存查询 Mapper（只读：现存量查询 / 流水台账查询）
 *
 * @author vivi
 */
public interface JxcStockQueryMapper
{
    /**
     * 现货存量查询（联表商品，含临期天数与库存金额）
     *
     * @param productId 商品ID（可空）
     * @param batchNo 批次号（可空，模糊）
     * @param nearExpiryDays 临期天数阈值（可空；不为空且 >=0 时仅查 expiry_date 距今不超过该天数的批次）
     * @param includeZero 是否包含零库存行（1 包含 / 其他不含）
     */
    List<Map<String, Object>> selectStockList(@Param("productId") Long productId,
                                              @Param("batchNo") String batchNo,
                                              @Param("nearExpiryDays") Integer nearExpiryDays,
                                              @Param("includeZero") String includeZero);

    /**
     * 库存流水台账查询（联表商品）
     *
     * @param productId 商品ID（可空）
     * @param batchNo 批次号（可空，模糊）
     * @param bizType 业务类型（可空，字典 jxc_biz_type）
     * @param bizNo 来源单号（可空，模糊）
     */
    List<Map<String, Object>> selectStockFlowList(@Param("productId") Long productId,
                                                  @Param("batchNo") String batchNo,
                                                  @Param("bizType") String bizType,
                                                  @Param("bizNo") String bizNo);
}
