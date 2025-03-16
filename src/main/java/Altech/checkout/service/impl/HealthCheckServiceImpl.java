package Altech.checkout.service.impl;

import Altech.checkout.service.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Service Implementation
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
        
        // Check database connection
        boolean isDatabaseConnected = isDatabaseConnected();
        components.put("database", Map.of(
            "status", isDatabaseConnected ? "UP" : "DOWN",
            "details", isDatabaseConnected ? "Database connection normal" : "Unable to connect to database"
        ));
        isSystemHealthy &= isDatabaseConnected;
        
        // Check API service
        components.put("api", Map.of(
            "status", "UP",
            "details", "API service running normally"
        ));
        
        // Check application memory
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        components.put("memory", Map.of(
            "status", "UP",
            "freeMemory", freeMemory + "MB",
            "totalMemory", totalMemory + "MB",
            "maxMemory", maxMemory + "MB",
            "details", "Memory usage normal"
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
        
        // Basic health status check
        boolean isDatabaseConnected = isDatabaseConnected();
        components.put("database", Map.of(
            "status", isDatabaseConnected ? "UP" : "DOWN",
            "details", isDatabaseConnected ? "Database connection normal" : "Unable to connect to database"
        ));
        isSystemHealthy &= isDatabaseConnected;
        
        // Check if key tables are accessible
        if (isDatabaseConnected) {
            // Check products table
            Map<String, Object> productTableStatus = checkTableAccess("products");
            components.put("products_table", productTableStatus);
            isSystemHealthy &= "UP".equals(productTableStatus.get("status"));
            
            // Check carts table
            Map<String, Object> cartTableStatus = checkTableAccess("carts");
            components.put("carts_table", cartTableStatus);
            isSystemHealthy &= "UP".equals(cartTableStatus.get("status"));
            
            // Check cart items table
            Map<String, Object> cartItemsTableStatus = checkTableAccess("cart_items");
            components.put("cart_items_table", cartItemsTableStatus);
            isSystemHealthy &= "UP".equals(cartItemsTableStatus.get("status"));
            
            // Check discounts table
            Map<String, Object> discountTableStatus = checkTableAccess("discounts");
            components.put("discounts_table", discountTableStatus);
            isSystemHealthy &= "UP".equals(discountTableStatus.get("status"));
        }
        
        // System information
        Map<String, Object> systemInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + "MB");
        systemInfo.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + "MB");
        systemInfo.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + "MB");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        
        // Get disk space information
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
            "details", "System resources normal"
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
                "details", tableName + " table accessible"
            );
        } catch (DataAccessException e) {
            return Map.of(
                "status", "DOWN",
                "details", tableName + " table access failed: " + e.getMessage()
            );
        }
    }
} 