package com.core.cashin.commons.mapper;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.entity.*;
import com.core.cashin.commons.model.DepositRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
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

    public PaymentEntity buildPaymentEntity(DepositRequest request, PayerEntity payerEntity, PaymentFeeEntity paymentFeeEntity,
                                            String idPayment, HttpServletRequest httpServletRequest) {

        LocalDateTime now = LocalDateTime.now();

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
                .ip(validateIp(request, httpServletRequest))
                .transactionId(idPayment)
                .gateway(gateway)
                .merchant(merchant)
                .payerEntity(payerEntity)
                .paymentFeeEntity(paymentFeeEntity)
                .build();
    }


    private String validateIp(DepositRequest request, HttpServletRequest httpServletRequest) {
        String ip = request.getIp();
        if (ip != null && !ip.isBlank() && isValidIp(ip)) {
            return ip;
        }

        return httpServletRequest.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address != null;
        } catch (Exception e) {
            return false;
        }
    }


}
