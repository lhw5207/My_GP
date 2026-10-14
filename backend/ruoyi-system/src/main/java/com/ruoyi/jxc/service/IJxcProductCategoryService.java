package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcProductCategory;

/**
 * 商品分类Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcProductCategoryService 
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
     * 批量删除商品分类
     * 
     * @param ids 需要删除的商品分类主键集合
     * @return 结果
     */
    public int deleteJxcProductCategoryByIds(Long[] ids);

    /**
     * 删除商品分类信息
     * 
     * @param id 商品分类主键
     * @return 结果
     */
    public int deleteJxcProductCategoryById(Long id);
}
