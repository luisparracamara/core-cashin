package com.core.cashin.commons.service;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.OAuthRequest;
import com.core.cashin.commons.model.OAuthTokenResponse;

public interface OAuthProvider {

    ConnectorEnum getConnector();

    String buildAuthUrl(OAuthRequest request);

    OAuthTokenResponse exchangeCode(String code, String state, Long merchantId);

    OAuthTokenResponse refreshToken(String refreshToken, Long merchantId);

}
