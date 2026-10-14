package com.ruoyi.jxc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcStockInItem;
import com.ruoyi.jxc.mapper.JxcStockInMapper;
import com.ruoyi.jxc.domain.JxcStockIn;
import com.ruoyi.jxc.service.IJxcStockInService;

/**
 * 采购入库单Service业务层处理
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcStockInServiceImpl implements IJxcStockInService 
{
    @Autowired
    private JxcStockInMapper jxcStockInMapper;

    /**
     * 查询采购入库单
     * 
     * @param id 采购入库单主键
     * @return 采购入库单
     */
    @Override
    public JxcStockIn selectJxcStockInById(Long id)
    {
        return jxcStockInMapper.selectJxcStockInById(id);
    }

    /**
     * 查询采购入库单列表
     * 
     * @param jxcStockIn 采购入库单
     * @return 采购入库单
     */
    @Override
    public List<JxcStockIn> selectJxcStockInList(JxcStockIn jxcStockIn)
    {
        return jxcStockInMapper.selectJxcStockInList(jxcStockIn);
    }

    /**
     * 新增采购入库单
     * 
     * @param jxcStockIn 采购入库单
     * @return 结果
     */
    @Transactional
    @Override
    public int insertJxcStockIn(JxcStockIn jxcStockIn)
    {
        jxcStockIn.setCreateTime(DateUtils.getNowDate());
        int rows = jxcStockInMapper.insertJxcStockIn(jxcStockIn);
        insertJxcStockInItem(jxcStockIn);
        return rows;
    }

    /**
     * 修改采购入库单
     * 
     * @param jxcStockIn 采购入库单
     * @return 结果
     */
    @Transactional
    @Override
    public int updateJxcStockIn(JxcStockIn jxcStockIn)
    {
        jxcStockIn.setUpdateTime(DateUtils.getNowDate());
        jxcStockInMapper.deleteJxcStockInItemByInId(jxcStockIn.getId());
        insertJxcStockInItem(jxcStockIn);
        return jxcStockInMapper.updateJxcStockIn(jxcStockIn);
    }

    /**
     * 批量删除采购入库单
     * 
     * @param ids 需要删除的采购入库单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteJxcStockInByIds(Long[] ids)
    {
        jxcStockInMapper.deleteJxcStockInItemByInIds(ids);
        return jxcStockInMapper.deleteJxcStockInByIds(ids);
    }

    /**
     * 删除采购入库单信息
     * 
     * @param id 采购入库单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteJxcStockInById(Long id)
    {
        jxcStockInMapper.deleteJxcStockInItemByInId(id);
        return jxcStockInMapper.deleteJxcStockInById(id);
    }

    /**
     * 新增入库单明细信息
     * 
     * @param jxcStockIn 采购入库单对象
     */
    public void insertJxcStockInItem(JxcStockIn jxcStockIn)
    {
        List<JxcStockInItem> jxcStockInItemList = jxcStockIn.getJxcStockInItemList();
        Long id = jxcStockIn.getId();
        if (StringUtils.isNotNull(jxcStockInItemList))
        {
            List<JxcStockInItem> list = new ArrayList<JxcStockInItem>();
            for (JxcStockInItem jxcStockInItem : jxcStockInItemList)
            {
                jxcStockInItem.setInId(id);
                list.add(jxcStockInItem);
            }
            if (list.size() > 0)
            {
                jxcStockInMapper.batchJxcStockInItem(list);
            }
        }
    }
}
