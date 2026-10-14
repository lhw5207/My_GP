package com.ruoyi.jxc.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;

/**
 * 库存核心 Mapper（跨模块复用）
 * 职责：现存量 upsert、批次结存查询、库存流水、应付/应收台账写入
 * 说明：所有出入库业务（采购入库/销售出库/退货/盘点）统一经由本 Mapper 变更库存，
 *       保证「现存量（jxc_stock）为唯一校验依据 + 流水（jxc_stock_flow）只增不改」的双轨约定。
 *
 * @author vivi
 */
public interface JxcStockCoreMapper
{
    /**
     * 现存量增加（批次维度 upsert）
     * 唯一键 (product_id, batch_no) 命中则累加数量并更新批次成本/效期，否则新增行。
     * 非批次商品 batch_no 传空串（禁止 NULL）。
     */
    int upsertStockQtyIncrease(@Param("productId") Long productId,
                               @Param("batchNo") String batchNo,
                               @Param("productionDate") String productionDate,
                               @Param("expiryDate") String expiryDate,
                               @Param("quantity") BigDecimal quantity,
                               @Param("costPrice") BigDecimal costPrice,
                               @Param("operName") String operName);

    /**
     * 现存量原子扣减（防并发超扣）
     * 例：UPDATE jxc_stock SET quantity = quantity - #{quantity}
     *     WHERE product_id=? AND batch_no=? AND quantity &gt;= #{quantity}
     * 返回受影响行数：0 表示库存不足（调用方须回滚事务）。
     */
    int deductStockQty(@Param("productId") Long productId,
                       @Param("batchNo") String batchNo,
                       @Param("quantity") BigDecimal quantity,
                       @Param("operName") String operName);

    /**
     * 查询批次结存数量（扣减/增加后用于写流水的 balance_qty 快照）
     */
    BigDecimal selectStockQty(@Param("productId") Long productId,
                              @Param("batchNo") String batchNo);

    /**
     * 写入库存流水（只增不改）
     * @param flowType 1入库 2出库
     * @param changeQty 变动数量（入正出负）
     * @param balanceQty 变动后批次结存快照
     */
    int insertStockFlow(@Param("productId") Long productId,
                        @Param("batchNo") String batchNo,
                        @Param("flowType") String flowType,
                        @Param("bizType") String bizType,
                        @Param("bizId") Long bizId,
                        @Param("bizItemId") Long bizItemId,
                        @Param("bizNo") String bizNo,
                        @Param("changeQty") BigDecimal changeQty,
                        @Param("balanceQty") BigDecimal balanceQty,
                        @Param("operName") String operName,
                        @Param("remark") String remark);

    /**
     * 写入应付台账（入库等增加应付的业务调用）
     */
    int insertPayable(@Param("supplierId") Long supplierId,
                      @Param("sourceNo") String sourceNo,
                      @Param("sourceType") String sourceType,
                      @Param("amount") BigDecimal amount,
                      @Param("operName") String operName,
                      @Param("remark") String remark);

    /**
     * 写入应收台账（赊销销售等业务调用）
     */
    int insertReceivable(@Param("memberId") Long memberId,
                         @Param("sourceNo") String sourceNo,
                         @Param("sourceType") String sourceType,
                         @Param("amount") BigDecimal amount,
                         @Param("operName") String operName,
                         @Param("remark") String remark);
}
