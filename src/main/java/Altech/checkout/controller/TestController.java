package Altech.checkout.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "用于测试 Swagger UI 是否正常工作")
public class TestController {

    @Operation(
        summary = "测试接口",
        description = "返回一个简单的问候消息，用于测试 Swagger UI 是否正常工作"
    )
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Swagger UI is working!";
    }
} 