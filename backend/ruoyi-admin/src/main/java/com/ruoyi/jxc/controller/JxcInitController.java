package com.ruoyi.jxc.controller;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.jxc.mapper.JxcInitMapper;
import com.ruoyi.jxc.mapper.JxcStockCoreMapper;

/**
 * 期初数据导入（数据迁移工具）
 * 流程：下载模板 → 上传解析（试运行校验，不落库）→ 确认导入（同事务写入现存量+期初流水）
 * 校验规则：
 * ①商品编码必填且存在于商品档案；②数量必填且 > 0；③成本单价必填且 >= 0；
 * ④批次商品批次号必填、非批次商品批次号必须留空；⑤同一商品+批次在文件内不可重复；
 * ⑥目标商品+批次已有大于 0 的库存时拒绝（期初建账只允许对空账批次执行）。
 *
 * @author vivi
 */
@RestController
@RequestMapping("/jxc/init")
public class JxcInitController extends BaseController
{
    @Autowired
    private JxcInitMapper jxcInitMapper;

    @Autowired
    private JxcStockCoreMapper jxcStockCoreMapper;

    private static final String[] HEADERS = {"商品编码*", "批次号(非批次商品留空)", "生产日期(yyyy-MM-dd)", "到期日(yyyy-MM-dd)", "期初数量*", "成本单价*", "备注"};

    /**
     * 下载期初导入模板（含示例行与填写说明）
     */
    @PreAuthorize("@ss.hasPermi('jxc:init:list')")
    @GetMapping("/template")
    public void template(HttpServletResponse response) throws Exception
    {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("期初库存导入");
        // 表头
        CellStyle headStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headStyle.setFont(font);
        Row head = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++)
        {
            Cell cell = head.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headStyle);
            sheet.setColumnWidth(i, 22 * 256);
        }
        // 示例行
        Row demo = sheet.createRow(1);
        demo.createCell(0).setCellValue("SP0001");
        demo.createCell(1).setCellValue("P20261001");
        demo.createCell(2).setCellValue("2026-10-01");
        demo.createCell(3).setCellValue("2027-06-28");
        demo.createCell(4).setCellValue(100);
        demo.createCell(5).setCellValue(1.8);
        demo.createCell(6).setCellValue("示例行，导入前可删除");
        // 说明行
        Row note = sheet.createRow(3);
        note.createCell(0).setCellValue("说明：仅允许对尚未建账（库存为0）的商品+批次导入期初数据；批次商品必须填批次号，非批次商品留空。");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("期初库存导入模板.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
        OutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
        wb.close();
    }

    /**
     * 上传解析 + 试运行校验（不落库）
     */
    @PreAuthorize("@ss.hasPermi('jxc:init:list')")
    @PostMapping("/parse")
    public AjaxResult parse(@RequestParam("file") MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            return error("请上传 Excel 文件");
        }
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (Workbook wb = WorkbookFactory.create(file.getInputStream()))
        {
            Sheet sheet = wb.getSheetAt(0);
            int last = sheet.getLastRowNum();
            Map<String, Integer> seen = new HashMap<String, Integer>();
            for (int r = 1; r <= last; r++)
            {
                Row row = sheet.getRow(r);
                if (row == null)
                {
                    continue;
                }
                String productCode = getCellString(row.getCell(0));
                String batchNoRaw = getCellString(row.getCell(1));
                String productionDate = getCellDate(row.getCell(2), sdf);
                String expiryDate = getCellDate(row.getCell(3), sdf);
                String qtyStr = getCellString(row.getCell(4));
                String priceStr = getCellString(row.getCell(5));
                String remark = getCellString(row.getCell(6));
                // 空行跳过
                if (isEmpty(productCode) && isEmpty(batchNoRaw) && isEmpty(qtyStr) && isEmpty(priceStr))
                {
                    continue;
                }
                // 说明行跳过（明显不是数据的行由校验拦截即可）
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("rowNum", r + 1);
                item.put("productCode", productCode);
                item.put("batchNo", batchNoRaw == null ? "" : batchNoRaw.trim());
                item.put("productionDate", productionDate);
                item.put("expiryDate", expiryDate);
                item.put("quantity", qtyStr);
                item.put("costPrice", priceStr);
                item.put("remark", remark);
                List<String> errors = new ArrayList<String>();

                Map<String, Object> product = isEmpty(productCode) ? null : jxcInitMapper.selectProductByCode(productCode.trim());
                if (isEmpty(productCode))
                {
                    errors.add("商品编码不能为空");
                }
                else if (product == null)
                {
                    errors.add("商品编码[" + productCode + "]不存在于商品档案");
                }
                item.put("productName", product == null ? "" : product.get("productName"));

                BigDecimal qty = parseDecimal(qtyStr);
                if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0)
                {
                    errors.add("期初数量必须为大于 0 的数字");
                }
                BigDecimal price = parseDecimal(priceStr);
                if (price == null || price.compareTo(BigDecimal.ZERO) < 0)
                {
                    errors.add("成本单价必须为不小于 0 的数字");
                }
                if (product != null)
                {
                    boolean enableBatch = "1".equals(String.valueOf(product.get("enableBatch")));
                    String batchNo = item.get("batchNo").toString();
                    if (enableBatch && isEmpty(batchNo))
                    {
                        errors.add("批次商品必须填写批次号");
                    }
                    if (!enableBatch && !isEmpty(batchNo))
                    {
                        errors.add("非批次商品批次号必须留空");
                    }
                    // 重复行检查
                    String key = productCode.trim() + "|" + batchNo;
                    if (seen.containsKey(key))
                    {
                        errors.add("与第 " + seen.get(key) + " 行重复（同商品同批次）");
                    }
                    else
                    {
                        seen.put(key, r + 1);
                    }
                    // 已建账检查
                    BigDecimal existQty = jxcStockCoreMapper.selectStockQty((Long) product.get("id"), batchNo);
                    if (existQty != null && existQty.compareTo(BigDecimal.ZERO) > 0)
                    {
                        errors.add("该商品+批次已有库存(" + existQty + ")，期初仅允许对空账批次导入");
                    }
                }
                if (!isEmpty(productionDate) && !isValidDate(productionDate))
                {
                    errors.add("生产日期格式应为 yyyy-MM-dd");
                }
                if (!isEmpty(expiryDate) && !isValidDate(expiryDate))
                {
                    errors.add("到期日格式应为 yyyy-MM-dd");
                }
                item.put("valid", errors.isEmpty());
                item.put("errors", errors);
                rows.add(item);
            }
        }
        int validCount = 0;
        for (Map<String, Object> item : rows)
        {
            if (Boolean.TRUE.equals(item.get("valid")))
            {
                validCount++;
            }
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("total", rows.size());
        result.put("validCount", validCount);
        result.put("invalidCount", rows.size() - validCount);
        result.put("rows", rows);
        return success(result);
    }

    /**
     * 确认导入（仅导入校验通过的行；同事务：现存量 + 期初流水）
     */
    @PreAuthorize("@ss.hasPermi('jxc:init:list')")
    @Log(title = "期初数据导入", businessType = BusinessType.IMPORT)
    @PostMapping("/commit")
    @Transactional
    public AjaxResult commit(@RequestBody List<Map<String, Object>> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return error("没有待导入的数据");
        }
        String operName = getUsername();
        int success = 0;
        List<String> failed = new ArrayList<String>();
        for (Map<String, Object> row : rows)
        {
            String productCode = row.get("productCode") == null ? "" : String.valueOf(row.get("productCode")).trim();
            String batchNo = row.get("batchNo") == null ? "" : String.valueOf(row.get("batchNo")).trim();
            String qtyStr = row.get("quantity") == null ? "" : String.valueOf(row.get("quantity"));
            String priceStr = row.get("costPrice") == null ? "" : String.valueOf(row.get("costPrice"));
            try
            {
                Map<String, Object> product = jxcInitMapper.selectProductByCode(productCode);
                if (product == null)
                {
                    throw new IllegalArgumentException("商品编码不存在");
                }
                BigDecimal qty = parseDecimal(qtyStr);
                BigDecimal price = parseDecimal(priceStr);
                if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0 || price == null || price.compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new IllegalArgumentException("数量或成本单价不合法");
                }
                BigDecimal existQty = jxcStockCoreMapper.selectStockQty((Long) product.get("id"), batchNo);
                if (existQty != null && existQty.compareTo(BigDecimal.ZERO) > 0)
                {
                    throw new IllegalArgumentException("该商品+批次已有库存(" + existQty + ")");
                }
                Long productId = (Long) product.get("id");
                String productionDate = row.get("productionDate") == null || String.valueOf(row.get("productionDate")).isEmpty()
                        ? null : String.valueOf(row.get("productionDate"));
                String expiryDate = row.get("expiryDate") == null || String.valueOf(row.get("expiryDate")).isEmpty()
                        ? null : String.valueOf(row.get("expiryDate"));
                // ① 现存量（期初建账）
                jxcStockCoreMapper.upsertStockQtyIncrease(productId, batchNo, productionDate, expiryDate, qty, price, operName);
                // ② 期初流水（biz_type=INIT，biz_id=0 为哨兵值：期初建账无来源单据，保留迁移痕迹）
                BigDecimal balanceQty = jxcStockCoreMapper.selectStockQty(productId, batchNo);
                jxcStockCoreMapper.insertStockFlow(productId, batchNo, "1", "INIT",
                        0L, null, "期初建账", qty, balanceQty, operName, "期初数据导入");
                success++;
            }
            catch (Exception e)
            {
                failed.add("商品[" + productCode + "]批次[" + batchNo + "]：" + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("successCount", success);
        result.put("failCount", failed.size());
        result.put("failed", failed);
        return success(result);
    }

    private String getCellString(Cell cell)
    {
        if (cell == null)
        {
            return null;
        }
        switch (cell.getCellType())
        {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d))
                {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try
                {
                    return cell.getStringCellValue();
                }
                catch (Exception e)
                {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private String getCellDate(Cell cell, SimpleDateFormat sdf)
    {
        if (cell == null)
        {
            return null;
        }
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
        {
            Date date = cell.getDateCellValue();
            return sdf.format(date);
        }
        String v = getCellString(cell);
        return v == null ? null : v.trim();
    }

    private BigDecimal parseDecimal(String v)
    {
        if (isEmpty(v))
        {
            return null;
        }
        try
        {
            return new BigDecimal(v.trim());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private boolean isValidDate(String v)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(v);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean isEmpty(String v)
    {
        return v == null || v.trim().isEmpty();
    }
}
