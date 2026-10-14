package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcProductCategory;

/**
 * 商品分类Mapper接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface JxcProductCategoryMapper 
{
    /**
     * 查询商品分类
     * 
     * @param id 商品分类主键
     * @return 商品分类
     */
    public JxcProductCategory selectJxcProductCategoryById(Long id);

    /**
     * 查询商品分类列表
     * 
     * @param jxcProductCategory 商品分类
     * @return 商品分类集合
     */
    public List<JxcProductCategory> selectJxcProductCategoryList(JxcProductCategory jxcProductCategory);

    /**
     * 新增商品分类
     * 
     * @param jxcProductCategory 商品分类
     * @return 结果
     */
    public int insertJxcProductCategory(JxcProductCategory jxcProductCategory);

    /**
     * 修改商品分类
     * 
     * @param jxcProductCategory 商品分类
     * @return 结果
     */
    public int updateJxcProductCategory(JxcProductCategory jxcProductCategory);

    /**
     * 删除商品分类
     * 
     * @param id 商品分类主键
     * @return 结果
     */
    public int deleteJxcProductCategoryById(Long id);

    /**
     * 批量删除商品分类
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJxcProductCategoryByIds(Long[] ids);
}
