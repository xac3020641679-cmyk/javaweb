package com.enterprise.catering.controller;

import com.enterprise.catering.dao.BlanketOrderDAO;
import com.enterprise.catering.dao.OrderDAO;
import com.enterprise.catering.model.BlanketOrder;
import com.enterprise.catering.model.Order;
import com.enterprise.catering.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 文件下载接口：总括订单导出等。
 */
@RestController
@RequestMapping("/api/downloads")
public class DownloadController {

    @Autowired
    private BlanketOrderDAO blanketOrderDAO;
    @Autowired
    private OrderDAO orderDAO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("DownloadController is working!");
    }
    
    @GetMapping("/blanket-order")
    public ResponseEntity<String> downloadBlanketOrder(@RequestParam(required = false) String date) {
        try {
            Date reportDate = parseDateOrToday(date);
            
            List<BlanketOrder> blanketOrders = blanketOrderDAO.getBlanketOrdersByDate(reportDate);
            
            // 如果数据为空，返回提示信息
            if (blanketOrders == null || blanketOrders.isEmpty()) {
                StringBuilder emptyCsv = new StringBuilder();
                emptyCsv.append("\uFEFF"); // BOM for Excel UTF-8 support
                emptyCsv.append("总括订单 - ").append(sdf.format(reportDate)).append("\n");
                emptyCsv.append("提示：该日期暂无订单数据\n");
                emptyCsv.append("菜名,单位,总数量,单价,小计\n");
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
                String filename = "总括订单_" + sdf.format(reportDate) + ".csv";
                setFilenameHeader(headers, filename);
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(emptyCsv.toString());
            }

            StringBuilder csv = new StringBuilder();
            csv.append("\uFEFF"); // BOM for Excel UTF-8 support
            csv.append("总括订单 - ").append(sdf.format(reportDate)).append("\n");
            csv.append("菜名,单位,总数量,单价,小计\n");
            
            double totalAmount = 0;
            int itemCount = 0;
            for (BlanketOrder order : blanketOrders) {
                if (order == null) {
                    continue;
                }
                
                csv.append(escapeCsv(order.getItemName() != null ? order.getItemName() : "")).append(",");
                csv.append(escapeCsv(order.getUnit() != null ? order.getUnit() : "")).append(",");
                csv.append(order.getTotalQuantity()).append(",");
                csv.append(String.format("%.2f", order.getPrice())).append(",");
                csv.append(String.format("%.2f", order.getSubtotal())).append("\n");
                totalAmount += order.getSubtotal();
                itemCount++;
            }
            
            csv.append("总计,,,,").append(String.format("%.2f", totalAmount)).append("\n");
            
            String csvContent = csv.toString();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
            String filename = "总括订单_" + sdf.format(reportDate) + ".csv";
            setFilenameHeader(headers, filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);
        } catch (ParseException e) {
            System.err.println("日期解析失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            System.err.println("生成总括订单报表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("生成报表失败: " + e.getMessage());
        }
    }
    
    private Date parseDateOrToday(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty() || dateStr.trim().isEmpty()) {
            return new Date();
        }
        try {
            return sdf.parse(dateStr.trim());
        } catch (ParseException e) {
            System.err.println("日期解析失败: " + dateStr + ", 错误: " + e.getMessage());
            throw e;
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * 设置文件名响应头（统一处理编码）
     */
    private void setFilenameHeader(HttpHeaders headers, String filename) {
        try {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
        } catch (Exception e) {
            System.err.println("文件名编码失败，使用简单方式: " + e.getMessage());
            headers.setContentDispositionFormData("attachment", filename);
        }
    }
    
    @GetMapping("/daily-order")
    public ResponseEntity<String> downloadDailyOrder(@RequestParam(required = false) String date) {
        try {
            Date reportDate = parseDateOrToday(date);
            
            List<Order> orders = orderDAO.getOrdersByDate(reportDate);
            
            // 如果数据为空，返回提示信息
            if (orders == null || orders.isEmpty()) {
                StringBuilder emptyCsv = new StringBuilder();
                emptyCsv.append("\uFEFF"); // BOM for Excel UTF-8 support
                emptyCsv.append("每日订单 - ").append(sdf.format(reportDate)).append("\n\n");
                emptyCsv.append("提示：该日期暂无订单数据\n");
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
                String filename = "每日订单_" + sdf.format(reportDate) + ".csv";
                setFilenameHeader(headers, filename);
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(emptyCsv.toString());
            }

            StringBuilder csv = new StringBuilder();
            csv.append("\uFEFF"); // BOM for Excel UTF-8 support
            csv.append("每日订单 - ").append(sdf.format(reportDate)).append("\n\n");
            
            double totalAmount = 0;
            int processedOrders = 0;
            
            for (Order order : orders) {
                if (order == null) {
                    continue;
                }
                
                try {
                    // 订单基本信息
                    csv.append("订单ID: ").append(order.getId()).append("\n");
                    csv.append("员工姓名: ").append(escapeCsv(order.getUserName() != null ? order.getUserName() : "")).append("\n");
                    csv.append("联系电话: ").append(escapeCsv(order.getPhone() != null ? order.getPhone() : "")).append("\n");
                    csv.append("订单时间: ").append(order.getOrderTime() != null ? sdfDateTime.format(order.getOrderTime()) : "").append("\n");
                    csv.append("订单总额: ¥").append(String.format("%.2f", order.getTotalPrice())).append("\n");
                    csv.append("\n");
                    
                    // 订单明细
                    csv.append("菜品明细:\n");
                    csv.append("菜名,单位,数量,单价,小计\n");
                    
                    List<OrderItem> items = orderDAO.getOrderItemsByOrderId(order.getId());
                    
                    if (items != null && !items.isEmpty()) {
                        for (OrderItem item : items) {
                            if (item == null) {
                                continue;
                            }
                            csv.append(escapeCsv(item.getName() != null ? item.getName() : "")).append(",");
                            csv.append(escapeCsv(item.getUnit() != null ? item.getUnit() : "")).append(",");
                            csv.append(item.getQuantity()).append(",");
                            csv.append(String.format("%.2f", item.getPrice())).append(",");
                            csv.append(String.format("%.2f", item.getSubtotal())).append("\n");
                        }
                    } else {
                        csv.append("（无订单项）\n");
                    }
                    
                    csv.append("订单小计,,").append(items != null ? items.size() : 0).append(",,").append(String.format("%.2f", order.getTotalPrice())).append("\n");
                    csv.append("\n");
                    csv.append("----------------------------------------\n");
                    csv.append("\n");
                    
                    totalAmount += order.getTotalPrice();
                    processedOrders++;
                } catch (Exception e) {
                    System.err.println("处理订单 " + order.getId() + " 时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理下一个订单
                }
            }
            
            // 汇总信息
            csv.append("汇总信息\n");
            csv.append("订单总数: ").append(processedOrders).append("\n");
            csv.append("总金额: ¥").append(String.format("%.2f", totalAmount)).append("\n");
            
            String csvContent = csv.toString();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
            String filename = "每日订单_" + sdf.format(reportDate) + ".csv";
            setFilenameHeader(headers, filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);
        } catch (ParseException e) {
            System.err.println("日期解析失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            System.err.println("生成每日订单报表时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("生成报表失败: " + e.getMessage());
        }
    }
}

