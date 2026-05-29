package com.core.cashin.payment.methods.methods.stripe;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "stripe-oauth", url = "${stripe.connect-url}")
public interface StripeOAuthClient {

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    StripeTokenResponse exchangeToken(@RequestBody Map<String, String> body);

}
