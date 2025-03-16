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
@Tag(name = "Customer Operations", description = "APIs for Customers")
public class CustomerController {
    private final ProductService productService;
    private final CartService cartService;

    // Product Browsing
    @Operation(
        summary = "Get All Products",
        description = "Customers can get a list of all available products"
    )
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(
        summary = "Get Product Details",
        description = "Customers can get product details by ID"
    )
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Cart Operations
    @Operation(
        summary = "Create Cart",
        description = "Customers can create a new shopping cart"
    )
    @PostMapping("/cart")
    public ResponseEntity<Cart> createCart() {
        return new ResponseEntity<>(cartService.createCart(), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get All Carts",
        description = "Get all shopping carts (for administration and testing)"
    )
    @GetMapping("/carts")
    public ResponseEntity<List<Cart>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @Operation(
        summary = "Get Cart",
        description = "Customers can get cart details by ID"
    )
    @GetMapping("/cart/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @Operation(
        summary = "Delete Cart",
        description = "Delete a cart by ID (for administration and testing)"
    )
    @DeleteMapping("/cart/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Add Item to Cart",
        description = "Customers can add items to their cart"
    )
    @PostMapping("/cart/{cartId}/items")
    public ResponseEntity<Cart> addItemToCart(
            @PathVariable Long cartId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(cartId, productId, quantity));
    }

    @Operation(
        summary = "Remove Item from Cart",
        description = "Customers can remove items from their cart"
    )
    @DeleteMapping("/cart/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartId, itemId));
    }

    @Operation(
        summary = "Update Cart Item Quantity",
        description = "Customers can update the quantity of items in their cart"
    )
    @PutMapping("/cart/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> updateCartItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(cartId, itemId, quantity));
    }

    @Operation(
        summary = "Apply Discounts",
        description = "Apply discounts to items in the cart"
    )
    @PostMapping("/cart/{cartId}/apply-discounts")
    public ResponseEntity<Cart> applyDiscounts(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.applyDiscounts(cartId));
    }

    @Operation(
        summary = "Generate Cart Receipt",
        description = "Customers can generate a receipt including all items, applied discounts, and total price"
    )
    @GetMapping("/cart/{cartId}/receipt")
    public ResponseEntity<Map<String, Object>> generateReceipt(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.generateReceipt(cartId));
    }
} 