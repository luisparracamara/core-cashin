package com.core.cashin.payment.methods.methods.stripe;

import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.model.DepositMetadataResponse;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.model.PaymentInfoResponse;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
public class StripeCheckoutMapper {

    /**
     * Converts amount from decimal (e.g. 100.00 MXN) to cents (10000) as required by Stripe.
     */
    public long toStripeCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    public DepositResponse buildDepositResponse(DepositRequest request, PaymentIntent intent, Long paymentId) {
        DepositMetadataResponse metadata = DepositMetadataResponse.builder()
                .payerName(request.getPayer().getFirstName() + " " + request.getPayer().getLastName())
                .beneficiaryName(request.getMerchant().getMerchantName())
                .build();

        PaymentInfoResponse paymentInfo = PaymentInfoResponse.builder()
                .type(StripeConstants.CHECKOUT_TYPE)
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodName(request.getPaymentMethodName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .createdAt(Instant.ofEpochSecond(intent.getCreated()).toString())
                .metadata(metadata)
                .build();

        return DepositResponse.builder()
                .depositId(paymentId)
                .checkoutType(StripeConstants.CHECKOUT_TYPE)
                // client_secret is returned so the frontend can confirm via Stripe.js
                .redirectUrl(intent.getClientSecret())
                .paymentInfo(paymentInfo)
                .build();
    }

    public PaymentCashinEntity buildPaymentCashinEntity(PaymentIntent intent, PaymentEntity paymentEntity,
                                                         LocalDateTime now, String rawJson) {
        return PaymentCashinEntity.builder()
                .externalReference(intent.getId())
                .data(rawJson)
                .createdAt(now)
                .updatedAt(now)
                .paymentEntity(paymentEntity)
                .build();
    }

    /**
     * Extracts only serializable fields from PaymentIntent.
     * The full PaymentIntent object contains StripeResponse (lastResponse) which Jackson cannot serialize.
     */
    public Map<String, Object> toSnapshot(PaymentIntent intent) {
        return Map.of(
                "id", intent.getId(),
                "status", intent.getStatus(),
                "amount", intent.getAmount(),
                "currency", intent.getCurrency(),
                "created", intent.getCreated(),
                "paymentMethodTypes", intent.getPaymentMethodTypes()
        );
    }

    public LocalDateTime fromEpochSeconds(Long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }
}
