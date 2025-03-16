package Altech.checkout.controller;

import Altech.checkout.service.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "用于测试系统和健康检查")
public class TestController {

    @Autowired
    private HealthCheckService healthCheckService;

    @Operation(
        summary = "测试接口",
        description = "返回一个简单的问候消息，用于测试 Swagger UI 是否正常工作"
    )
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Swagger UI is working!";
    }
    
    @Operation(
        summary = "健康检查接口",
        description = "执行系统健康检查，检查各组件是否正常工作"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = healthCheckService.performBasicHealthCheck();
        return ResponseEntity.ok(healthStatus);
    }
    
    @Operation(
        summary = "详细健康检查接口",
        description = "执行详细的系统健康检查，包括数据库表检查和系统资源状态"
    )
    @GetMapping("/health/detail")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> detailedHealthStatus = healthCheckService.performDetailedHealthCheck();
        return ResponseEntity.ok(detailedHealthStatus);
    }
} 