package com.core.cashin.payment.methods.methods.stripe;

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
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class StripeCheckoutDirector implements PaymentRedirector {

    private final StripeCheckoutComponent component;
    private final StripeCheckoutMapper mapper;
    private final MetadataService metadataService;
    private final PaymentOperationService paymentOperationService;
    private final Utils utils;

    public StripeCheckoutDirector(StripeCheckoutComponent component,
                                   StripeCheckoutMapper mapper,
                                   MetadataService metadataService,
                                   PaymentOperationService paymentOperationService,
                                   Utils utils) {
        this.component = component;
        this.mapper = mapper;
        this.metadataService = metadataService;
        this.paymentOperationService = paymentOperationService;
        this.utils = utils;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.STRIPE;
    }

    @Override
    public CashInMethod getCashInMethod() {
        return CashInMethod.CREDIT_CARD;
    }

    @Override
    public DepositResponse create(DepositRequest request, PaymentEntity paymentEntity) {
        Long merchantId = request.getMerchant().getMerchantId();
        String merchantAccountId = resolveMerchantAccountId(request, merchantId);

        long amountInCents = mapper.toStripeCents(request.getAmount());
        String idempotencyKey = MDC.get("correlationId");
        boolean automatic = isAutomaticMode(request);

        log.info("[StripeCheckout] creating PaymentIntent paymentId={} amount={} cents={} currency={} merchantAccount={} automaticPaymentMethods={}",
                paymentEntity.getId(), request.getAmount(), amountInCents, request.getCurrency(), merchantAccountId, automatic);

        PaymentIntent intent = component.createPaymentIntent(
                amountInCents, request.getCurrency(), merchantAccountId, idempotencyKey, automatic);

        log.debug("[StripeCheckout] PaymentIntent id={} clientSecret=***", intent.getId());

        DepositResponse response = mapper.buildDepositResponse(request, intent, paymentEntity.getId());

        String intentSnapshot = utils.toJson(mapper.toSnapshot(intent));
        PaymentCashinEntity cashinEntity = mapper.buildPaymentCashinEntity(
                intent, paymentEntity, paymentEntity.getCreatedAt(), intentSnapshot);
        paymentOperationService.savePaymentCashinPending(paymentEntity, cashinEntity);

        return response;
    }

    @Override
    public boolean checkStatus(String paymentIntentId, Long merchantId) {
        Map<String, String> metadata = metadataService.retrieveGatewayMetadata(
                getConnector().getName(), merchantId);
        String merchantAccountId = metadata.get(GatewayMetadataEnum.ACCOUNT_ID.name());

        if (merchantAccountId == null) {
            log.warn("[StripeCheckout] checkStatus: no accountId found merchantId={}", merchantId);
            throw new BadRequestException("Stripe account not connected for merchantId: " + merchantId);
        }

        PaymentIntent intent = component.getPaymentIntent(paymentIntentId, merchantAccountId);
        log.debug("[StripeCheckout] checkStatus paymentIntentId={} status={}", paymentIntentId, intent.getStatus());

        return "succeeded".equals(intent.getStatus());
    }

    /**
     * automatic = true  → Apple Pay, Google Pay, Link, cards (Stripe detecta por browser/país)
     * automatic = false → solo tarjeta (default)
     */
    private boolean isAutomaticMode(DepositRequest request) {
        if (request.getPaymentData() == null) return false;
        return "automatic".equalsIgnoreCase(request.getPaymentData().get("mode"));
    }

    /**
     * Resolves the merchant's Stripe account ID (acct_xxx).
     * First tries gatewayMetadata from the request (already loaded by routing layer),
     * falls back to fetching directly from MetadataService.
     */
    private String resolveMerchantAccountId(DepositRequest request, Long merchantId) {
        Map<String, String> gatewayMetadata = request.getGatewayMetadata();
        if (gatewayMetadata != null) {
            String accountId = gatewayMetadata.get(GatewayMetadataEnum.ACCOUNT_ID.name());
            if (accountId != null && !accountId.isBlank()) {
                return accountId;
            }
        }
        // fallback
        Map<String, String> storedMetadata = metadataService.retrieveGatewayMetadata(
                getConnector().getName(), merchantId);
        String accountId = storedMetadata.get(GatewayMetadataEnum.ACCOUNT_ID.name());
        if (accountId == null || accountId.isBlank()) {
            log.error("[StripeCheckout] Stripe account not connected merchantId={}", merchantId);
            throw new BadRequestException("Stripe account not connected for merchantId: " + merchantId);
        }
        return accountId;
    }
}
