package com.core.cashin.payment.methods.methods;

import com.core.cashin.commons.constants.CashInMethod;
import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.service.PaymentRedirector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MercadoPagoCheckoutPro implements PaymentRedirector {

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @Override
    public CashInMethod getCashInMethod() {
        return CashInMethod.VOUCHER;
    }

    @Override
    public DepositResponse create(DepositRequest request) {
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro {}", getConnector());

        return null;
    }
}
