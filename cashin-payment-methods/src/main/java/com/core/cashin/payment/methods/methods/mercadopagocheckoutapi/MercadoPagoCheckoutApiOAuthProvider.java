package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.OAuthRequest;
import com.core.cashin.commons.model.OAuthTokenResponse;
import com.core.cashin.commons.service.OAuthProvider;
import com.core.cashin.payment.methods.methods.mercadopagocheckoutpro.MercadoPagoOAuthProvider;
import org.springframework.stereotype.Service;

@Service
public class MercadoPagoCheckoutApiOAuthProvider implements OAuthProvider {

    private final MercadoPagoOAuthProvider delegate;

    public MercadoPagoCheckoutApiOAuthProvider(MercadoPagoOAuthProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_API;
    }

    @Override
    public String buildAuthUrl(OAuthRequest request) {
        return delegate.buildAuthUrl(request);
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code, String state, Long merchantId) {
        return delegate.exchangeCode(code, state, merchantId);
    }

    @Override
    public OAuthTokenResponse refreshToken(String refreshToken, Long merchantId) {
        return delegate.refreshToken(refreshToken, merchantId);
    }

}
