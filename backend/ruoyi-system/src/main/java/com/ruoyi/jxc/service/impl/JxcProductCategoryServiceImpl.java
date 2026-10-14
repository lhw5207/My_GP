package com.ruoyi.jxc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.jxc.mapper.JxcProductCategoryMapper;
import com.ruoyi.jxc.domain.JxcProductCategory;
import com.ruoyi.jxc.service.IJxcProductCategoryService;

/**
 * 商品分类Service业务层处理
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcProductCategoryServiceImpl implements IJxcProductCategoryService 
{
    @Autowired
    private JxcProductCategoryMapper jxcProductCategoryMapper;

    /**
     * 查询商品分类
     * 
     * @param id 商品分类主键
     * @return 商品分类
     */
    @Override
    public JxcProductCategory selectJxcProductCategoryById(Long id)
    {
        return jxcProductCategoryMapper.selectJxcProductCategoryById(id);
    }

    /**
     * 查询商品分类列表
     * 
     * @param jxcProductCategory 商品分类
     * @return 商品分类
     */
    @Override
    public List<JxcProductCategory> selectJxcProductCategoryList(JxcProductCategory jxcProductCategory)
    {
        return jxcProductCategoryMapper.selectJxcProductCategoryList(jxcProductCategory);
    }

    /**
     * 新增商品分类
     * 
     * @param jxcProductCategory 商品分类
     * @return 结果
     */
    @Override
    public int insertJxcProductCategory(JxcProductCategory jxcProductCategory)
    {
        jxcProductCategory.setCreateTime(DateUtils.getNowDate());
        return jxcProductCategoryMapper.insertJxcProductCategory(jxcProductCategory);
    }

    /**
     * 修改商品分类
     * 
     * @param jxcProductCategory 商品分类
     * @return 结果
     */
    @Override
    public int updateJxcProductCategory(JxcProductCategory jxcProductCategory)
    {
        jxcProductCategory.setUpdateTime(DateUtils.getNowDate());
        return jxcProductCategoryMapper.updateJxcProductCategory(jxcProductCategory);
    }

    /**
     * 批量删除商品分类
     * 
     * @param ids 需要删除的商品分类主键
     * @return 结果
     */
    @Override
    public int deleteJxcProductCategoryByIds(Long[] ids)
    {
        return jxcProductCategoryMapper.deleteJxcProductCategoryByIds(ids);
    }

    /**
     * 删除商品分类信息
     * 
     * @param id 商品分类主键
     * @return 结果
     */
    @Override
    public int deleteJxcProductCategoryById(Long id)
    {
        return jxcProductCategoryMapper.deleteJxcProductCategoryById(id);
    }
}
