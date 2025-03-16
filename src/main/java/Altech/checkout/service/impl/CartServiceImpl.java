package Altech.checkout.service.impl;

import Altech.checkout.model.*;
import Altech.checkout.repository.CartRepository;
import Altech.checkout.repository.DiscountRepository;
import Altech.checkout.service.CartService;
import Altech.checkout.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final DiscountRepository discountRepository;

    @Override
    public Cart createCart() {
        Cart cart = new Cart();
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTotalAmount(BigDecimal.ZERO);
        return cartRepository.save(cart);
    }

    @Override
    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + id));
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteCart(Long id) {
        Cart cart = getCartById(id);
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public Cart addItemToCart(Long cartId, Long productId, Integer quantity) {
        Cart cart = getCartById(cartId);
        Product product = productService.getProductById(productId);
        
        // Check stock
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock available for product: " + product.getName());
        }
        
        // Check if the product is already in the cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setUnitPrice(product.getPrice());
            item.calculateTotalPrice();
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cart.getItems().add(newItem);
        }
        
        // Update cart total amount
        updateCartTotal(cart);
        
        // Update product stock
        product.setStock(product.getStock() - quantity);
        productService.updateProduct(product.getId(), product);
        
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeItemFromCart(Long cartId, Long cartItemId) {
        Cart cart = getCartById(cartId);
        
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found with id: " + cartItemId));
        
        // Restore product stock
        Product product = itemToRemove.getProduct();
        product.setStock(product.getStock() + itemToRemove.getQuantity());
        productService.updateProduct(product.getId(), product);
        
        // Remove item from cart
        cart.getItems().remove(itemToRemove);
        
        // Update cart total amount
        updateCartTotal(cart);
        
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateCartItemQuantity(Long cartId, Long cartItemId, Integer quantity) {
        Cart cart = getCartById(cartId);
        
        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found with id: " + cartItemId));
        
        Product product = itemToUpdate.getProduct();
        
        // Calculate stock change
        int stockChange = itemToUpdate.getQuantity() - quantity;
        
        // Check if stock is sufficient
        if (stockChange < 0 && product.getStock() < Math.abs(stockChange)) {
            throw new IllegalArgumentException("Not enough stock available for product: " + product.getName());
        }
        
        // Update item quantity
        itemToUpdate.setQuantity(quantity);
        itemToUpdate.calculateTotalPrice();
        
        // Update product stock
        product.setStock(product.getStock() + stockChange);
        productService.updateProduct(product.getId(), product);
        
        // Update cart total amount
        updateCartTotal(cart);
        
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart applyDiscounts(Long cartId) {
        Cart cart = getCartById(cartId);
        List<Discount> activeDiscounts = discountRepository.findAll().stream()
                .filter(Discount::getIsActive)
                .filter(d -> d.getStartDate() == null || d.getStartDate().isBefore(LocalDateTime.now()))
                .filter(d -> d.getEndDate() == null || d.getEndDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        
        // Reset all discounts
        cart.getItems().forEach(item -> {
            item.setAppliedDiscount(null);
            item.setDiscountAmount(BigDecimal.ZERO);
            item.calculateTotalPrice();
        });
        
        // Group by product
        Map<Product, List<CartItem>> itemsByProduct = cart.getItems().stream()
                .collect(Collectors.groupingBy(CartItem::getProduct));
        
        // Apply discounts
        for (Map.Entry<Product, List<CartItem>> entry : itemsByProduct.entrySet()) {
            Product product = entry.getKey();
            List<CartItem> items = entry.getValue();
            
            // Calculate total quantity of the product
            int totalQuantity = items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
            
            // Find the best applicable discount
            Discount bestDiscount = findBestDiscount(activeDiscounts, totalQuantity);
            
            if (bestDiscount != null) {
                applyDiscountToItems(items, bestDiscount, totalQuantity);
            }
        }
        
        // Update cart total amount
        updateCartTotal(cart);
        
        return cartRepository.save(cart);
    }

    @Override
    public Map<String, Object> generateReceipt(Long cartId) {
        Cart cart = getCartById(cartId);
        
        // Apply discounts
        applyDiscounts(cartId);
        
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("cartId", cart.getId());
        receipt.put("createdAt", cart.getCreatedAt());
        receipt.put("updatedAt", cart.getUpdatedAt());
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProduct().getId());
            itemMap.put("productName", item.getProduct().getName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unitPrice", item.getUnitPrice());
            itemMap.put("totalPrice", item.getTotalPrice());
            
            if (item.getAppliedDiscount() != null) {
                Map<String, Object> discountMap = new HashMap<>();
                discountMap.put("discountId", item.getAppliedDiscount().getId());
                discountMap.put("discountName", item.getAppliedDiscount().getName());
                discountMap.put("discountType", item.getAppliedDiscount().getType());
                discountMap.put("discountAmount", item.getDiscountAmount());
                itemMap.put("appliedDiscount", discountMap);
            }
            
            items.add(itemMap);
        }
        
        receipt.put("items", items);
        receipt.put("totalAmount", cart.getTotalAmount());
        
        return receipt;
    }
    
    // Helper methods
    
    private void updateCartTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
        cart.setUpdatedAt(LocalDateTime.now());
    }
    
    private Discount findBestDiscount(List<Discount> discounts, int quantity) {
        return discounts.stream()
                .filter(d -> d.getMinQuantity() == null || d.getMinQuantity() <= quantity)
                .max(Comparator.comparing(d -> calculateDiscountValue(d, quantity)))
                .orElse(null);
    }
    
    private BigDecimal calculateDiscountValue(Discount discount, int quantity) {
        BigDecimal value = BigDecimal.ZERO;
        
        switch (discount.getType()) {
            case PERCENTAGE:
                // Percentage discount
                value = discount.getValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                // Fixed amount discount
                value = discount.getValue();
                break;
            case BUY_X_GET_Y_FREE:
                // Buy X get Y free
                int freeItems = quantity / (discount.getMinQuantity() + 1);
                value = BigDecimal.valueOf(freeItems);
                break;
            case SECOND_UNIT_PERCENTAGE:
                // Second unit discount
                if (quantity >= 2) {
                    value = discount.getValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                break;
        }
        
        return value;
    }
    
    private void applyDiscountToItems(List<CartItem> items, Discount discount, int totalQuantity) {
        switch (discount.getType()) {
            case PERCENTAGE:
                // Percentage discount
                for (CartItem item : items) {
                    BigDecimal discountRate = discount.getValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal originalTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal discountAmount = originalTotal.multiply(discountRate);
                    
                    item.setAppliedDiscount(discount);
                    item.setDiscountAmount(discountAmount);
                    item.calculateTotalPrice();
                }
                break;
                
            case FIXED_AMOUNT:
                // Fixed amount discount - proportionally distributed to each item
                BigDecimal totalOriginalPrice = items.stream()
                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                for (CartItem item : items) {
                    BigDecimal originalItemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal ratio = originalItemTotal.divide(totalOriginalPrice, 4, RoundingMode.HALF_UP);
                    BigDecimal discountAmount = discount.getValue().multiply(ratio);
                    
                    item.setAppliedDiscount(discount);
                    item.setDiscountAmount(discountAmount);
                    item.calculateTotalPrice();
                }
                break;
                
            case BUY_X_GET_Y_FREE:
                // Buy X get Y free
                int minQuantity = discount.getMinQuantity();
                int freeItems = totalQuantity / (minQuantity + 1);
                
                if (freeItems > 0) {
                    // Distribute free items proportionally
                    int remainingFreeItems = freeItems;
                    
                    // Sort by quantity, prioritize items with higher quantities for discounts
                    List<CartItem> sortedItems = new ArrayList<>(items);
                    sortedItems.sort(Comparator.comparing(CartItem::getQuantity).reversed());
                    
                    for (CartItem item : sortedItems) {
                        if (remainingFreeItems <= 0) break;
                        
                        int freeForThisItem = Math.min(remainingFreeItems, item.getQuantity());
                        BigDecimal discountAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(freeForThisItem));
                        
                        item.setAppliedDiscount(discount);
                        item.setDiscountAmount(discountAmount);
                        item.calculateTotalPrice();
                        
                        remainingFreeItems -= freeForThisItem;
                    }
                }
                break;
                
            case SECOND_UNIT_PERCENTAGE:
                // Second unit discount
                if (totalQuantity >= 2) {
                    int discountedItems = totalQuantity / 2;
                    BigDecimal discountRate = discount.getValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    
                    // Distribute discounted items proportionally
                    int remainingDiscountedItems = discountedItems;
                    
                    // Sort by quantity, prioritize items with higher quantities for discounts
                    List<CartItem> sortedItems = new ArrayList<>(items);
                    sortedItems.sort(Comparator.comparing(CartItem::getQuantity).reversed());
                    
                    for (CartItem item : sortedItems) {
                        if (remainingDiscountedItems <= 0) break;
                        
                        int discountedForThisItem = Math.min(remainingDiscountedItems, item.getQuantity() / 2);
                        BigDecimal discountAmount = item.getUnitPrice()
                                .multiply(BigDecimal.ONE.subtract(discountRate))
                                .multiply(BigDecimal.valueOf(discountedForThisItem));
                        
                        item.setAppliedDiscount(discount);
                        item.setDiscountAmount(discountAmount);
                        item.calculateTotalPrice();
                        
                        remainingDiscountedItems -= discountedForThisItem;
                    }
                }
                break;
        }
    }
} 