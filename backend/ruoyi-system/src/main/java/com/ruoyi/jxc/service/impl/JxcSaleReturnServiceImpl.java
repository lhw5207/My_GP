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
import com.ruoyi.jxc.domain.JxcSaleReturnItem;
import com.ruoyi.jxc.mapper.JxcSaleReturnMapper;
import com.ruoyi.jxc.mapper.JxcStockCoreMapper;
import com.ruoyi.jxc.domain.JxcSaleReturn;
import com.ruoyi.jxc.service.IJxcSaleReturnService;

/**
 * 销售退货Service业务层处理
 * 业务约定：退货单「审核即执行」——审核时同事务完成：
 * ①库存回增（按原批次，成本沿用原销售成本快照还原）②流水（SALE_RETURN 入库）
 * ③应收红冲/退款（红字台账，仅会员单生成）
 * 已审核单据不可修改/删除。
 *
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcSaleReturnServiceImpl implements IJxcSaleReturnService 
{
    @Autowired
    private JxcSaleReturnMapper jxcSaleReturnMapper;

    @Autowired
    private JxcStockCoreMapper jxcStockCoreMapper;

    @Override
    public JxcSaleReturn selectJxcSaleReturnById(Long id)
    {
        return jxcSaleReturnMapper.selectJxcSaleReturnById(id);
    }

    @Override
    public List<JxcSaleReturn> selectJxcSaleReturnList(JxcSaleReturn jxcSaleReturn)
    {
        return jxcSaleReturnMapper.selectJxcSaleReturnList(jxcSaleReturn);
    }

    /**
     * 新增销售退货单（草稿；审核后才联动库存与应收）
     */
    @Transactional
    @Override
    public int insertJxcSaleReturn(JxcSaleReturn jxcSaleReturn)
    {
        jxcSaleReturn.setCreateTime(DateUtils.getNowDate());
        int rows = jxcSaleReturnMapper.insertJxcSaleReturn(jxcSaleReturn);
        insertJxcSaleReturnItem(jxcSaleReturn);
        return rows;
    }

    /**
     * 修改销售退货单（仅草稿态可改）
     */
    @Transactional
    @Override
    public int updateJxcSaleReturn(JxcSaleReturn jxcSaleReturn)
    {
        JxcSaleReturn exist = jxcSaleReturnMapper.selectJxcSaleReturnById(jxcSaleReturn.getId());
        if (exist == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!"0".equals(exist.getStatus()))
        {
            throw new ServiceException("已审核/已作废单据不可修改，纠错请走红字流程");
        }
        jxcSaleReturn.setUpdateTime(DateUtils.getNowDate());
        jxcSaleReturnMapper.deleteJxcSaleReturnItemByReturnId(jxcSaleReturn.getId());
        insertJxcSaleReturnItem(jxcSaleReturn);
        return jxcSaleReturnMapper.updateJxcSaleReturn(jxcSaleReturn);
    }

    /**
     * 批量删除销售退货单（仅草稿态可删）
     */
    @Transactional
    @Override
    public int deleteJxcSaleReturnByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            JxcSaleReturn exist = jxcSaleReturnMapper.selectJxcSaleReturnById(id);
            if (exist != null && !"0".equals(exist.getStatus()))
            {
                throw new ServiceException("单据[" + exist.getReturnNo() + "]已审核/已作废，不可删除");
            }
        }
        jxcSaleReturnMapper.deleteJxcSaleReturnItemByReturnIds(ids);
        return jxcSaleReturnMapper.deleteJxcSaleReturnByIds(ids);
    }

    @Transactional
    @Override
    public int deleteJxcSaleReturnById(Long id)
    {
        JxcSaleReturn exist = jxcSaleReturnMapper.selectJxcSaleReturnById(id);
        if (exist != null && !"0".equals(exist.getStatus()))
        {
            throw new ServiceException("单据[" + exist.getReturnNo() + "]已审核/已作废，不可删除");
        }
        jxcSaleReturnMapper.deleteJxcSaleReturnItemByReturnId(id);
        return jxcSaleReturnMapper.deleteJxcSaleReturnById(id);
    }

    /**
     * 审核销售退货单（审核即执行）
     * ①原批次回增库存（成本沿用原销售成本快照）②流水（SALE_RETURN 入库）③应收红冲/退款（红字台账）④状态更新
     */
    @Transactional
    @Override
    public int auditJxcSaleReturn(Long id, String operName)
    {
        JxcSaleReturn saleReturn = jxcSaleReturnMapper.selectJxcSaleReturnById(id);
        if (saleReturn == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!"0".equals(saleReturn.getStatus()))
        {
            throw new ServiceException("单据已审核或已作废，不可重复审核");
        }
        List<JxcSaleReturnItem> items = saleReturn.getJxcSaleReturnItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("退货单无明细，不可审核");
        }
        for (JxcSaleReturnItem item : items)
        {
            String batchNo = StringUtils.isEmpty(item.getBatchNo()) ? "" : item.getBatchNo();
            // ① 原批次回增库存（成本沿用原销售成本快照，实现"成本还原"）
            BigDecimal costPrice = item.getCostPrice() == null ? BigDecimal.ZERO : item.getCostPrice();
            jxcStockCoreMapper.upsertStockQtyIncrease(item.getProductId(), batchNo, null, null,
                    item.getQuantity(), costPrice, operName);
            // ② 库存流水（入库，正数变动 + 结存快照）
            BigDecimal balanceQty = jxcStockCoreMapper.selectStockQty(item.getProductId(), batchNo);
            jxcStockCoreMapper.insertStockFlow(item.getProductId(), batchNo, "1", "SALE_RETURN",
                    saleReturn.getId(), item.getId(), saleReturn.getReturnNo(),
                    item.getQuantity(), balanceQty, operName,
                    "销售退货回增（原批次：" + (batchNo.isEmpty() ? "非批次" : batchNo) + "）");
        }
        // ③ 应收红冲/退款（红字台账；仅会员单生成，散客为现金退款不产生应收）
        BigDecimal refund = saleReturn.getRefundAmount() == null ? BigDecimal.ZERO : saleReturn.getRefundAmount();
        if (refund.compareTo(BigDecimal.ZERO) <= 0)
        {
            refund = saleReturn.getTotalAmount();
        }
        if (saleReturn.getMemberId() != null && refund != null && refund.compareTo(BigDecimal.ZERO) > 0)
        {
            jxcStockCoreMapper.insertReceivable(saleReturn.getMemberId(), saleReturn.getReturnNo(), "SALE_RETURN",
                    refund.negate(), operName, "销售退货红冲应收/退款" +
                            (StringUtils.isNotEmpty(saleReturn.getSourceSaleNo()) ? "（原销售单：" + saleReturn.getSourceSaleNo() + "）" : ""));
        }
        // ④ 单据状态更新
        JxcSaleReturn update = new JxcSaleReturn();
        update.setId(id);
        update.setStatus("1");
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        return jxcSaleReturnMapper.updateJxcSaleReturn(update);
    }

    /**
     * 新增销售退货明细信息
     */
    public void insertJxcSaleReturnItem(JxcSaleReturn jxcSaleReturn)
    {
        List<JxcSaleReturnItem> jxcSaleReturnItemList = jxcSaleReturn.getJxcSaleReturnItemList();
        Long id = jxcSaleReturn.getId();
        if (StringUtils.isNotNull(jxcSaleReturnItemList))
        {
            List<JxcSaleReturnItem> list = new ArrayList<JxcSaleReturnItem>();
            for (JxcSaleReturnItem jxcSaleReturnItem : jxcSaleReturnItemList)
            {
                jxcSaleReturnItem.setReturnId(id);
                list.add(jxcSaleReturnItem);
            }
            if (list.size() > 0)
            {
                jxcSaleReturnMapper.batchJxcSaleReturnItem(list);
            }
        }
    }
}
