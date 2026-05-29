package com.core.cashin.payment.methods.methods.stripe;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.exception.InternalServerException;
import com.core.cashin.commons.exception.NotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StripeCheckoutComponent {

    public ConnectorEnum getConnector() {
        return ConnectorEnum.STRIPE;
    }

    @CircuitBreaker(name = "stripeCB", fallbackMethod = "fallbackCreatePaymentIntent")
    public PaymentIntent createPaymentIntent(long amountInCents, String currency,
                                              String merchantAccountId, String idempotencyKey,
                                              boolean automaticPaymentMethods) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(merchantAccountId)
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            PaymentIntentCreateParams params = buildParams(amountInCents, currency, automaticPaymentMethods);

            PaymentIntent intent = PaymentIntent.create(params, options);
            log.debug("[StripeCheckout] PaymentIntent created id={} status={} merchantAccount={}",
                    intent.getId(), intent.getStatus(), merchantAccountId);
            return intent;
        } catch (StripeException e) {
            if (e.getStatusCode() != null && e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                log.error("[StripeCheckout] createPaymentIntent failed status={} message={}",
                        e.getStatusCode(), e.getMessage());
                throw new BadRequestException("Payment failed [" + e.getStatusCode() + "]: " + e.getMessage(), e);
            }
            log.error("[StripeCheckout] createPaymentIntent provider error status={} connector={}",
                    e.getStatusCode(), getConnector().name(), e);
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    private PaymentIntentCreateParams buildParams(long amountInCents, String currency,
                                                   boolean automaticPaymentMethods) {
        PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase());

        if (automaticPaymentMethods) {
            // Apple Pay, Google Pay, Link, cards — Stripe detecta según browser y país
            builder.setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
            );
        } else {
            // Solo tarjeta — control explícito
            builder.addPaymentMethodType(StripeConstants.PAYMENT_METHOD_TYPE_CARD);
        }

        return builder.build();
    }

    public PaymentIntent fallbackCreatePaymentIntent(long amountInCents, String currency,
                                                      String merchantAccountId, String idempotencyKey,
                                                      boolean automaticPaymentMethods, Throwable t) {
        if (t instanceof BadRequestException badRequest) {
            throw badRequest;
        }
        log.error("[StripeCheckout] circuit breaker open connector={} cause={}", getConnector().name(), t.getMessage(), t);
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }

    @CircuitBreaker(name = "stripeCB", fallbackMethod = "fallbackGetPaymentIntent")
    public PaymentIntent getPaymentIntent(String paymentIntentId, String merchantAccountId) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(merchantAccountId)
                    .build();

            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId, options);
            log.debug("[StripeCheckout] PaymentIntent retrieved id={} status={}", intent.getId(), intent.getStatus());
            return intent;
        } catch (StripeException e) {
            if (e.getStatusCode() != null && e.getStatusCode() == 404) {
                log.warn("[StripeCheckout] PaymentIntent not found id={}", paymentIntentId);
                throw new NotFoundException("PaymentIntent not found: " + paymentIntentId);
            }
            log.error("[StripeCheckout] getPaymentIntent failed id={} status={}", paymentIntentId, e.getStatusCode(), e);
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    public PaymentIntent fallbackGetPaymentIntent(String paymentIntentId, String merchantAccountId, Throwable t) {
        if (t instanceof NotFoundException notFound) {
            throw notFound;
        }
        log.error("[StripeCheckout] circuit breaker open connector={} paymentIntentId={} cause={}",
                getConnector().name(), paymentIntentId, t.getMessage(), t);
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }
}
