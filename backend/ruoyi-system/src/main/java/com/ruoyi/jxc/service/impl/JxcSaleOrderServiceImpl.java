package com.ruoyi.jxc.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.jxc.domain.JxcSaleOrderItem;
import com.ruoyi.jxc.mapper.JxcSaleOrderMapper;
import com.ruoyi.jxc.mapper.JxcStockCoreMapper;
import com.ruoyi.jxc.domain.JxcSaleOrder;
import com.ruoyi.jxc.service.IJxcSaleOrderService;

/**
 * 销售单Service业务层处理
 * 业务约定：销售单「保存即生效」（POS 场景，一手交钱一手交货）
 * 保存时同事务完成：FEFO 拆批 → 库存原子扣减 → 流水（成本快照）→ 明细落库（含成本）→ 应收台账 → 单据生效
 * 
 * @author vivi
 * @date 2026-10-14
 */
@Service
public class JxcSaleOrderServiceImpl implements IJxcSaleOrderService 
{
    @Autowired
    private JxcSaleOrderMapper jxcSaleOrderMapper;

    @Autowired
    private JxcStockCoreMapper jxcStockCoreMapper;

    /**
     * 查询销售单
     * 
     * @param id 销售单主键
     * @return 销售单
     */
    @Override
    public JxcSaleOrder selectJxcSaleOrderById(Long id)
    {
        return jxcSaleOrderMapper.selectJxcSaleOrderById(id);
    }

    /**
     * 查询销售单列表
     * 
     * @param jxcSaleOrder 销售单
     * @return 销售单
     */
    @Override
    public List<JxcSaleOrder> selectJxcSaleOrderList(JxcSaleOrder jxcSaleOrder)
    {
        return jxcSaleOrderMapper.selectJxcSaleOrderList(jxcSaleOrder);
    }

    /**
     * 新增销售单（保存即生效）
     * ① FEFO 拆批试算（失效日期近的批次先出，非批次商品空串批次）并展开为多行明细（含批次与成本快照）
     * ② 主表金额计算与收款状态判定
     * ③ 插入主表与明细
     * ④ 逐拆分行原子扣减库存（affected=0 即库存不足，整单回滚）+ 写流水（负数变动+批次结存快照）
     * ⑤ 存在未收款金额时生成应收台账（必须有会员，否则拒绝）
     *
     * @param jxcSaleOrder 销售单
     * @return 结果
     */
    @Transactional
    @Override
    public int insertJxcSaleOrder(JxcSaleOrder jxcSaleOrder)
    {
        String operName = SecurityUtils.getUsername();
        List<JxcSaleOrderItem> inputItems = jxcSaleOrder.getJxcSaleOrderItemList();
        if (inputItems == null || inputItems.isEmpty())
        {
            throw new ServiceException("销售单无明细，不可保存");
        }

        // ① FEFO 拆批（只读试算；实际扣减在插入后进行，扣减失败整单回滚）
        List<JxcSaleOrderItem> expanded = new ArrayList<JxcSaleOrderItem>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long rowNo = 1L;
        for (JxcSaleOrderItem item : inputItems)
        {
            BigDecimal remaining = item.getQuantity();
            if (remaining == null || remaining.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("明细数量必须大于 0");
            }
            if (item.getPrice() == null)
            {
                throw new ServiceException("明细售价不能为空");
            }
            List<Map<String, Object>> batches = jxcStockCoreMapper.selectFefoBatches(item.getProductId());
            BigDecimal available = BigDecimal.ZERO;
            for (Map<String, Object> b : batches)
            {
                available = available.add((BigDecimal) b.get("quantity"));
            }
            if (available.compareTo(remaining) < 0)
            {
                throw new ServiceException("商品[ID:" + item.getProductId() + "]可用库存不足（可用 " + available + "，需要 " + remaining + "）");
            }
            for (Map<String, Object> b : batches)
            {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0)
                {
                    break;
                }
                BigDecimal batchQty = (BigDecimal) b.get("quantity");
                BigDecimal take = batchQty.min(remaining);
                if (take.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                JxcSaleOrderItem row = new JxcSaleOrderItem();
                row.setRowNo(rowNo++);
                row.setProductId(item.getProductId());
                row.setBatchNo((String) b.get("batchNo") == null ? "" : (String) b.get("batchNo"));
                row.setQuantity(take);
                row.setPrice(item.getPrice());
                BigDecimal amount = take.multiply(item.getPrice()).setScale(2, RoundingMode.HALF_UP);
                row.setAmount(amount);
                BigDecimal costPrice = (BigDecimal) b.get("costPrice");
                row.setCostPrice(costPrice);
                row.setCostAmount(take.multiply(costPrice).setScale(2, RoundingMode.HALF_UP));
                row.setRemark(item.getRemark());
                row.setCreateBy(operName);
                row.setCreateTime(DateUtils.getNowDate());
                expanded.add(row);
                totalAmount = totalAmount.add(amount);
                remaining = remaining.subtract(take);
            }
        }

        // ② 主表金额与收款状态（保存即生效：status=1 已审核）
        BigDecimal discount = jxcSaleOrder.getDiscountAmount() == null ? BigDecimal.ZERO : jxcSaleOrder.getDiscountAmount();
        BigDecimal actual = totalAmount.subtract(discount);
        if (actual.compareTo(BigDecimal.ZERO) < 0)
        {
            actual = BigDecimal.ZERO;
        }
        BigDecimal received = jxcSaleOrder.getReceivedAmount() == null ? BigDecimal.ZERO : jxcSaleOrder.getReceivedAmount();
        if (received.compareTo(actual) > 0)
        {
            received = actual;
        }
        jxcSaleOrder.setTotalAmount(totalAmount);
        jxcSaleOrder.setDiscountAmount(discount);
        jxcSaleOrder.setActualAmount(actual);
        jxcSaleOrder.setReceivedAmount(received);
        if (received.compareTo(actual) >= 0)
        {
            jxcSaleOrder.setPayStatus("2");
        }
        else if (received.compareTo(BigDecimal.ZERO) > 0)
        {
            jxcSaleOrder.setPayStatus("1");
        }
        else
        {
            jxcSaleOrder.setPayStatus("0");
        }
        jxcSaleOrder.setStatus("1");
        jxcSaleOrder.setAuditBy(operName);
        jxcSaleOrder.setAuditTime(DateUtils.getNowDate());
        jxcSaleOrder.setCreateBy(operName);
        jxcSaleOrder.setCreateTime(DateUtils.getNowDate());
        jxcSaleOrder.setJxcSaleOrderItemList(expanded);

        // ③ 插入主表与明细
        int rows = jxcSaleOrderMapper.insertJxcSaleOrder(jxcSaleOrder);
        insertJxcSaleOrderItem(jxcSaleOrder);

        // ④ 逐拆分行扣减 + 流水（重新查询明细以获得明细ID，用于流水 biz_item_id 可追溯）
        List<JxcSaleOrderItem> savedItems = jxcSaleOrderMapper.selectJxcSaleOrderById(jxcSaleOrder.getId()).getJxcSaleOrderItemList();
        for (int i = 0; i < expanded.size(); i++)
        {
            JxcSaleOrderItem row = expanded.get(i);
            Long itemId = (savedItems != null && savedItems.size() > i) ? savedItems.get(i).getId() : null;
            int affected = jxcStockCoreMapper.deductStockQty(row.getProductId(), row.getBatchNo(), row.getQuantity(), operName);
            if (affected == 0)
            {
                throw new ServiceException("商品[ID:" + row.getProductId() + "]批次[" + row.getBatchNo() + "]库存不足（并发变动），整单已回滚");
            }
            BigDecimal balance = jxcStockCoreMapper.selectStockQty(row.getProductId(), row.getBatchNo());
            jxcStockCoreMapper.insertStockFlow(row.getProductId(), row.getBatchNo(), "2", "SALE_OUT",
                    jxcSaleOrder.getId(), itemId, jxcSaleOrder.getSaleNo(), row.getQuantity().negate(), balance,
                    operName, "销售出库（FEFO 批次：" + row.getBatchNo() + "）");
        }

        // ⑤ 应收账款（未收款/部分收款：必须挂会员，生成差额应收台账）
        BigDecimal unpaid = actual.subtract(received);
        if (unpaid.compareTo(BigDecimal.ZERO) > 0)
        {
            if (jxcSaleOrder.getMemberId() == null)
            {
                throw new ServiceException("存在未收款金额（" + unpaid + "），必须选择会员挂账或收清全款");
            }
            jxcStockCoreMapper.insertReceivable(jxcSaleOrder.getMemberId(), jxcSaleOrder.getSaleNo(), "SALE",
                    unpaid, operName, "销售挂账/部分收款生成应收");
        }
        return rows;
    }

    /**
     * 修改销售单：销售单保存即生效，不允许修改（纠错走销售退货单）
     */
    @Transactional
    @Override
    public int updateJxcSaleOrder(JxcSaleOrder jxcSaleOrder)
    {
        JxcSaleOrder exist = jxcSaleOrderMapper.selectJxcSaleOrderById(jxcSaleOrder.getId());
        if (exist == null)
        {
            throw new ServiceException("销售单不存在");
        }
        throw new ServiceException("销售单保存即生效，不可修改；如需纠错请开销售退货单");
    }

    /**
     * 批量删除销售单：已生效销售单不允许删除（纠错走销售退货单）
     */
    @Transactional
    @Override
    public int deleteJxcSaleOrderByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            JxcSaleOrder exist = jxcSaleOrderMapper.selectJxcSaleOrderById(id);
            if (exist != null)
            {
                throw new ServiceException("销售单[" + exist.getSaleNo() + "]已生效，不可删除；如需纠错请开销售退货单");
            }
        }
        jxcSaleOrderMapper.deleteJxcSaleOrderItemBySaleIds(ids);
        return jxcSaleOrderMapper.deleteJxcSaleOrderByIds(ids);
    }

    /**
     * 删除销售单信息：已生效销售单不允许删除
     */
    @Transactional
    @Override
    public int deleteJxcSaleOrderById(Long id)
    {
        JxcSaleOrder exist = jxcSaleOrderMapper.selectJxcSaleOrderById(id);
        if (exist != null)
        {
            throw new ServiceException("销售单[" + exist.getSaleNo() + "]已生效，不可删除；如需纠错请开销售退货单");
        }
        jxcSaleOrderMapper.deleteJxcSaleOrderItemBySaleId(id);
        return jxcSaleOrderMapper.deleteJxcSaleOrderById(id);
    }

    /**
     * 新增销售单明细信息
     * 
     * @param jxcSaleOrder 销售单对象
     */
    public void insertJxcSaleOrderItem(JxcSaleOrder jxcSaleOrder)
    {
        List<JxcSaleOrderItem> jxcSaleOrderItemList = jxcSaleOrder.getJxcSaleOrderItemList();
        Long id = jxcSaleOrder.getId();
        if (StringUtils.isNotNull(jxcSaleOrderItemList))
        {
            List<JxcSaleOrderItem> list = new ArrayList<JxcSaleOrderItem>();
            for (JxcSaleOrderItem jxcSaleOrderItem : jxcSaleOrderItemList)
            {
                jxcSaleOrderItem.setSaleId(id);
                list.add(jxcSaleOrderItem);
            }
            if (list.size() > 0)
            {
                jxcSaleOrderMapper.batchJxcSaleOrderItem(list);
            }
        }
    }
}
