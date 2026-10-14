package com.ruoyi.jxc.service;

import java.util.List;
import com.ruoyi.jxc.domain.JxcSupplier;

/**
 * 供应商管理Service接口
 * 
 * @author vivi
 * @date 2026-10-14
 */
public interface IJxcSupplierService 
{
    /**
     * 查询供应商管理
     * 
     * @param id 供应商管理主键
     * @return 供应商管理
     */
    public JxcSupplier selectJxcSupplierById(Long id);

    /**
     * 查询供应商管理列表
     * 
     * @param jxcSupplier 供应商管理
     * @return 供应商管理集合
     */
    public List<JxcSupplier> selectJxcSupplierList(JxcSupplier jxcSupplier);

    /**
     * 新增供应商管理
     * 
     * @param jxcSupplier 供应商管理
     * @return 结果
     */
    public int insertJxcSupplier(JxcSupplier jxcSupplier);

    /**
     * 修改供应商管理
     * 
     * @param jxcSupplier 供应商管理
     * @return 结果
     */
    public int updateJxcSupplier(JxcSupplier jxcSupplier);

    /**
     * 批量删除供应商管理
     * 
     * @param ids 需要删除的供应商管理主键集合
     * @return 结果
     */
    public int deleteJxcSupplierByIds(Long[] ids);

    /**
     * 删除供应商管理信息
     * 
     * @param id 供应商管理主键
     * @return 结果
     */
    public int deleteJxcSupplierById(Long id);
}
