package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mercadopago-checkout-api", url = "${mercadopago.api-url}")
public interface MercadoPagoCheckoutApiClient {

    @PostMapping(value = "/v1/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    MercadoPagoOrderResponse createOrder(
            @RequestHeader(MercadoPagoCheckoutApiConstants.HEADER_AUTHORIZATION) String authorization,
            @RequestHeader(MercadoPagoCheckoutApiConstants.HEADER_PLATFORM_ID) String platformId,
            @RequestHeader(MercadoPagoCheckoutApiConstants.HEADER_IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestBody MercadoPagoOrderRequest body
    );

    @GetMapping("/v1/orders/{orderId}")
    MercadoPagoOrderResponse getOrder(
            @RequestHeader(MercadoPagoCheckoutApiConstants.HEADER_AUTHORIZATION) String authorization,
            @PathVariable("orderId") String orderId
    );

}
