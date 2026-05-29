package com.core.cashin.payment.methods.methods.stripe;

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
public class StripeOAuthProvider implements OAuthProvider {

    @Value("${stripe.platform-secret-key}")
    private String platformSecretKey;

    @Value("${stripe.client-id}")
    private String clientId;

    @Value("${stripe.redirect-uri}")
    private String redirectUri;

    private final StripeOAuthClient oAuthClient;

    public StripeOAuthProvider(StripeOAuthClient oAuthClient) {
        this.oAuthClient = oAuthClient;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.STRIPE;
    }

    @Override
    public String buildAuthUrl(OAuthRequest request) {
        return StripeConstants.AUTH_URL
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&scope=" + StripeConstants.SCOPE
                + "&state=" + URLEncoder.encode(request.getState(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }

    @Override
    public OAuthTokenResponse exchangeCode(String code, String state, Long merchantId) {
        Map<String, String> body = Map.of(
                "grant_type", StripeConstants.GRANT_TYPE_AUTH_CODE,
                "client_secret", platformSecretKey,
                "code", code
        );

        try {
            StripeTokenResponse response = oAuthClient.exchangeToken(body);
            log.debug("[StripeOAuth] Token exchanged merchantId={} stripeUserId={}", merchantId, response.getStripeUserId());
            return toOAuthTokenResponse(response);
        } catch (FeignException.BadRequest | FeignException.UnprocessableEntity e) {
            log.error("[StripeOAuth] Invalid request status={} body={}", e.status(), e.contentUTF8());
            throw new BadRequestException("Invalid OAuth request to Stripe: " + e.contentUTF8());
        } catch (FeignException e) {
            log.error("[StripeOAuth] Error calling token endpoint status={} body={}", e.status(), e.contentUTF8());
            throw new InternalServerException("Error exchanging OAuth token with Stripe", e);
        }
    }

    @Override
    public OAuthTokenResponse refreshToken(String refreshToken, Long merchantId) {
        // Stripe tokens never expire
        log.warn("[StripeOAuth] Stripe tokens do not expire, refresh not needed merchantId={}", merchantId);
        throw new BadRequestException("Stripe access tokens do not expire and cannot be refreshed");
    }

    private OAuthTokenResponse toOAuthTokenResponse(StripeTokenResponse response) {
        return OAuthTokenResponse.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .tokenType(response.getTokenType())
                .scope(response.getScope())
                .accountId(response.getStripeUserId())
                .publicKey(response.getStripePublishableKey())
                .liveMode(response.getLiveMode())
                .build();
    }
}
