package com.core.cashin.payment.methods.methods.stripe;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.platform-secret-key}")
    private String platformSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = platformSecretKey;
        log.info("[StripeConfig] Stripe SDK initialized with platform key");
    }
}
