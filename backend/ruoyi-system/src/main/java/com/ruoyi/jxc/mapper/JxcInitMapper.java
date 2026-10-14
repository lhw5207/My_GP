package com.ruoyi.jxc.mapper;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 期初数据导入 Mapper
 *
 * @author vivi
 */
public interface JxcInitMapper
{
    /**
     * 按商品编码查询商品档案（含是否批次管理、保质期天数）
     */
    Map<String, Object> selectProductByCode(@Param("productCode") String productCode);
}
