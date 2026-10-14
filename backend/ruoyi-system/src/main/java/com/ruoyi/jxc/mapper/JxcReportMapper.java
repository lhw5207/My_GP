package com.ruoyi.jxc.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 统计报表 Mapper（只读）
 * 数据来源：销售单/明细（含成本快照 cost_amount，可算毛利）、库存/商品档案
 *
 * @author vivi
 */
public interface JxcReportMapper
{
    /**
     * 销售日报（按天聚合：单数、销售额、成本、毛利）
     */
    List<Map<String, Object>> selectSaleDaily(@Param("beginDate") String beginDate,
                                              @Param("endDate") String endDate);

    /**
     * 商品销售排行（销量/销售额/成本/毛利，按销售额降序取前 N）
     */
    List<Map<String, Object>> selectSaleByProduct(@Param("beginDate") String beginDate,
                                                  @Param("endDate") String endDate);

    /**
     * 低库存预警（按商品聚合现存量/按批次汇总，低于或等于预警下限）
     */
    List<Map<String, Object>> selectLowStock();

    /**
     * 临期预警（批次维度：days 天内到期且仍有库存）
     */
    List<Map<String, Object>> selectNearExpiry(@Param("days") Integer days);
}
