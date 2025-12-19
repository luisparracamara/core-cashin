package com.core.cashin.payment.methods.methods;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.service.PaymentRedirector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeRedirector implements PaymentRedirector {

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.CONNECTOR;
    }

    @Override
    public void redirect(DepositRequest request) {
        log.debug("[StripeRedirector] StripeRedirector {}", getConnector());

        // lógica Stripe

    }
}