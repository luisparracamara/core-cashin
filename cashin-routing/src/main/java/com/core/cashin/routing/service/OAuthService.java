package com.core.cashin.routing.service;

import com.core.cashin.commons.model.OAuthTokenResponse;

public interface OAuthService {

    String getAuthUrl(String connector, Long merchantId, String state);

    OAuthTokenResponse handleCallback(String code, String state);

    OAuthTokenResponse refresh(String connector, Long merchantId);

    boolean isConnected(String connector, Long merchantId);

    void disconnect(String connector, Long merchantId);

}
