package com.core.cashin.routing.cron;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.constants.GatewayMetadataEnum;
import com.core.cashin.commons.entity.Gateway;
import com.core.cashin.commons.repository.MetadataRepository;
import com.core.cashin.commons.service.MetadataService;
import com.core.cashin.routing.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class OAuthTokenRefreshCron {

    private static final long DAYS_BEFORE_EXPIRY = 7;
    private static final Set<String> OAUTH_CAPABLE_CONNECTORS = ConnectorEnum.getOAuthCapableNames();

    private final OAuthService oAuthService;
    private final MetadataService metadataService;
    private final MetadataRepository metadataRepository;

    public OAuthTokenRefreshCron(OAuthService oAuthService,
                                 MetadataService metadataService, MetadataRepository metadataRepository) {
        this.oAuthService = oAuthService;
        this.metadataService = metadataService;
        this.metadataRepository = metadataRepository;
    }

    @Scheduled(cron = "${jobs.refresh-token-oauth.cron}", zone = "${jobs.refresh-token-oauth.zone}")
    @SchedulerLock(name = "oauth-token-refresh", lockAtMostFor = "10m", lockAtLeastFor = "5m")
    public void refreshExpiringTokens() {
        log.info("[CRON-OAUTH-REFRESH] Starting token refresh check. OAuth-capable connectors: {}", OAUTH_CAPABLE_CONNECTORS);

        int refreshed = 0;
        int errors = 0;

        for (Gateway gateway : metadataRepository.findAllGateways()) {
            if (!OAUTH_CAPABLE_CONNECTORS.contains(gateway.getConnectorName())) {
                continue;
            }
            try {
                ConnectorEnum connector = ConnectorEnum.fromDisplayName(gateway.getConnectorName());
                boolean wasRefreshed = refreshIfNeeded(connector, gateway.getMerchantId());
                if (wasRefreshed) refreshed++;
            } catch (Exception e) {
                errors++;
                log.error("[CRON-OAUTH-REFRESH] Error refreshing token for connector={} merchantId={}: {}",
                        gateway.getConnectorName(), gateway.getMerchantId(), e.getMessage(), e);
            }
        }

        log.info("[CRON-OAUTH-REFRESH] Finished — refreshed={} errors={}", refreshed, errors);
    }

    private boolean refreshIfNeeded(ConnectorEnum connector, Long merchantId) {
        Map<String, String> metadata = metadataService.retrieveGatewayMetadata(connector.getName(), merchantId);

        String expiresAt = metadata.get(GatewayMetadataEnum.TOKEN_EXPIRES_AT.name());
        String refreshToken = metadata.get(GatewayMetadataEnum.REFRESH_TOKEN.name());

        if (expiresAt == null || refreshToken == null) {
            return false;
        }

        Instant expiry = Instant.parse(expiresAt);
        Instant threshold = Instant.now().plus(DAYS_BEFORE_EXPIRY, ChronoUnit.DAYS);

        if (expiry.isBefore(threshold)) {
            log.info("[CRON-OAUTH-REFRESH] Token for connector={} merchantId={} expires at {}, refreshing...",
                    connector.getName(), merchantId, expiresAt);
            oAuthService.refresh(connector.getName(), merchantId);
            log.info("[CRON-OAUTH-REFRESH] Token refreshed for connector={} merchantId={}", connector.getName(), merchantId);
            return true;
        }

        return false;
    }

}
