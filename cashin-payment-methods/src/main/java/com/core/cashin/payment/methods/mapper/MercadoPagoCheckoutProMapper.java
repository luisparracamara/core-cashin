package com.core.cashin.payment.methods.mapper;

import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.model.DepositMetadataResponse;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.model.PaymentInfoResponse;
import com.core.cashin.payment.methods.model.MercadoPagoCheckoutProPreferenceResponse;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoCheckoutProMapper {

    public PreferenceRequest buildPreferenceRequest(DepositRequest request, String idPayment) {
        Instant userDateTime = Instant.parse(request.getDate());

        PreferenceItemRequest itemRequest =
                PreferenceItemRequest.builder()
                        .id(idPayment)
                        .title(request.getMerchant().getMerchantName())
                        .description("Reference: " + idPayment)
                        .quantity(1)
                        .currencyId(request.getCurrency())
                        .unitPrice(new BigDecimal(request.getAmount()))
                        .categoryId("services")
                        .build();
        List<PreferenceItemRequest> items = List.of(itemRequest);

        PreferenceBackUrlsRequest backUrlsRequest = PreferenceBackUrlsRequest.builder()
                .success(request.getSuccessUrl())
                .failure(request.getErrorUrl())
                .build();

        PreferencePayerRequest payerRequest = PreferencePayerRequest.builder()
                .name(request.getPayer().getFirstName())
                .surname(request.getPayer().getLastName())
                .email(request.getPayer().getEmail())
                .build();

        return PreferenceRequest.builder()
                .items(items)
                .binaryMode(true)
                .externalReference(idPayment)
                .notificationUrl(request.getNotificationUrl())
                .backUrls(backUrlsRequest)
                .autoReturn("approved")
                .expires(true)
                .expirationDateTo(userDateTime.plus(Duration.ofHours(24)).atOffset(ZoneOffset.UTC))
                .statementDescriptor(request.getMerchant().getMerchantName())
                .payer(payerRequest)
                .build();

    }

    public MPRequestOptions buildMPRequestOptions(String accessToken, String platformId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Platform-Id", platformId);
        return MPRequestOptions.builder()
                .accessToken(accessToken)
                .customHeaders(headers)
                .build();
    }

    public MPRequestOptions buildMPRequestOptions(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        return MPRequestOptions.builder()
                .accessToken(accessToken)
                .customHeaders(headers)
                .build();
    }

    public DepositResponse buildDepositResponse(DepositRequest request, MercadoPagoCheckoutProPreferenceResponse mercadoPagoResponse,
                                                String idPayment, String cashInMethod) {

        DepositMetadataResponse depositMetadataResponse = DepositMetadataResponse.builder()
                .payerName(request.getPayer().getFirstName() + " " + request.getPayer().getLastName())
                .beneficiaryName(request.getMerchant().getMerchantName())
                .build();

        PaymentInfoResponse paymentInfoResponse = PaymentInfoResponse.builder()
                .type(cashInMethod)
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodName(request.getPaymentMethodName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .createdAt(mercadoPagoResponse.dateCreated())
                .metadata(depositMetadataResponse)
                .build();

        return DepositResponse.builder()
                .depositId(idPayment)
                .checkoutType("ONE_SHOT")
                .redirectUrl(mercadoPagoResponse.redirectUrl())
                .redirectUrlSandbox(mercadoPagoResponse.sandboxInitPoint())
                .paymentInfo(paymentInfoResponse)
                .build();
    }

    public MercadoPagoCheckoutProPreferenceResponse buildMercadoPagoCheckoutProPreferenceResponse(Preference preference) {
        return MercadoPagoCheckoutProPreferenceResponse.builder()
                .additionalInfo(preference.getAdditionalInfo())
                .clientId(preference.getClientId())
                .checkoutCollectorId(preference.getCollectorId())
                .dateCreated(preference.getDateCreated().format(DateTimeFormatter.ISO_DATE_TIME))
                .externalReference(preference.getExternalReference())
                .id(preference.getId())
                .redirectUrl(preference.getInitPoint())
                .notificationUrl(preference.getNotificationUrl())
                .operationType(preference.getOperationType())
                .sandboxInitPoint(preference.getSandboxInitPoint())
                .build();
    }


    public PaymentCashinEntity buildPaymentCashinEntity(MercadoPagoCheckoutProPreferenceResponse mercadoPagoResponse, PaymentEntity paymentEntity,
                                                        LocalDateTime now, String json) {
        return PaymentCashinEntity.builder()
                .data(json)
                .externalReference(String.valueOf(mercadoPagoResponse.id()))
                .createdAt(now)
                .updatedAt(now)
                .paymentEntity(paymentEntity)
                .build();
    }
}
