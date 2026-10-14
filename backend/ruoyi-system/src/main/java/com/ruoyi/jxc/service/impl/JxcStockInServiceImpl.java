package com.ruoyi.jxc.service.impl;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcStockInItem;
import com.ruoyi.jxc.mapper.JxcStockCoreMapper;
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

    @Autowired
    private JxcStockCoreMapper jxcStockCoreMapper;

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
     * 修改采购入库单（仅草稿态可改；已审核单纠错走红字/退货单）
     */
    @Transactional
    @Override
    public int updateJxcStockIn(JxcStockIn jxcStockIn)
    {
        JxcStockIn exist = jxcStockInMapper.selectJxcStockInById(jxcStockIn.getId());
        if (exist == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!"0".equals(exist.getStatus()))
        {
            throw new ServiceException("已审核/已作废单据不可修改，纠错请开红字或退货单");
        }
        jxcStockIn.setUpdateTime(DateUtils.getNowDate());
        jxcStockInMapper.deleteJxcStockInItemByInId(jxcStockIn.getId());
        insertJxcStockInItem(jxcStockIn);
        return jxcStockInMapper.updateJxcStockIn(jxcStockIn);
    }

    /**
     * 批量删除采购入库单（仅草稿态可删）
     */
    @Transactional
    @Override
    public int deleteJxcStockInByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            JxcStockIn exist = jxcStockInMapper.selectJxcStockInById(id);
            if (exist != null && !"0".equals(exist.getStatus()))
            {
                throw new ServiceException("单据[" + exist.getInNo() + "]已审核/已作废，不可删除");
            }
        }
        jxcStockInMapper.deleteJxcStockInItemByInIds(ids);
        return jxcStockInMapper.deleteJxcStockInByIds(ids);
    }

    /**
     * 删除采购入库单信息（仅草稿态可删）
     */
    @Transactional
    @Override
    public int deleteJxcStockInById(Long id)
    {
        JxcStockIn exist = jxcStockInMapper.selectJxcStockInById(id);
        if (exist != null && !"0".equals(exist.getStatus()))
        {
            throw new ServiceException("单据[" + exist.getInNo() + "]已审核/已作废，不可删除");
        }
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

    /**
     * 审核采购入库单（审核即执行）
     * 同事务完成：①现存量增加（批次 upsert）②库存流水（批次结存快照）③应付台账生成 ④单据状态更新
     */
    @Transactional
    @Override
    public int auditJxcStockIn(Long id, String operName)
    {
        JxcStockIn stockIn = jxcStockInMapper.selectJxcStockInById(id);
        if (stockIn == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!"0".equals(stockIn.getStatus()))
        {
            throw new ServiceException("单据已审核或已作废，不可重复审核");
        }
        List<JxcStockInItem> items = stockIn.getJxcStockInItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("入库单无明细，不可审核");
        }
        for (JxcStockInItem item : items)
        {
            // 批次号空串约定（非批次商品统一空串，禁止 NULL）
            String batchNo = StringUtils.isEmpty(item.getBatchNo()) ? "" : item.getBatchNo();
            String productionDate = item.getProductionDate() == null ? null
                    : DateUtils.parseDateToStr("yyyy-MM-dd", item.getProductionDate());
            String expiryDate = item.getExpiryDate() == null ? null
                    : DateUtils.parseDateToStr("yyyy-MM-dd", item.getExpiryDate());
            // ① 现存量增加（批次维度 upsert；同批次入库成本以最新入库价为准）
            jxcStockCoreMapper.upsertStockQtyIncrease(item.getProductId(), batchNo, productionDate,
                    expiryDate, item.getQuantity(), item.getPrice(), operName);
            // ② 库存流水（变动后批次结存快照）
            BigDecimal balanceQty = jxcStockCoreMapper.selectStockQty(item.getProductId(), batchNo);
            jxcStockCoreMapper.insertStockFlow(item.getProductId(), batchNo, "1", "PURCHASE_IN",
                    stockIn.getId(), item.getId(), stockIn.getInNo(), item.getQuantity(), balanceQty,
                    operName, "采购入库审核");
        }
        // ③ 应付台账（采购入库且有供应商时生成；盘盈入库不生成）
        if ("PURCHASE".equals(stockIn.getSourceType()) && stockIn.getSupplierId() != null)
        {
            jxcStockCoreMapper.insertPayable(stockIn.getSupplierId(), stockIn.getInNo(), "STOCK_IN",
                    stockIn.getTotalAmount(), operName, "采购入库生成应付");
        }
        // ④ 采购订单回写：累加 order_item.received_qty，推进订单状态（2 部分执行 / 3 已执行）
        if ("PURCHASE".equals(stockIn.getSourceType()) && StringUtils.isNotEmpty(stockIn.getSourceNo()))
        {
            Long orderId = jxcStockInMapper.selectPurchaseOrderIdByNo(stockIn.getSourceNo());
            if (orderId != null)
            {
                for (JxcStockInItem item : items)
                {
                    jxcStockInMapper.updateOrderItemReceivedQty(orderId, item.getProductId(), item.getQuantity());
                }
                int unfinished = jxcStockInMapper.countOrderUnfinishedItems(orderId);
                jxcStockInMapper.updatePurchaseOrderStatus(orderId, unfinished > 0 ? "2" : "3");
            }
        }
        // ⑤ 单据状态更新（审核即执行；已审单不可作废，纠错走红字/退货单）
        JxcStockIn update = new JxcStockIn();
        update.setId(id);
        update.setStatus("1");
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        return jxcStockInMapper.updateJxcStockIn(update);
    }
}
