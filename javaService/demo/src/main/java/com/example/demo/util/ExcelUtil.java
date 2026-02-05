package com.example.demo.util;

import com.example.demo.dto.ImportError;
import com.example.demo.entity.Order;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 工具类
 * 处理订单的导入导出功能
 */
@Component
public class ExcelUtil {

    // Excel 模板列定义
    private static final String[] TEMPLATE_HEADERS = {
        "货物名称", "货物类型", "重量(kg)", "体积(m³)", "数量(件)",
        "发货人", "发货人电话", "发货地址",
        "收货人", "收货人电话", "收货地址",
        "快递公司", "备注"
    };

    // 导出列定义（包含订单号、运单号和状态）
    private static final String[] EXPORT_HEADERS = {
        "订单号", "运单号", "货物名称", "货物类型", "重量(kg)", "体积(m³)", "数量(件)",
        "发货人", "发货人电话", "发货地址",
        "收货人", "收货人电话", "收货地址",
        "快递公司", "状态", "创建时间", "备注"
    };

    // 错误导出列定义
    private static final String[] ERROR_HEADERS = {
        "行号", "字段名", "原始值", "错误原因"
    };

    /**
     * 生成导入模板
     * @return Excel 文件字节数组
     */
    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("订单导入模板");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // 添加示例数据行（可选）
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("示例货物");
            exampleRow.createCell(1).setCellValue("普通");
            exampleRow.createCell(2).setCellValue(10.5);
            exampleRow.createCell(3).setCellValue(0.5);
            exampleRow.createCell(4).setCellValue(1);
            exampleRow.createCell(5).setCellValue("张三");
            exampleRow.createCell(6).setCellValue("13800138000");
            exampleRow.createCell(7).setCellValue("北京市朝阳区xxx街道");
            exampleRow.createCell(8).setCellValue("李四");
            exampleRow.createCell(9).setCellValue("13900139000");
            exampleRow.createCell(10).setCellValue("上海市浦东新区xxx路");
            exampleRow.createCell(11).setCellValue("顺丰速运");
            exampleRow.createCell(12).setCellValue("请小心轻放");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 解析上传的 Excel 文件
     * 支持导入模板格式和导出格式，根据表头自动识别
     * @param file 上传的文件
     * @return 解析后的订单列表
     */
    public List<Order> parseExcel(MultipartFile file) throws IOException {
        List<Order> orders = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            
            // 读取表头，建立列名到索引的映射
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return orders;
            }
            
            java.util.Map<String, Integer> columnMap = new java.util.HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = getCellStringValue(headerRow.getCell(i));
                if (header != null && !header.isEmpty()) {
                    columnMap.put(header, i);
                }
            }
            
            // 从第二行开始读取（跳过表头）
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                
                Order order = new Order();
                // 读取订单号（如果存在）
                order.setOrderNo(getCellByHeader(row, columnMap, "订单号"));
                order.setCargoName(getCellByHeader(row, columnMap, "货物名称"));
                order.setCargoType(getCellByHeader(row, columnMap, "货物类型"));
                order.setCargoWeight(getDoubleCellByHeader(row, columnMap, "重量(kg)"));
                order.setCargoVolume(getDoubleCellByHeader(row, columnMap, "体积(m³)"));
                order.setCargoQuantity(getIntCellByHeader(row, columnMap, "数量(件)"));
                order.setSenderName(getCellByHeader(row, columnMap, "发货人"));
                order.setSenderPhone(getCellByHeader(row, columnMap, "发货人电话"));
                order.setOrigin(getCellByHeader(row, columnMap, "发货地址"));
                order.setReceiverName(getCellByHeader(row, columnMap, "收货人"));
                order.setReceiverPhone(getCellByHeader(row, columnMap, "收货人电话"));
                order.setDestination(getCellByHeader(row, columnMap, "收货地址"));
                order.setExpressCompany(getCellByHeader(row, columnMap, "快递公司"));
                order.setRemark(getCellByHeader(row, columnMap, "备注"));
                
                orders.add(order);
            }
        }
        
        return orders;
    }

    /**
     * 根据列名获取单元格字符串值
     */
    private String getCellByHeader(Row row, java.util.Map<String, Integer> columnMap, String header) {
        Integer index = columnMap.get(header);
        if (index == null) {
            return null;
        }
        return getCellStringValue(row.getCell(index));
    }

    /**
     * 根据列名获取单元格 Double 值
     */
    private Double getDoubleCellByHeader(Row row, java.util.Map<String, Integer> columnMap, String header) {
        Integer index = columnMap.get(header);
        if (index == null) {
            return null;
        }
        return getCellDoubleValue(row.getCell(index));
    }

    /**
     * 根据列名获取单元格 Integer 值
     */
    private Integer getIntCellByHeader(Row row, java.util.Map<String, Integer> columnMap, String header) {
        Integer index = columnMap.get(header);
        if (index == null) {
            return null;
        }
        return getCellIntValue(row.getCell(index));
    }


    // 货物类型映射：内部代码 -> 中文名称
    private static final java.util.Map<String, String> CARGO_TYPE_MAP = java.util.Map.of(
        "normal", "普通",
        "fragile", "易碎",
        "cold", "生鲜",
        "dangerous", "贵重",
        "document", "文件"
    );
    
    // 快递公司映射：内部代码 -> 中文名称
    private static final java.util.Map<String, String> EXPRESS_COMPANY_MAP = java.util.Map.ofEntries(
        java.util.Map.entry("sf", "顺丰速运"),
        java.util.Map.entry("zto", "中通快递"),
        java.util.Map.entry("yto", "圆通速递"),
        java.util.Map.entry("yd", "韵达快递"),
        java.util.Map.entry("sto", "申通快递"),
        java.util.Map.entry("jd", "京东物流"),
        java.util.Map.entry("ems", "邮政EMS"),
        java.util.Map.entry("deppon", "德邦快递"),
        java.util.Map.entry("jitu", "极兔速递"),
        java.util.Map.entry("best", "百世快递")
    );
    
    // 状态映射：内部代码 -> 中文名称
    private static final java.util.Map<String, String> STATUS_MAP = java.util.Map.of(
        "pending", "待发货",
        "shipping", "运输中",
        "completed", "已完成",
        "cancelled", "已取消"
    );

    /**
     * 将订单列表导出为 Excel
     * @param orders 订单列表
     * @return Excel 文件字节数组
     */
    public byte[] exportOrders(List<Order> orders) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("订单数据");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            
            // 填充数据
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderNo() != null ? order.getOrderNo() : "");
                row.createCell(1).setCellValue(order.getTrackingNo() != null ? order.getTrackingNo() : "");
                row.createCell(2).setCellValue(order.getCargoName() != null ? order.getCargoName() : "");
                // 货物类型：转换为中文显示名称
                row.createCell(3).setCellValue(convertCargoType(order.getCargoType()));
                row.createCell(4).setCellValue(order.getCargoWeight() != null ? order.getCargoWeight() : 0);
                row.createCell(5).setCellValue(order.getCargoVolume() != null ? order.getCargoVolume() : 0);
                row.createCell(6).setCellValue(order.getCargoQuantity() != null ? order.getCargoQuantity() : 0);
                row.createCell(7).setCellValue(order.getSenderName() != null ? order.getSenderName() : "");
                row.createCell(8).setCellValue(order.getSenderPhone() != null ? order.getSenderPhone() : "");
                row.createCell(9).setCellValue(order.getOrigin() != null ? order.getOrigin() : "");
                row.createCell(10).setCellValue(order.getReceiverName() != null ? order.getReceiverName() : "");
                row.createCell(11).setCellValue(order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
                row.createCell(12).setCellValue(order.getDestination() != null ? order.getDestination() : "");
                // 快递公司：转换为中文显示名称
                row.createCell(13).setCellValue(convertExpressCompany(order.getExpressCompany()));
                // 状态：转换为中文显示名称
                row.createCell(14).setCellValue(convertStatus(order.getStatus()));
                row.createCell(15).setCellValue(order.getCreateTime() != null ? order.getCreateTime() : "");
                row.createCell(16).setCellValue(order.getRemark() != null ? order.getRemark() : "");
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 转换货物类型为中文显示名称
     */
    private String convertCargoType(String cargoType) {
        if (cargoType == null || cargoType.isEmpty()) {
            return "";
        }
        // 如果已经是中文名称，直接返回
        if (CARGO_TYPE_MAP.containsValue(cargoType)) {
            return cargoType;
        }
        // 转换内部代码为中文名称
        return CARGO_TYPE_MAP.getOrDefault(cargoType, cargoType);
    }

    /**
     * 转换快递公司为中文显示名称
     */
    private String convertExpressCompany(String expressCompany) {
        if (expressCompany == null || expressCompany.isEmpty()) {
            return "";
        }
        // 如果已经是中文名称，直接返回
        if (EXPRESS_COMPANY_MAP.containsValue(expressCompany)) {
            return expressCompany;
        }
        // 转换内部代码为中文名称
        return EXPRESS_COMPANY_MAP.getOrDefault(expressCompany, expressCompany);
    }

    /**
     * 转换状态为中文显示名称
     */
    private String convertStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "";
        }
        return STATUS_MAP.getOrDefault(status, status);
    }

    /**
     * 将错误列表导出为 Excel
     * @param errors 错误列表
     * @return Excel 文件字节数组
     */
    public byte[] exportErrors(List<ImportError> errors) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("导入失败记录");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < ERROR_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(ERROR_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            
            // 填充数据
            int rowNum = 1;
            for (ImportError error : errors) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(error.getRow());
                row.createCell(1).setCellValue(error.getField() != null ? error.getField() : "");
                row.createCell(2).setCellValue(error.getValue() != null ? error.getValue() : "");
                row.createCell(3).setCellValue(error.getMessage() != null ? error.getMessage() : "");
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }


    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        return style;
    }

    /**
     * 判断行是否为空
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 处理数字类型，避免科学计数法
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    /**
     * 获取单元格 Double 值
     */
    private Double getCellDoubleValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * 获取单元格 Integer 值
     */
    private Integer getCellIntValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}
