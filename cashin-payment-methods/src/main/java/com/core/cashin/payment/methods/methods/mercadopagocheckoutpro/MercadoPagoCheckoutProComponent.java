package com.core.cashin.payment.methods.methods.mercadopagocheckoutpro;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.exception.InternalServerException;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MercadoPagoCheckoutProComponent {

    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @CircuitBreaker(name = "mercadoPagoCB", fallbackMethod = "fallbackCallProvider")
    public Preference callProvider(PreferenceRequest preferenceRequest, MPRequestOptions mpRequestOptions) {
        try {
            PreferenceClient client = new PreferenceClient();
            return client.create(preferenceRequest, mpRequestOptions);
        } catch (MPApiException e) {
            if (e.getStatusCode() < 500) {
                log.error("[MP] callProvider failed status={} connector={} message={}",
                        e.getStatusCode(), getConnector().name(), e.getMessage());
                throw new BadRequestException("Invalid data sent to MercadoPago", e);
            }
            log.error("[MP] callProvider provider error status={} connector={}", e.getStatusCode(), getConnector().name(), e);
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        } catch (MPException e) {
            log.error("[MP] callProvider connection error connector={} cause={}", getConnector().name(), e.getMessage(), e);
            throw new InternalServerException("PROVIDER ERROR: " + getConnector().name(), e);
        }
    }

    public Preference fallbackCallProvider(PreferenceRequest preferenceRequest, MPRequestOptions mpRequestOptions, Throwable t) {
        log.error("[MP] circuit breaker open connector={} cause={}", getConnector().name(), t.getMessage(), t);
        throw new InternalServerException("Payment system " + getConnector().name() + " is not currently available", t);
    }

}
