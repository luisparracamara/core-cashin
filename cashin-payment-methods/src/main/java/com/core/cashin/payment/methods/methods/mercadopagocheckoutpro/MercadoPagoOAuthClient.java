package com.core.cashin.payment.methods.methods.mercadopagocheckoutpro;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "mercadopago-oauth", url = "${mercadopago.api-url}")
public interface MercadoPagoOAuthClient {

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    MercadoPagoTokenResponse exchangeToken(@RequestBody Map<String, String> body);

}
