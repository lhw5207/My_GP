package com.ruoyi.jxc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.jxc.mapper.JxcSupplierMapper;
import com.ruoyi.jxc.domain.JxcSupplier;
import com.ruoyi.jxc.service.IJxcSupplierService;

/**
 * 供应商管理Service业务层处理
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcSupplierServiceImpl implements IJxcSupplierService 
{
    @Autowired
    private JxcSupplierMapper jxcSupplierMapper;

    /**
     * 查询供应商管理
     * 
     * @param id 供应商管理主键
     * @return 供应商管理
     */
    @Override
    public JxcSupplier selectJxcSupplierById(Long id)
    {
        return jxcSupplierMapper.selectJxcSupplierById(id);
    }

    /**
     * 查询供应商管理列表
     * 
     * @param jxcSupplier 供应商管理
     * @return 供应商管理
     */
    @Override
    public List<JxcSupplier> selectJxcSupplierList(JxcSupplier jxcSupplier)
    {
        return jxcSupplierMapper.selectJxcSupplierList(jxcSupplier);
    }

    /**
     * 新增供应商管理
     * 
     * @param jxcSupplier 供应商管理
     * @return 结果
     */
    @Override
    public int insertJxcSupplier(JxcSupplier jxcSupplier)
    {
        jxcSupplier.setCreateTime(DateUtils.getNowDate());
        return jxcSupplierMapper.insertJxcSupplier(jxcSupplier);
    }

    /**
     * 修改供应商管理
     * 
     * @param jxcSupplier 供应商管理
     * @return 结果
     */
    @Override
    public int updateJxcSupplier(JxcSupplier jxcSupplier)
    {
        jxcSupplier.setUpdateTime(DateUtils.getNowDate());
        return jxcSupplierMapper.updateJxcSupplier(jxcSupplier);
    }

    /**
     * 批量删除供应商管理
     * 
     * @param ids 需要删除的供应商管理主键
     * @return 结果
     */
    @Override
    public int deleteJxcSupplierByIds(Long[] ids)
    {
        return jxcSupplierMapper.deleteJxcSupplierByIds(ids);
    }

    /**
     * 删除供应商管理信息
     * 
     * @param id 供应商管理主键
     * @return 结果
     */
    @Override
    public int deleteJxcSupplierById(Long id)
    {
        return jxcSupplierMapper.deleteJxcSupplierById(id);
    }
}
