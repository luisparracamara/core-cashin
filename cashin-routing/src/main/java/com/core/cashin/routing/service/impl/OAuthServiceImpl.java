package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.constants.GatewayMetadataEnum;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.model.OAuthRequest;
import com.core.cashin.commons.model.OAuthTokenResponse;
import com.core.cashin.commons.service.MetadataService;
import com.core.cashin.commons.service.OAuthProvider;
import com.core.cashin.routing.cache.OAuthStateStore;
import com.core.cashin.routing.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class OAuthServiceImpl implements OAuthService {

    private final OAuthProviderResolver resolver;
    private final MetadataService metadataService;
    private final OAuthStateStore stateStore;

    public OAuthServiceImpl(OAuthProviderResolver resolver, MetadataService metadataService, OAuthStateStore stateStore) {
        this.resolver = resolver;
        this.metadataService = metadataService;
        this.stateStore = stateStore;
    }

    @Override
    public String getAuthUrl(String connector, Long merchantId) {
        ConnectorEnum connectorEnum = ConnectorEnum.fromDisplayName(connector);

        if (!ConnectorEnum.OAUTH_CAPABLE.contains(connectorEnum)) {
            log.warn("[OAuthService] unsupported connector for OAuth connector={}", connector);
            throw new BadRequestException("Connector does not support OAuth: " + connector);
        }

        OAuthProvider provider = resolver.resolve(connectorEnum);

        String encodedState = merchantId + ":" + connector + ":" + UUID.randomUUID();

        OAuthRequest request = OAuthRequest.builder()
                .state(encodedState)
                .connectorName(connector)
                .merchantId(merchantId)
                .build();

        String authUrl = provider.buildAuthUrl(request);
        stateStore.save(encodedState);
        log.debug("[OAuthService] Auth URL generated for connector {} merchantId {}: {}", connector, merchantId, authUrl);
        return authUrl;
    }

    @Override
    public OAuthTokenResponse handleCallback(String code, String state) {
        if (!stateStore.consumeIfValid(state)) {
            log.warn("[OAuthService] invalid or expired OAuth state state={}", state);
            throw new BadRequestException("Invalid or expired OAuth state");
        }
        Long merchantId = extractMerchantId(state);
        String connector = extractConnector(state);
        ConnectorEnum connectorEnum = ConnectorEnum.fromDisplayName(connector);
        OAuthProvider provider = resolver.resolve(connectorEnum);

        OAuthTokenResponse tokenResponse = provider.exchangeCode(code, state, merchantId);
        log.debug("[OAuthService] Tokens received for connector {} merchantId {}", connector, merchantId);

        persistTokens(connectorEnum, merchantId, tokenResponse);

        return tokenResponse;
    }

    private Long extractMerchantId(String state) {
        try {
            return Long.parseLong(state.split(":")[0]);
        } catch (Exception e) {
            log.error("[OAuthService] failed to extract merchantId from state cause={}", e.getMessage());
            throw new IllegalArgumentException("Invalid state parameter: " + state);
        }
    }

    private String extractConnector(String state) {
        try {
            return state.split(":")[1];
        } catch (Exception e) {
            log.error("[OAuthService] failed to extract connector from state cause={}", e.getMessage());
            throw new IllegalArgumentException("Invalid state parameter, connector missing: " + state);
        }
    }

    @Override
    public OAuthTokenResponse refresh(String connector, Long merchantId) {
        ConnectorEnum connectorEnum = ConnectorEnum.fromDisplayName(connector);

        if (!ConnectorEnum.OAUTH_CAPABLE.contains(connectorEnum)) {
            throw new BadRequestException("Connector does not support OAuth: " + connector);
        }

        OAuthProvider provider = resolver.resolve(connectorEnum);

        Map<String, String> metadata = metadataService.retrieveGatewayMetadata(connectorEnum.getName(), merchantId);
        String refreshToken = metadata.get(GatewayMetadataEnum.REFRESH_TOKEN.name());

        if (refreshToken == null) {
            log.warn("[OAuthService] no refresh token found connector={} merchantId={}", connector, merchantId);
            throw new BadRequestException("No refresh token found for connector: " + connector);
        }

        OAuthTokenResponse tokenResponse = provider.refreshToken(refreshToken, merchantId);
        log.debug("[OAuthService] Tokens refreshed for connector {} merchantId {}", connector, merchantId);

        persistTokens(connectorEnum, merchantId, tokenResponse);

        return tokenResponse;
    }

    @Override
    public boolean isConnected(String connector, Long merchantId) {
        ConnectorEnum connectorEnum = ConnectorEnum.fromDisplayName(connector);
        Map<String, String> metadata = metadataService.retrieveGatewayMetadata(connectorEnum.getName(), merchantId);

        if (!metadata.containsKey(GatewayMetadataEnum.ACCESS_TOKEN.name())) {
            log.debug("[OAuthService] isConnected=false no access token connector={} merchantId={}", connector, merchantId);
            return false;
        }

        String expiresAt = metadata.get(GatewayMetadataEnum.TOKEN_EXPIRES_AT.name());
        if (expiresAt != null) {
            boolean connected = Instant.parse(expiresAt).isAfter(Instant.now());
            log.debug("[OAuthService] isConnected={} connector={} merchantId={} expiresAt={}", connected, connector, merchantId, expiresAt);
            return connected;
        }

        log.debug("[OAuthService] isConnected=true connector={} merchantId={}", connector, merchantId);
        return true;
    }

    @Override
    public void disconnect(String connector, Long merchantId) {
        ConnectorEnum connectorEnum = ConnectorEnum.fromDisplayName(connector);
        String connectorName = connectorEnum.getName();

        metadataService.deleteMetadata(connectorName, merchantId, GatewayMetadataEnum.ACCESS_TOKEN.name());
        metadataService.deleteMetadata(connectorName, merchantId, GatewayMetadataEnum.REFRESH_TOKEN.name());
        metadataService.deleteMetadata(connectorName, merchantId, GatewayMetadataEnum.TOKEN_EXPIRES_AT.name());
        metadataService.deleteMetadata(connectorName, merchantId, GatewayMetadataEnum.USER_ID.name());
        metadataService.deleteMetadata(connectorName, merchantId, GatewayMetadataEnum.PUBLIC_KEY.name());

        log.debug("[OAuthService] Disconnected connector {} merchantId {}", connectorName, merchantId);
    }

    private void persistTokens(ConnectorEnum connector, Long merchantId, OAuthTokenResponse tokenResponse) {
        String connectorName = connector.getName();

        metadataService.saveOrUpdateMetadata(connectorName, merchantId,
                GatewayMetadataEnum.ACCESS_TOKEN.name(), tokenResponse.getAccessToken());

        if (tokenResponse.getRefreshToken() != null) {
            metadataService.saveOrUpdateMetadata(connectorName, merchantId,
                    GatewayMetadataEnum.REFRESH_TOKEN.name(), tokenResponse.getRefreshToken());
        }

        if (tokenResponse.getExpiresIn() != null) {
            String expiresAt = Instant.now()
                    .plusSeconds(tokenResponse.getExpiresIn())
                    .toString();
            metadataService.saveOrUpdateMetadata(connectorName, merchantId,
                    GatewayMetadataEnum.TOKEN_EXPIRES_AT.name(), expiresAt);
        }

        if (tokenResponse.getUserId() != null) {
            metadataService.saveOrUpdateMetadata(connectorName, merchantId,
                    GatewayMetadataEnum.USER_ID.name(), tokenResponse.getUserId().toString());
        }

        if (tokenResponse.getPublicKey() != null) {
            metadataService.saveOrUpdateMetadata(connectorName, merchantId,
                    GatewayMetadataEnum.PUBLIC_KEY.name(), tokenResponse.getPublicKey());
        }

        log.debug("[OAuthService] Tokens persisted for connector {} merchantId {}", connectorName, merchantId);
    }

}
