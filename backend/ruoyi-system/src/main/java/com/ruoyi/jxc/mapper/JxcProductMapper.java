package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcProduct;

/**
 * 商品信息Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcProductMapper 
{
    /**
     * 查询商品信息
     * 
     * @param id 商品信息主键
     * @return 商品信息
     */
    public JxcProduct selectJxcProductById(Long id);

    /**
     * 查询商品信息列表
     * 
     * @param jxcProduct 商品信息
     * @return 商品信息集合
     */
    public List<JxcProduct> selectJxcProductList(JxcProduct jxcProduct);

    /**
     * 新增商品信息
     * 
     * @param jxcProduct 商品信息
     * @return 结果
     */
    public int insertJxcProduct(JxcProduct jxcProduct);

    /**
     * 修改商品信息
     * 
     * @param jxcProduct 商品信息
     * @return 结果
     */
    public int updateJxcProduct(JxcProduct jxcProduct);

    /**
     * 删除商品信息
     * 
     * @param id 商品信息主键
     * @return 结果
     */
    public int deleteJxcProductById(Long id);

    /**
     * 批量删除商品信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcProductByIds(Long[] ids);
}
