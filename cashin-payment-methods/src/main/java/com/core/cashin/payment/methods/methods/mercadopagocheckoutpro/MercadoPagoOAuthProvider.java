package com.core.cashin.payment.methods.methods.mercadopagocheckoutpro;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.exception.InternalServerException;
import com.core.cashin.commons.model.OAuthRequest;
import com.core.cashin.commons.model.OAuthTokenResponse;
import com.core.cashin.commons.service.OAuthProvider;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class MercadoPagoOAuthProvider implements OAuthProvider {


    @Value("${mercadopago.client-id}")
    private String clientId;

    @Value("${mercadopago.client-secret}")
    private String clientSecret;

    @Value("${mercadopago.redirect-uri}")
    private String redirectUri;

    private final MercadoPagoOAuthClient oAuthClient;

    public MercadoPagoOAuthProvider(MercadoPagoOAuthClient oAuthClient) {
        this.oAuthClient = oAuthClient;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @Override
    public String buildAuthUrl(OAuthRequest request) {
        return MercadoPagoConstants.AUTH_URL
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&platform_id=" + MercadoPagoConstants.PLATFORM_ID
                + "&state=" + URLEncoder.encode(request.getState(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code, String state, Long merchantId) {
        Map<String, String> body = Map.of(
                MercadoPagoConstants.GRANT_TYPE, MercadoPagoConstants.GRANT_TYPE_AUTH_CODE,
                MercadoPagoConstants.PARAM_CLIENT_ID, clientId,
                MercadoPagoConstants.PARAM_CLIENT_SECRET, clientSecret,
                MercadoPagoConstants.PARAM_CODE, code,
                MercadoPagoConstants.PARAM_REDIRECT_URI, redirectUri
        );

        return callTokenEndpoint(body);
    }

    @Override
    public OAuthTokenResponse refreshToken(String refreshToken, Long merchantId) {
        Map<String, String> body = Map.of(
                MercadoPagoConstants.GRANT_TYPE, MercadoPagoConstants.GRANT_TYPE_REFRESH,
                MercadoPagoConstants.PARAM_CLIENT_ID, clientId,
                MercadoPagoConstants.PARAM_CLIENT_SECRET, clientSecret,
                MercadoPagoConstants.PARAM_REFRESH_TOKEN, refreshToken
        );

        return callTokenEndpoint(body);
    }

    private OAuthTokenResponse callTokenEndpoint(Map<String, String> body) {
        try {
            MercadoPagoTokenResponse response = oAuthClient.exchangeToken(body);
            return toOAuthTokenResponse(response);
        } catch (FeignException.BadRequest | FeignException.UnprocessableEntity e) {
            log.error("[MercadoPagoOAuth] Invalid request: status={} body={}", e.status(), e.contentUTF8());
            throw new BadRequestException("Invalid OAuth request to MercadoPago: " + e.contentUTF8());
        } catch (FeignException e) {
            log.error("[MercadoPagoOAuth] Error calling token endpoint: status={} body={}", e.status(), e.contentUTF8());
            throw new InternalServerException("Error exchanging OAuth token with MercadoPago", e);
        }
    }

    private OAuthTokenResponse toOAuthTokenResponse(MercadoPagoTokenResponse response) {
        return OAuthTokenResponse.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .tokenType(response.getTokenType())
                .expiresIn(response.getExpiresIn())
                .scope(response.getScope())
                .userId(response.getUserId())
                .publicKey(response.getPublicKey())
                .liveMode(response.getLiveMode())
                .build();
    }

}
