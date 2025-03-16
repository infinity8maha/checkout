package Altech.checkout.service;

import Altech.checkout.model.Discount;
import java.util.List;

public interface DiscountService {
    Discount createDiscount(Discount discount);
    Discount getDiscountById(Long id);
    List<Discount> getAllDiscounts();
    Discount updateDiscount(Long id, Discount discount);
    void deleteDiscount(Long id);
} 