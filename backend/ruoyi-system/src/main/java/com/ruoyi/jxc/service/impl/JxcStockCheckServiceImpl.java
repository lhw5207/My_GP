package com.ruoyi.jxc.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcStockCheckItem;
import com.ruoyi.jxc.mapper.JxcStockCheckMapper;
import com.ruoyi.jxc.mapper.JxcStockCoreMapper;
import com.ruoyi.jxc.domain.JxcStockCheck;
import com.ruoyi.jxc.service.IJxcStockCheckService;

/**
 * 库存盘点Service业务层处理
 * 业务约定：盘点单「审核即执行」——审核时对差异行自动生成调整单并联动库存：
 * ①盘盈行 → 生成盘盈入库单（source_type=CHECK，直接已审核）+ 库存增加 + 流水（CHECK_IN）
 * ②盘亏行 → 生成盘亏出库单（source_type=CHECK，直接已审核）+ 库存原子扣减 + 流水（CHECK_OUT）
 * ③调整单号回写盘点明细 gen_in_no / gen_out_no（可追溯）
 * 成本口径：盘盈盘亏均取商品档案参考进价快照。
 *
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcStockCheckServiceImpl implements IJxcStockCheckService 
{
    @Autowired
    private JxcStockCheckMapper jxcStockCheckMapper;

    @Autowired
    private JxcStockCoreMapper jxcStockCoreMapper;

    @Override
    public JxcStockCheck selectJxcStockCheckById(Long id)
    {
        return jxcStockCheckMapper.selectJxcStockCheckById(id);
    }

    @Override
    public List<JxcStockCheck> selectJxcStockCheckList(JxcStockCheck jxcStockCheck)
    {
        return jxcStockCheckMapper.selectJxcStockCheckList(jxcStockCheck);
    }

    /**
     * 新增盘点单（草稿；审核后生成调整单并联动库存）
     */
    @Transactional
    @Override
    public int insertJxcStockCheck(JxcStockCheck jxcStockCheck)
    {
        jxcStockCheck.setCreateTime(DateUtils.getNowDate());
        int rows = jxcStockCheckMapper.insertJxcStockCheck(jxcStockCheck);
        insertJxcStockCheckItem(jxcStockCheck);
        return rows;
    }

    /**
     * 修改盘点单（仅草稿态可改）
     */
    @Transactional
    @Override
    public int updateJxcStockCheck(JxcStockCheck jxcStockCheck)
    {
        JxcStockCheck exist = jxcStockCheckMapper.selectJxcStockCheckById(jxcStockCheck.getId());
        if (exist == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!"0".equals(exist.getStatus()))
        {
            throw new ServiceException("已审核/已作废单据不可修改");
        }
        jxcStockCheck.setUpdateTime(DateUtils.getNowDate());
        jxcStockCheckMapper.deleteJxcStockCheckItemByCheckId(jxcStockCheck.getId());
        insertJxcStockCheckItem(jxcStockCheck);
        return jxcStockCheckMapper.updateJxcStockCheck(jxcStockCheck);
    }

    /**
     * 批量删除盘点单（仅草稿态可删）
     */
    @Transactional
    @Override
    public int deleteJxcStockCheckByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            JxcStockCheck exist = jxcStockCheckMapper.selectJxcStockCheckById(id);
            if (exist != null && !"0".equals(exist.getStatus()))
            {
                throw new ServiceException("单据[" + exist.getCheckNo() + "]已审核，不可删除");
            }
        }
        jxcStockCheckMapper.deleteJxcStockCheckItemByCheckIds(ids);
        return jxcStockCheckMapper.deleteJxcStockCheckByIds(ids);
    }

    @Transactional
    @Override
    public int deleteJxcStockCheckById(Long id)
    {
        JxcStockCheck exist = jxcStockCheckMapper.selectJxcStockCheckById(id);
        if (exist != null && !"0".equals(exist.getStatus()))
        {
            throw new ServiceException("单据[" + exist.getCheckNo() + "]已审核，不可删除");
        }
        jxcStockCheckMapper.deleteJxcStockCheckItemByCheckId(id);
        return jxcStockCheckMapper.deleteJxcStockCheckById(id);
    }

    /**
     * 审核盘点单（审核即执行）
     * 盘盈生成入库单、盘亏生成出库单；库存、流水、调整单号回写同事务完成。
     */
    @Transactional
    @Override
    public int auditJxcStockCheck(Long id, String operName)
    {
        JxcStockCheck stockCheck = jxcStockCheckMapper.selectJxcStockCheckById(id);
        if (stockCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!"0".equals(stockCheck.getStatus()))
        {
            throw new ServiceException("单据已审核或已作废，不可重复审核");
        }
        List<JxcStockCheckItem> items = stockCheck.getJxcStockCheckItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("盘点单无明细，不可审核");
        }

        String checkNo = stockCheck.getCheckNo();
        String inNo = checkNo + "-IN";
        String outNo = checkNo + "-OUT";

        // 分离盘盈 / 盘亏行，计算调整单总额（成本=商品档案参考进价快照）
        List<JxcStockCheckItem> gainItems = new ArrayList<JxcStockCheckItem>();
        List<JxcStockCheckItem> lossItems = new ArrayList<JxcStockCheckItem>();
        BigDecimal inTotal = BigDecimal.ZERO;
        BigDecimal outTotal = BigDecimal.ZERO;
        for (JxcStockCheckItem item : items)
        {
            BigDecimal diff = item.getDiffQty() == null ? BigDecimal.ZERO : item.getDiffQty();
            item.setRemark(item.getRemark());
            if (diff.compareTo(BigDecimal.ZERO) > 0)
            {
                gainItems.add(item);
                BigDecimal price = jxcStockCheckMapper.selectProductPurchasePrice(item.getProductId());
                if (price == null) { price = BigDecimal.ZERO; }
                inTotal = inTotal.add(diff.multiply(price).setScale(2, RoundingMode.HALF_UP));
            }
            else if (diff.compareTo(BigDecimal.ZERO) < 0)
            {
                lossItems.add(item);
                BigDecimal price = jxcStockCheckMapper.selectProductPurchasePrice(item.getProductId());
                if (price == null) { price = BigDecimal.ZERO; }
                outTotal = outTotal.add(diff.abs().multiply(price).setScale(2, RoundingMode.HALF_UP));
            }
        }

        // ① 盘盈 → 盘盈入库单（直接已审核）+ 库存增加 + 流水 + 回写 gen_in_no
        if (!gainItems.isEmpty())
        {
            jxcStockCheckMapper.insertAdjustInHead(inNo, checkNo, inTotal, operName);
            Long inId = jxcStockCheckMapper.selectAdjustInIdByNo(inNo);
            int rowNo = 1;
            for (JxcStockCheckItem item : gainItems)
            {
                String batchNo = StringUtils.isEmpty(item.getBatchNo()) ? "" : item.getBatchNo();
                BigDecimal diff = item.getDiffQty();
                BigDecimal price = jxcStockCheckMapper.selectProductPurchasePrice(item.getProductId());
                if (price == null) { price = BigDecimal.ZERO; }
                BigDecimal amount = diff.multiply(price).setScale(2, RoundingMode.HALF_UP);
                jxcStockCheckMapper.insertAdjustInItem(inId, rowNo, item.getProductId(), batchNo, diff, price, amount, operName);
                jxcStockCoreMapper.upsertStockQtyIncrease(item.getProductId(), batchNo, null, null, diff, price, operName);
                BigDecimal balanceQty = jxcStockCoreMapper.selectStockQty(item.getProductId(), batchNo);
                jxcStockCoreMapper.insertStockFlow(item.getProductId(), batchNo, "1", "CHECK_IN",
                        stockCheck.getId(), item.getId(), inNo, diff, balanceQty, operName, "盘点盘盈入库");
                jxcStockCheckMapper.updateCheckItemGenNos(item.getId(), inNo, null);
                rowNo++;
            }
        }

        // ② 盘亏 → 盘亏出库单（直接已审核）+ 库存原子扣减 + 流水 + 回写 gen_out_no
        if (!lossItems.isEmpty())
        {
            jxcStockCheckMapper.insertAdjustOutHead(outNo, checkNo, outTotal, operName);
            Long outId = jxcStockCheckMapper.selectAdjustOutIdByNo(outNo);
            int rowNo = 1;
            for (JxcStockCheckItem item : lossItems)
            {
                String batchNo = StringUtils.isEmpty(item.getBatchNo()) ? "" : item.getBatchNo();
                BigDecimal lossQty = item.getDiffQty().abs();
                BigDecimal price = jxcStockCheckMapper.selectProductPurchasePrice(item.getProductId());
                if (price == null) { price = BigDecimal.ZERO; }
                BigDecimal amount = lossQty.multiply(price).setScale(2, RoundingMode.HALF_UP);
                int affected = jxcStockCoreMapper.deductStockQty(item.getProductId(), batchNo, lossQty, operName);
                if (affected == 0)
                {
                    throw new ServiceException("商品[ID:" + item.getProductId() + "]批次[" + batchNo + "]现存量不足，无法生成盘亏出库（实盘数可能高于账面）");
                }
                BigDecimal balanceQty = jxcStockCoreMapper.selectStockQty(item.getProductId(), batchNo);
                jxcStockCheckMapper.insertAdjustOutItem(outId, rowNo, item.getProductId(), batchNo, lossQty, price, amount, operName);
                jxcStockCoreMapper.insertStockFlow(item.getProductId(), batchNo, "2", "CHECK_OUT",
                        stockCheck.getId(), item.getId(), outNo, lossQty.negate(), balanceQty, operName, "盘点盘亏出库");
                jxcStockCheckMapper.updateCheckItemGenNos(item.getId(), null, outNo);
                rowNo++;
            }
        }

        // ③ 盘点单状态更新
        JxcStockCheck update = new JxcStockCheck();
        update.setId(id);
        update.setStatus("1");
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        return jxcStockCheckMapper.updateJxcStockCheck(update);
    }

    /**
     * 新增盘点明细信息
     */
    public void insertJxcStockCheckItem(JxcStockCheck jxcStockCheck)
    {
        List<JxcStockCheckItem> jxcStockCheckItemList = jxcStockCheck.getJxcStockCheckItemList();
        Long id = jxcStockCheck.getId();
        if (StringUtils.isNotNull(jxcStockCheckItemList))
        {
            List<JxcStockCheckItem> list = new ArrayList<JxcStockCheckItem>();
            for (JxcStockCheckItem jxcStockCheckItem : jxcStockCheckItemList)
            {
                jxcStockCheckItem.setCheckId(id);
                list.add(jxcStockCheckItem);
            }
            if (list.size() > 0)
            {
                jxcStockCheckMapper.batchJxcStockCheckItem(list);
            }
        }
    }
}
