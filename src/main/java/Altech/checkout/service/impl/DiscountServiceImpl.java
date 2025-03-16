package Altech.checkout.service.impl;

import Altech.checkout.model.Discount;
import Altech.checkout.repository.DiscountRepository;
import Altech.checkout.service.DiscountService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;

    @Override
    public Discount createDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    @Override
    public Discount getDiscountById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with id: " + id));
    }

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    @Override
    public Discount updateDiscount(Long id, Discount discountDetails) {
        Discount discount = getDiscountById(id);
        discount.setName(discountDetails.getName());
        discount.setDescription(discountDetails.getDescription());
        discount.setType(discountDetails.getType());
        discount.setValue(discountDetails.getValue());
        discount.setMinQuantity(discountDetails.getMinQuantity());
        discount.setStartDate(discountDetails.getStartDate());
        discount.setEndDate(discountDetails.getEndDate());
        discount.setIsActive(discountDetails.getIsActive());
        return discountRepository.save(discount);
    }

    @Override
    public void deleteDiscount(Long id) {
        Discount discount = getDiscountById(id);
        discount.setIsActive(false);
        discountRepository.save(discount);
    }
} 