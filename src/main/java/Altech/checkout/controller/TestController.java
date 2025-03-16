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
@Tag(name = "Test API", description = "For system testing and health check")
public class TestController {

    @Autowired
    private HealthCheckService healthCheckService;

    @Operation(
        summary = "Test API",
        description = "Returns a simple greeting message to test if Swagger UI is working properly"
    )
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Swagger UI is working!";
    }
    
    @Operation(
        summary = "Health Check API",
        description = "Performs system health check to verify all components are working properly"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = healthCheckService.performBasicHealthCheck();
        return ResponseEntity.ok(healthStatus);
    }
    
    @Operation(
        summary = "Detailed Health Check API",
        description = "Performs detailed system health check including database table checks and system resource status"
    )
    @GetMapping("/health/detail")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> detailedHealthStatus = healthCheckService.performDetailedHealthCheck();
        return ResponseEntity.ok(detailedHealthStatus);
    }
} 