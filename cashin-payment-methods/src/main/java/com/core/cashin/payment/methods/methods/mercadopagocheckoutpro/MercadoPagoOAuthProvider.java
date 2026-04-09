package com.core.cashin.payment.methods.methods.mercadopagocheckoutpro;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.InternalServerException;
import com.core.cashin.commons.model.OAuthRequest;
import com.core.cashin.commons.model.OAuthTokenResponse;
import com.core.cashin.commons.service.OAuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class MercadoPagoOAuthProvider implements OAuthProvider {

    private static final String AUTH_URL = "https://auth.mercadopago.com/authorization";
    private static final String TOKEN_URL = "https://api.mercadopago.com/oauth/token";

    @Value("${mercadopago.client-id}")
    private String clientId;

    @Value("${mercadopago.client-secret}")
    private String clientSecret;

    @Value("${mercadopago.redirect-uri}")
    private String redirectUri;

    private final RestClient restClient;

    public MercadoPagoOAuthProvider() {
        this.restClient = RestClient.create();
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @Override
    public String buildAuthUrl(OAuthRequest request) {
        return AUTH_URL
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&platform_id=mp"
                + "&state=" + request.getState()
                + "&redirect_uri=" + redirectUri;
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code, String state, Long merchantId) {
        Map<String, String> body = Map.of(
                "grant_type", "authorization_code",
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code,
                "redirect_uri", redirectUri
        );

        return callTokenEndpoint(body);
    }

    @Override
    public OAuthTokenResponse refreshToken(String refreshToken, Long merchantId) {
        Map<String, String> body = Map.of(
                "grant_type", "refresh_token",
                "client_id", clientId,
                "client_secret", clientSecret,
                "refresh_token", refreshToken
        );

        return callTokenEndpoint(body);
    }

    private OAuthTokenResponse callTokenEndpoint(Map<String, String> body) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new InternalServerException("Empty response from MercadoPago OAuth token endpoint");
            }

            return OAuthTokenResponse.builder()
                    .accessToken((String) response.get("access_token"))
                    .refreshToken((String) response.get("refresh_token"))
                    .tokenType((String) response.get("token_type"))
                    .expiresIn(response.get("expires_in") != null
                            ? ((Number) response.get("expires_in")).longValue() : null)
                    .scope((String) response.get("scope"))
                    .userId(response.get("user_id") != null
                            ? ((Number) response.get("user_id")).longValue() : null)
                    .publicKey((String) response.get("public_key"))
                    .liveMode((Boolean) response.get("live_mode"))
                    .build();
        } catch (InternalServerException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MercadoPagoOAuth] Error calling token endpoint: {}", e.getMessage(), e);
            throw new InternalServerException("Error exchanging OAuth token with MercadoPago", e);
        }
    }

}
