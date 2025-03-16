package Altech.checkout.service;

import Altech.checkout.model.Cart;
import java.util.List;
import java.util.Map;

public interface CartService {
    Cart createCart();
    Cart getCartById(Long id);
    List<Cart> getAllCarts();
    void deleteCart(Long id);
    Cart addItemToCart(Long cartId, Long productId, Integer quantity);
    Cart removeItemFromCart(Long cartId, Long cartItemId);
    Cart updateCartItemQuantity(Long cartId, Long cartItemId, Integer quantity);
    Cart applyDiscounts(Long cartId);
    Map<String, Object> generateReceipt(Long cartId);
} 