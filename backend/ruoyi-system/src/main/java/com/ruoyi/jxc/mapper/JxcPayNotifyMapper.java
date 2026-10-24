package com.ruoyi.jxc.mapper;

import java.util.List;
import com.ruoyi.jxc.domain.JxcPayNotify;

/**
 * 支付到账流水Mapper接口
 *
 * @author vivi
 * @date 2026-10-24
 */
public interface JxcPayNotifyMapper
{
    /**
     * 查询支付到账流水
     *
     * @param id 支付到账流水主键
     * @return 支付到账流水
     */
    public JxcPayNotify selectJxcPayNotifyById(Long id);

    /**
     * 查询支付到账流水列表
     *
     * @param jxcPayNotify 支付到账流水
     * @return 支付到账流水集合
     */
    public List<JxcPayNotify> selectJxcPayNotifyList(JxcPayNotify jxcPayNotify);

    /**
     * 新增支付到账流水（out_trade_no 唯一键为幂等依据，重复插入抛 DuplicateKeyException）
     *
     * @param jxcPayNotify 支付到账流水
     * @return 结果
     */
    public int insertJxcPayNotify(JxcPayNotify jxcPayNotify);
}
