package Altech.checkout.controller;

import Altech.checkout.model.Cart;
import Altech.checkout.model.Product;
import Altech.checkout.service.CartService;
import Altech.checkout.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@Tag(name = "客戶操作", description = "客戶專用API")
public class CustomerController {
    private final ProductService productService;
    private final CartService cartService;

    // 产品浏览
    @Operation(
        summary = "獲取所有產品",
        description = "客戶獲取所有可用產品的列表"
    )
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(
        summary = "獲取產品詳情",
        description = "客戶根據ID獲取產品詳情"
    )
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // 购物车操作
    @Operation(
        summary = "創建購物車",
        description = "客戶創建一個新的購物車"
    )
    @PostMapping("/cart")
    public ResponseEntity<Cart> createCart() {
        return new ResponseEntity<>(cartService.createCart(), HttpStatus.CREATED);
    }

    @Operation(
        summary = "獲取所有購物車",
        description = "獲取所有購物車（用於管理和測試）"
    )
    @GetMapping("/carts")
    public ResponseEntity<List<Cart>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @Operation(
        summary = "獲取購物車",
        description = "客戶根據ID獲取購物車詳情"
    )
    @GetMapping("/cart/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @Operation(
        summary = "刪除購物車",
        description = "刪除指定ID的購物車（用於管理和測試）"
    )
    @DeleteMapping("/cart/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "添加商品到購物車",
        description = "客戶向購物車中添加商品"
    )
    @PostMapping("/cart/{cartId}/items")
    public ResponseEntity<Cart> addItemToCart(
            @PathVariable Long cartId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(cartId, productId, quantity));
    }

    @Operation(
        summary = "從購物車中移除商品",
        description = "客戶從購物車中移除指定的商品"
    )
    @DeleteMapping("/cart/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartId, itemId));
    }

    @Operation(
        summary = "更新購物車商品數量",
        description = "客戶更新購物車中商品的數量"
    )
    @PutMapping("/cart/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> updateCartItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(cartId, itemId, quantity));
    }

    @Operation(
        summary = "應用折扣",
        description = "為購物車中的商品應用折扣"
    )
    @PostMapping("/cart/{cartId}/apply-discounts")
    public ResponseEntity<Cart> applyDiscounts(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.applyDiscounts(cartId));
    }

    @Operation(
        summary = "生成購物車收據",
        description = "客戶生成包含所有商品、應用的折扣和總價的收據"
    )
    @GetMapping("/cart/{cartId}/receipt")
    public ResponseEntity<Map<String, Object>> generateReceipt(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.generateReceipt(cartId));
    }
} 