package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.exception.InternalServerException;
import com.core.cashin.commons.exception.NotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MercadoPagoCheckoutApiComponent {

    private final MercadoPagoCheckoutApiClient client;

    public MercadoPagoCheckoutApiComponent(MercadoPagoCheckoutApiClient client) {
        this.client = client;
    }

    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_API;
    }

    @CircuitBreaker(name = "mercadoPagoCheckoutApiCB", fallbackMethod = "fallbackCreateOrder")
    public MercadoPagoOrderResponse createOrder(MercadoPagoOrderRequest request, String accessToken,
                                                 String platformId, String idempotencyKey) {
        try {
            String authorization = MercadoPagoCheckoutApiConstants.BEARER_PREFIX + accessToken;
            return client.createOrder(authorization, platformId, idempotencyKey, request);
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                log.error("[MP] createOrder failed status={} connector={} body={}", e.status(), getConnector().name(), e.contentUTF8());
                throw new BadRequestException("Payment failed [" + e.status() + "]: " + e.contentUTF8(), e);
            }
            log.error("[MP] createOrder provider error status={} connector={}", e.status(), getConnector().name(), e);
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    public MercadoPagoOrderResponse fallbackCreateOrder(MercadoPagoOrderRequest request, String accessToken,
                                                         String platformId, String idempotencyKey, Throwable t) {
        if (t instanceof BadRequestException badRequest) {
            throw badRequest;
        }
        log.error("[MP] circuit breaker open connector={} cause={}", getConnector().name(), t.getMessage(), t);
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }

    @CircuitBreaker(name = "mercadoPagoCheckoutApiCB", fallbackMethod = "fallbackGetOrder")
    public MercadoPagoOrderResponse getOrder(String orderId, String accessToken) {
        try {
            String authorization = MercadoPagoCheckoutApiConstants.BEARER_PREFIX + accessToken;
            return client.getOrder(authorization, orderId);
        } catch (FeignException.NotFound e) {
            log.warn("[MP] order not found orderId={}", orderId);
            throw new NotFoundException("Order not found: " + orderId);
        } catch (FeignException e) {
            log.error("[MP] getOrder failed status={} orderId={} connector={}", e.status(), orderId, getConnector().name(), e);
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    public MercadoPagoOrderResponse fallbackGetOrder(String orderId, String accessToken, Throwable t) {
        if (t instanceof NotFoundException notFound) {
            throw notFound;
        }
        log.error("[MP] circuit breaker open connector={} orderId={} cause={}", getConnector().name(), orderId, t.getMessage(), t);
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }

    public MercadoPagoOrderResponse pollForTicketUrl(String orderId, String accessToken) {
        int maxRetries = 3;
        int delayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                log.warn("[MP] pollForTicketUrl interrupted orderId={} attempt={}", orderId, attempt);
                Thread.currentThread().interrupt();
                break;
            }

            MercadoPagoOrderResponse order = getOrder(orderId, accessToken);
            log.debug("[MercadoPagoCheckoutApi] pollForTicketUrl attempt={} status={}", attempt, order.status());

            boolean hasTicketUrl = order.transactions() != null
                    && order.transactions().payments() != null
                    && !order.transactions().payments().isEmpty()
                    && order.transactions().payments().get(0).paymentMethod() != null
                    && order.transactions().payments().get(0).paymentMethod().ticketUrl() != null;

            if (hasTicketUrl) {
                return order;
            }
        }

        log.warn("[MercadoPagoCheckoutApi] pollForTicketUrl orderId={} ticket_url not available after {} retries", orderId, maxRetries);
        return getOrder(orderId, accessToken);
    }

}
