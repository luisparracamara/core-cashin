package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.exception.InternalServerException;
import com.core.cashin.commons.exception.NotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
                throw new BadRequestException("Payment failed [" + e.status() + "]: " + e.contentUTF8(), e);
            }
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    public MercadoPagoOrderResponse fallbackCreateOrder(MercadoPagoOrderRequest request, String accessToken,
                                                         String platformId, String idempotencyKey, Throwable t) {
        if (t instanceof BadRequestException badRequest) {
            throw badRequest;
        }
        log.error("Circuit Breaker fallback in: {} {}", getConnector().name(), t.getMessage());
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }

    @CircuitBreaker(name = "mercadoPagoCheckoutApiCB", fallbackMethod = "fallbackGetOrder")
    public MercadoPagoOrderResponse getOrder(String orderId, String accessToken) {
        try {
            String authorization = MercadoPagoCheckoutApiConstants.BEARER_PREFIX + accessToken;
            return client.getOrder(authorization, orderId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Order not found: " + orderId);
        } catch (FeignException e) {
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    public MercadoPagoOrderResponse fallbackGetOrder(String orderId, String accessToken, Throwable t) {
        if (t instanceof NotFoundException notFound) {
            throw notFound;
        }
        log.error("Circuit Breaker fallback in: {} {}", getConnector().name(), t.getMessage());
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }

    public MercadoPagoOrderResponse pollForTicketUrl(String orderId, String accessToken) {
        int maxRetries = 3;
        int delayMs = 2000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
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
