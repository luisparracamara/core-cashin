package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import com.core.cashin.commons.constants.CashInMethod;
import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.constants.GatewayMetadataEnum;
import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.service.MetadataService;
import com.core.cashin.commons.service.PaymentOperationService;
import com.core.cashin.commons.service.PaymentRedirector;
import com.core.cashin.commons.utils.Utils;
import com.core.cashin.payment.methods.mapper.MercadoPagoCheckoutApiMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class MercadoPagoCheckoutApiDirector implements PaymentRedirector {

    private final Utils utils;
    private final MercadoPagoCheckoutApiMapper mapper;
    private final PaymentOperationService paymentOperationService;
    private final MetadataService metadataService;
    private final MercadoPagoCheckoutApiComponent component;

    public MercadoPagoCheckoutApiDirector(Utils utils, MercadoPagoCheckoutApiMapper mapper,
                                           PaymentOperationService paymentOperationService,
                                           MetadataService metadataService,
                                           MercadoPagoCheckoutApiComponent component) {
        this.utils = utils;
        this.mapper = mapper;
        this.paymentOperationService = paymentOperationService;
        this.metadataService = metadataService;
        this.component = component;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_API;
    }

    @Override
    public CashInMethod getCashInMethod() {
        return CashInMethod.CREDIT_CARD;
    }

    @Override
    public DepositResponse create(DepositRequest request, PaymentEntity paymentEntity) {
        log.debug("[MercadoPagoCheckoutApi] create connector={}", getConnector());
        log.debug("[MercadoPagoCheckoutApi] request={}", utils.toJson(request));

        validatePaymentData(request);

        String accessToken = request.getGatewayMetadata().get(GatewayMetadataEnum.ACCESS_TOKEN.name());
        String platformId = request.getGatewayMetadata().get(GatewayMetadataEnum.PLATFORM_ID.name());
        String paymentId = String.valueOf(paymentEntity.getId());
        CashInMethod cashInMethod = resolveCashInMethod(request);

        String idempotencyKey = MDC.get("correlationId");
        MercadoPagoOrderRequest orderRequest = mapper.buildOrderRequest(request, paymentId);
        log.debug("[MercadoPagoCheckoutApi] orderRequest={}", utils.toJson(orderRequest));

        MercadoPagoOrderResponse orderResponse = component.createOrder(orderRequest, accessToken, platformId, idempotencyKey);
        log.debug("[MercadoPagoCheckoutApi] orderResponse={}", utils.toJson(orderResponse));

        if (CashInMethod.BANK_TRANSFER.equals(cashInMethod) || CashInMethod.VOUCHER.equals(cashInMethod)) {
            orderResponse = component.pollForTicketUrl(orderResponse.id(), accessToken);
            log.debug("[MercadoPagoCheckoutApi] orderResponse after polling={}", utils.toJson(orderResponse));
        }

        DepositResponse response = mapper.buildDepositResponse(request, orderResponse, paymentEntity.getId(),
                cashInMethod.name());

        PaymentCashinEntity paymentCashinEntity = mapper.buildPaymentCashinEntity(
                orderResponse, paymentEntity, paymentEntity.getCreatedAt(), utils.toJson(orderResponse));
        paymentOperationService.savePaymentCashinPending(paymentEntity, paymentCashinEntity);

        log.debug("[MercadoPagoCheckoutApi] saved paymentId={} orderId={}",
                paymentEntity.getId(), orderResponse.id());

        return response;
    }

    @Override
    public boolean checkStatus(String orderId, Long merchantId) {
        Map<String, String> gatewayMetadata = metadataService.retrieveGatewayMetadata(
                getConnector().getName(), merchantId);
        String accessToken = gatewayMetadata.get(GatewayMetadataEnum.ACCESS_TOKEN.name());

        MercadoPagoOrderResponse order = component.getOrder(orderId, accessToken);
        log.debug("[MercadoPagoCheckoutApi] checkStatus orderId={} status={} detail={}",
                orderId, order.status(), order.statusDetail());

        return "processed".equals(order.status());
    }

    private void validatePaymentData(DepositRequest request) {
        if (request.getPayer() == null || request.getPayer().getEmail() == null || request.getPayer().getEmail().isBlank()) {
            throw new BadRequestException("payer.email is required for " + getConnector().name());
        }

        Map<String, String> paymentData = request.getPaymentData();
        if (paymentData == null || paymentData.isEmpty()) {
            throw new BadRequestException("paymentData is required for " + getConnector().name());
        }
        String cardType = paymentData.get(MercadoPagoCheckoutApiConstants.CARD_TYPE);
        if (cardType == null || cardType.isBlank()) {
            throw new BadRequestException("paymentData.cardType is required for " + getConnector().name());
        }
        String cardBrand = paymentData.get(MercadoPagoCheckoutApiConstants.CARD_BRAND);
        if (cardBrand == null || cardBrand.isBlank()) {
            throw new BadRequestException("paymentData.id (card brand) is required for " + getConnector().name());
        }
        if (MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_TICKET.equals(cardType)) {
            if (!MercadoPagoCheckoutApiConstants.PAYMENT_METHOD_OXXO.equals(cardBrand)) {
                throw new BadRequestException("Only oxxo is supported for ticket payments");
            }
        } else if (!MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_ACCOUNT_MONEY.equals(cardType)
                && !MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_BANK_TRANSFER.equals(cardType)) {
            String cardToken = paymentData.get(MercadoPagoCheckoutApiConstants.CARD_TOKEN);
            if (cardToken == null || cardToken.isBlank()) {
                throw new BadRequestException("paymentData.cardToken is required for card payments");
            }
        }
    }

    private CashInMethod resolveCashInMethod(DepositRequest request) {
        String cardType = request.getPaymentData().get(MercadoPagoCheckoutApiConstants.CARD_TYPE);
        return switch (cardType) {
            case MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_CREDIT -> CashInMethod.CREDIT_CARD;
            case MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_DEBIT -> CashInMethod.DEBIT_CARD;
            case MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_ACCOUNT_MONEY -> CashInMethod.WALLET;
            case MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_BANK_TRANSFER -> CashInMethod.BANK_TRANSFER;
            case MercadoPagoCheckoutApiConstants.PAYMENT_TYPE_TICKET -> CashInMethod.VOUCHER;
            default -> throw new BadRequestException("Unsupported cardType: " + cardType);
        };
    }

}
