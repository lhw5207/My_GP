package com.ruoyi.jxc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.jxc.mapper.JxcProductMapper;
import com.ruoyi.jxc.domain.JxcProduct;
import com.ruoyi.jxc.service.IJxcProductService;

/**
 * 商品信息Service业务层处理
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcProductServiceImpl implements IJxcProductService 
{
    @Autowired
    private JxcProductMapper jxcProductMapper;

    /**
     * 查询商品信息
     * 
     * @param id 商品信息主键
     * @return 商品信息
     */
    @Override
    public JxcProduct selectJxcProductById(Long id)
    {
        return jxcProductMapper.selectJxcProductById(id);
    }

    /**
     * 查询商品信息列表
     * 
     * @param jxcProduct 商品信息
     * @return 商品信息
     */
    @Override
    public List<JxcProduct> selectJxcProductList(JxcProduct jxcProduct)
    {
        return jxcProductMapper.selectJxcProductList(jxcProduct);
    }

    /**
     * 新增商品信息
     * 
     * @param jxcProduct 商品信息
     * @return 结果
     */
    @Override
    public int insertJxcProduct(JxcProduct jxcProduct)
    {
        jxcProduct.setCreateTime(DateUtils.getNowDate());
        return jxcProductMapper.insertJxcProduct(jxcProduct);
    }

    /**
     * 修改商品信息
     * 
     * @param jxcProduct 商品信息
     * @return 结果
     */
    @Override
    public int updateJxcProduct(JxcProduct jxcProduct)
    {
        jxcProduct.setUpdateTime(DateUtils.getNowDate());
        return jxcProductMapper.updateJxcProduct(jxcProduct);
    }

    /**
     * 批量删除商品信息
     * 
     * @param ids 需要删除的商品信息主键
     * @return 结果
     */
    @Override
    public int deleteJxcProductByIds(Long[] ids)
    {
        return jxcProductMapper.deleteJxcProductByIds(ids);
    }

    /**
     * 删除商品信息信息
     * 
     * @param id 商品信息主键
     * @return 结果
     */
    @Override
    public int deleteJxcProductById(Long id)
    {
        return jxcProductMapper.deleteJxcProductById(id);
    }
}
