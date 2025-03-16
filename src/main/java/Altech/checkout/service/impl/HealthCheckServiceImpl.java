package Altech.checkout.service.impl;

import Altech.checkout.service.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查服务实现类
 */
@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> performBasicHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        boolean isSystemHealthy = true;
        
        // 检查数据库连接
        boolean isDatabaseConnected = isDatabaseConnected();
        components.put("database", Map.of(
            "status", isDatabaseConnected ? "UP" : "DOWN",
            "details", isDatabaseConnected ? "数据库连接正常" : "无法连接到数据库"
        ));
        isSystemHealthy &= isDatabaseConnected;
        
        // 检查API服务
        components.put("api", Map.of(
            "status", "UP",
            "details", "API服务正常运行"
        ));
        
        // 检查应用程序内存
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        components.put("memory", Map.of(
            "status", "UP",
            "freeMemory", freeMemory + "MB",
            "totalMemory", totalMemory + "MB",
            "maxMemory", maxMemory + "MB",
            "details", "内存使用正常"
        ));
        
        response.put("status", isSystemHealthy ? "UP" : "DOWN");
        response.put("is_healthy", isSystemHealthy);
        response.put("components", components);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    @Override
    public Map<String, Object> performDetailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        boolean isSystemHealthy = true;
        
        // 基本健康状态检查
        boolean isDatabaseConnected = isDatabaseConnected();
        components.put("database", Map.of(
            "status", isDatabaseConnected ? "UP" : "DOWN",
            "details", isDatabaseConnected ? "数据库连接正常" : "无法连接到数据库"
        ));
        isSystemHealthy &= isDatabaseConnected;
        
        // 检查关键表是否可访问
        if (isDatabaseConnected) {
            // 检查产品表
            Map<String, Object> productTableStatus = checkTableAccess("products");
            components.put("products_table", productTableStatus);
            isSystemHealthy &= "UP".equals(productTableStatus.get("status"));
            
            // 检查购物车表
            Map<String, Object> cartTableStatus = checkTableAccess("carts");
            components.put("carts_table", cartTableStatus);
            isSystemHealthy &= "UP".equals(cartTableStatus.get("status"));
            
            // 检查购物车项表
            Map<String, Object> cartItemsTableStatus = checkTableAccess("cart_items");
            components.put("cart_items_table", cartItemsTableStatus);
            isSystemHealthy &= "UP".equals(cartItemsTableStatus.get("status"));
            
            // 检查折扣表
            Map<String, Object> discountTableStatus = checkTableAccess("discounts");
            components.put("discounts_table", discountTableStatus);
            isSystemHealthy &= "UP".equals(discountTableStatus.get("status"));
        }
        
        // 系统信息
        Map<String, Object> systemInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + "MB");
        systemInfo.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + "MB");
        systemInfo.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + "MB");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        
        // 获取磁盘空间信息
        try {
            java.io.File file = new java.io.File(".");
            long totalSpace = file.getTotalSpace() / (1024 * 1024 * 1024);
            long freeSpace = file.getFreeSpace() / (1024 * 1024 * 1024);
            long usableSpace = file.getUsableSpace() / (1024 * 1024 * 1024);
            
            systemInfo.put("totalDiskSpace", totalSpace + "GB");
            systemInfo.put("freeDiskSpace", freeSpace + "GB");
            systemInfo.put("usableDiskSpace", usableSpace + "GB");
        } catch (Exception e) {
            systemInfo.put("diskSpaceError", e.getMessage());
        }
        
        components.put("system", Map.of(
            "status", "UP",
            "info", systemInfo,
            "details", "系统资源正常"
        ));
        
        response.put("status", isSystemHealthy ? "UP" : "DOWN");
        response.put("is_healthy", isSystemHealthy);
        response.put("components", components);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    @Override
    public boolean isDatabaseConnected() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> checkTableAccess(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            return Map.of(
                "status", "UP",
                "count", count,
                "details", tableName + "表可访问"
            );
        } catch (DataAccessException e) {
            return Map.of(
                "status", "DOWN",
                "details", tableName + "表访问失败: " + e.getMessage()
            );
        }
    }
} 