package com.core.cashin.commons.mapper;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.entity.Gateway;
import com.core.cashin.commons.entity.Merchant;
import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.model.DepositRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CashinMapper {

    public PayerEntity buildPayerEntity(DepositRequest request) {
        return PayerEntity.builder()
                .document(request.getPayer().getDocument())
                .documentType(request.getPayer().getDocumentType())
                .firstName(request.getPayer().getFirstName())
                .lastName(request.getPayer().getLastName())
                .email(request.getPayer().getEmail())
                .build();
    }

    public PaymentEntity buildPaymentEntity(DepositRequest request, PayerEntity payerEntity, String idPayment, LocalDateTime now) {

        Merchant merchant = Merchant.builder()
                .id(request.getMerchant().getMerchantId())
                .build();

        Gateway gateway = Gateway.builder()
                .id(request.getGatewayId())
                .build();

        return PaymentEntity.builder()
                .amount(request.getAmount())
                .country(request.getCountry())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodName(request.getPaymentMethodName())
                .description(idPayment)
                .notificationUrl(request.getNotificationUrl())
                .successUrl(request.getSuccessUrl())
                .errorUrl(request.getErrorUrl())
                .createdAt(now)
                .updatedAt(now)
                .status(PaymentStatusEnum.CREATED)
                .ip("TEST IP")
                .transactionId(idPayment)
                .gateway(gateway)
                .merchant(merchant)
                .payerEntity(payerEntity)
                .build();
    }


}
