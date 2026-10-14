package com.ruoyi.jxc.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcPurchaseReturnItem;
import com.ruoyi.jxc.mapper.JxcPurchaseReturnMapper;
import com.ruoyi.jxc.mapper.JxcStockCoreMapper;
import com.ruoyi.jxc.domain.JxcPurchaseReturn;
import com.ruoyi.jxc.service.IJxcPurchaseReturnService;

/**
 * 采购退货Service业务层处理
 * 业务约定：退货单「审核即执行」——审核时同事务完成：
 * ①库存扣减（按原批次原子扣减，不足拒绝）②流水（PURCHASE_RETURN 出库）③应付红冲（红字台账）
 * 已审核单据不可修改/删除，纠错走红字流程。
 *
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcPurchaseReturnServiceImpl implements IJxcPurchaseReturnService 
{
    @Autowired
    private JxcPurchaseReturnMapper jxcPurchaseReturnMapper;

    @Autowired
    private JxcStockCoreMapper jxcStockCoreMapper;

    @Override
    public JxcPurchaseReturn selectJxcPurchaseReturnById(Long id)
    {
        return jxcPurchaseReturnMapper.selectJxcPurchaseReturnById(id);
    }

    @Override
    public List<JxcPurchaseReturn> selectJxcPurchaseReturnList(JxcPurchaseReturn jxcPurchaseReturn)
    {
        return jxcPurchaseReturnMapper.selectJxcPurchaseReturnList(jxcPurchaseReturn);
    }

    /**
     * 新增采购退货单（草稿；审核后才联动库存与应付）
     */
    @Transactional
    @Override
    public int insertJxcPurchaseReturn(JxcPurchaseReturn jxcPurchaseReturn)
    {
        jxcPurchaseReturn.setCreateTime(DateUtils.getNowDate());
        int rows = jxcPurchaseReturnMapper.insertJxcPurchaseReturn(jxcPurchaseReturn);
        insertJxcPurchaseReturnItem(jxcPurchaseReturn);
        return rows;
    }

    /**
     * 修改采购退货单（仅草稿态可改）
     */
    @Transactional
    @Override
    public int updateJxcPurchaseReturn(JxcPurchaseReturn jxcPurchaseReturn)
    {
        JxcPurchaseReturn exist = jxcPurchaseReturnMapper.selectJxcPurchaseReturnById(jxcPurchaseReturn.getId());
        if (exist == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!"0".equals(exist.getStatus()))
        {
            throw new ServiceException("已审核/已作废单据不可修改，纠错请走红字流程");
        }
        jxcPurchaseReturn.setUpdateTime(DateUtils.getNowDate());
        jxcPurchaseReturnMapper.deleteJxcPurchaseReturnItemByReturnId(jxcPurchaseReturn.getId());
        insertJxcPurchaseReturnItem(jxcPurchaseReturn);
        return jxcPurchaseReturnMapper.updateJxcPurchaseReturn(jxcPurchaseReturn);
    }

    /**
     * 批量删除采购退货单（仅草稿态可删）
     */
    @Transactional
    @Override
    public int deleteJxcPurchaseReturnByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            JxcPurchaseReturn exist = jxcPurchaseReturnMapper.selectJxcPurchaseReturnById(id);
            if (exist != null && !"0".equals(exist.getStatus()))
            {
                throw new ServiceException("单据[" + exist.getReturnNo() + "]已审核/已作废，不可删除");
            }
        }
        jxcPurchaseReturnMapper.deleteJxcPurchaseReturnItemByReturnIds(ids);
        return jxcPurchaseReturnMapper.deleteJxcPurchaseReturnByIds(ids);
    }

    @Transactional
    @Override
    public int deleteJxcPurchaseReturnById(Long id)
    {
        JxcPurchaseReturn exist = jxcPurchaseReturnMapper.selectJxcPurchaseReturnById(id);
        if (exist != null && !"0".equals(exist.getStatus()))
        {
            throw new ServiceException("单据[" + exist.getReturnNo() + "]已审核/已作废，不可删除");
        }
        jxcPurchaseReturnMapper.deleteJxcPurchaseReturnItemByReturnId(id);
        return jxcPurchaseReturnMapper.deleteJxcPurchaseReturnById(id);
    }

    /**
     * 审核采购退货单（审核即执行）
     * ①库存扣减（原批次原子扣减，不足拒绝）②流水（PURCHASE_RETURN）③应付红冲（红字台账）④状态更新
     */
    @Transactional
    @Override
    public int auditJxcPurchaseReturn(Long id, String operName)
    {
        JxcPurchaseReturn purchaseReturn = jxcPurchaseReturnMapper.selectJxcPurchaseReturnById(id);
        if (purchaseReturn == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!"0".equals(purchaseReturn.getStatus()))
        {
            throw new ServiceException("单据已审核或已作废，不可重复审核");
        }
        List<JxcPurchaseReturnItem> items = purchaseReturn.getJxcPurchaseReturnItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("退货单无明细，不可审核");
        }
        for (JxcPurchaseReturnItem item : items)
        {
            String batchNo = StringUtils.isEmpty(item.getBatchNo()) ? "" : item.getBatchNo();
            // ① 库存原子扣减（不足则整单回滚）
            int affected = jxcStockCoreMapper.deductStockQty(item.getProductId(), batchNo, item.getQuantity(), operName);
            if (affected == 0)
            {
                throw new ServiceException("商品[ID:" + item.getProductId() + "]批次[" + batchNo + "]库存不足，无法退货");
            }
            // ② 库存流水（出库，负数变动 + 结存快照）
            BigDecimal balanceQty = jxcStockCoreMapper.selectStockQty(item.getProductId(), batchNo);
            jxcStockCoreMapper.insertStockFlow(item.getProductId(), batchNo, "2", "PURCHASE_RETURN",
                    purchaseReturn.getId(), item.getId(), purchaseReturn.getReturnNo(),
                    item.getQuantity().negate(), balanceQty, operName, "采购退货出库");
        }
        // ③ 应付红冲（红字台账，负数金额保留完整审计痕迹）
        jxcStockCoreMapper.insertPayable(purchaseReturn.getSupplierId(), purchaseReturn.getReturnNo(), "PURCHASE_RETURN",
                purchaseReturn.getTotalAmount().negate(), operName, "采购退货红冲应付" +
                        (StringUtils.isNotEmpty(purchaseReturn.getSourceInNo()) ? "（原入库单：" + purchaseReturn.getSourceInNo() + "）" : ""));
        // ④ 单据状态更新
        JxcPurchaseReturn update = new JxcPurchaseReturn();
        update.setId(id);
        update.setStatus("1");
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        return jxcPurchaseReturnMapper.updateJxcPurchaseReturn(update);
    }

    /**
     * 新增采购退货明细信息
     */
    public void insertJxcPurchaseReturnItem(JxcPurchaseReturn jxcPurchaseReturn)
    {
        List<JxcPurchaseReturnItem> jxcPurchaseReturnItemList = jxcPurchaseReturn.getJxcPurchaseReturnItemList();
        Long id = jxcPurchaseReturn.getId();
        if (StringUtils.isNotNull(jxcPurchaseReturnItemList))
        {
            List<JxcPurchaseReturnItem> list = new ArrayList<JxcPurchaseReturnItem>();
            for (JxcPurchaseReturnItem jxcPurchaseReturnItem : jxcPurchaseReturnItemList)
            {
                jxcPurchaseReturnItem.setReturnId(id);
                list.add(jxcPurchaseReturnItem);
            }
            if (list.size() > 0)
            {
                jxcPurchaseReturnMapper.batchJxcPurchaseReturnItem(list);
            }
        }
    }
}
