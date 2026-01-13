package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.entity.Merchant;
import com.core.cashin.commons.entity.MerchantCostEntity;
import com.core.cashin.commons.entity.PaymentFeeEntity;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.repository.MerchantCostRepository;
import com.core.cashin.routing.service.PaymentFeeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentFeeServiceImpl implements PaymentFeeService {

    @PersistenceContext
    private EntityManager entityManager;

    private final MerchantCostRepository merchantCostRepository;

    public PaymentFeeServiceImpl(MerchantCostRepository merchantCostRepository) {
        this.merchantCostRepository = merchantCostRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentFeeEntity calculatePaymentFee(DepositRequest request) {
        BigDecimal amount = request.getAmount();
        MerchantCostEntity merchantCostEntity = merchantCostRepository.findByMerchantId(request.getMerchant().getMerchantId()).
                orElseThrow(() -> new NotFoundException("Merchant fee configuration not found"));
        PaymentFeeEntity paymentFeeEntity = null;
        Merchant merchant = entityManager.getReference(Merchant.class, request.getMerchant().getMerchantId());

        switch (merchantCostEntity.getFeeType()) {
            case FIXED -> {
                BigDecimal grossAmount = amount.add(merchantCostEntity.getFixRate());
                paymentFeeEntity = buildPaymentFeeEntity(grossAmount, merchantCostEntity.getFixRate(),
                        request, merchant);
            }
            case PERCENTAGE -> {
                BigDecimal percentage = request.getAmount().multiply(BigDecimal.valueOf(merchantCostEntity.getFeeRate()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                paymentFeeEntity = buildPaymentFeeEntity(percentage.add(amount), percentage, request, merchant);
            }
            case MIXED -> {
                BigDecimal mixed = request.getAmount().multiply(BigDecimal.valueOf(merchantCostEntity.getFeeRate()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                mixed = mixed.add(merchantCostEntity.getFixRate());
                paymentFeeEntity = buildPaymentFeeEntity(mixed.add(amount), mixed, request, merchant);
            }
        }

        log.info("Fee calculated successfully");
        return paymentFeeEntity;
    }


    private PaymentFeeEntity buildPaymentFeeEntity(BigDecimal grossAmount, BigDecimal feeAmount, DepositRequest request,
                                                   Merchant merchant) {
        return PaymentFeeEntity.builder()
                .grossAmount(grossAmount)
                .feeAmount(feeAmount)
                .netAmount(request.getAmount())
                .currency(request.getCurrency())
                .createdAt(LocalDateTime.now())
                .merchant(merchant)
                .build();
    }

}
