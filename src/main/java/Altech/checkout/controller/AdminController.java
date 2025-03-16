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
@Tag(name = "Admin Operations", description = "APIs for Administrators")
public class AdminController {
    private final ProductService productService;
    private final DiscountService discountService;

    // Product Management
    @Operation(
        summary = "Create New Product",
        description = "Admin creates a new product and saves it to the database"
    )
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.createProduct(product), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get All Products",
        description = "Admin gets a list of all products"
    )
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(
        summary = "Get Product by ID",
        description = "Admin gets product details by product ID"
    )
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(
        summary = "Update Product",
        description = "Admin updates product information by ID"
    )
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @Operation(
        summary = "Delete Product",
        description = "Admin deletes a product by ID (soft delete)"
    )
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Discount Management
    @Operation(
        summary = "Create New Discount",
        description = "Admin creates a new discount and saves it to the database"
    )
    @PostMapping("/discounts")
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        return new ResponseEntity<>(discountService.createDiscount(discount), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get All Discounts",
        description = "Admin gets a list of all discounts"
    )
    @GetMapping("/discounts")
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }

    @Operation(
        summary = "Get Discount by ID",
        description = "Admin gets discount details by discount ID"
    )
    @GetMapping("/discounts/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @Operation(
        summary = "Update Discount",
        description = "Admin updates discount information by ID"
    )
    @PutMapping("/discounts/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Long id, @RequestBody Discount discount) {
        return ResponseEntity.ok(discountService.updateDiscount(id, discount));
    }

    @Operation(
        summary = "Delete Discount",
        description = "Admin deletes a discount by ID (soft delete)"
    )
    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
} 