package Altech.checkout.controller;

import Altech.checkout.model.Discount;
import Altech.checkout.model.Product;
import Altech.checkout.service.DiscountService;
import Altech.checkout.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员操作", description = "管理员专用API")
public class AdminController {
    private final ProductService productService;
    private final DiscountService discountService;

    // 产品管理
    @Operation(
        summary = "创建新产品",
        description = "管理员创建一个新的产品并保存到数据库"
    )
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.createProduct(product), HttpStatus.CREATED);
    }

    @Operation(
        summary = "获取所有产品",
        description = "管理员获取所有产品的列表"
    )
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(
        summary = "根据ID获取产品",
        description = "管理员根据产品ID获取产品详情"
    )
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(
        summary = "更新产品",
        description = "管理员根据ID更新产品信息"
    )
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @Operation(
        summary = "删除产品",
        description = "管理员根据ID删除产品（软删除）"
    )
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 折扣管理
    @Operation(
        summary = "创建新折扣",
        description = "管理员创建一个新的折扣并保存到数据库"
    )
    @PostMapping("/discounts")
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        return new ResponseEntity<>(discountService.createDiscount(discount), HttpStatus.CREATED);
    }

    @Operation(
        summary = "获取所有折扣",
        description = "管理员获取所有折扣的列表"
    )
    @GetMapping("/discounts")
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }

    @Operation(
        summary = "根据ID获取折扣",
        description = "管理员根据折扣ID获取折扣详情"
    )
    @GetMapping("/discounts/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @Operation(
        summary = "更新折扣",
        description = "管理员根据ID更新折扣信息"
    )
    @PutMapping("/discounts/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Long id, @RequestBody Discount discount) {
        return ResponseEntity.ok(discountService.updateDiscount(id, discount));
    }

    @Operation(
        summary = "删除折扣",
        description = "管理员根据ID删除折扣（软删除）"
    )
    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
} 