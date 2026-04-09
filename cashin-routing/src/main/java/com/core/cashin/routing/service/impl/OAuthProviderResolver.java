package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.service.OAuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OAuthProviderResolver {

    private final Map<ConnectorEnum, OAuthProvider> providers;

    public OAuthProviderResolver(List<OAuthProvider> beans) {
        log.debug("[OAuthProviderResolver] List of OAuthProvider {}", beans);
        this.providers = beans.stream()
                .collect(Collectors.toMap(
                        OAuthProvider::getConnector,
                        Function.identity()
                ));
        log.debug("[OAuthProviderResolver] Map of OAuthProvider {}", providers);
    }

    public OAuthProvider resolve(ConnectorEnum connector) {
        OAuthProvider provider = providers.get(connector);

        if (provider == null) {
            throw new NotFoundException("No OAuth provider found for connector: " + connector);
        }

        return provider;
    }

    public Collection<ConnectorEnum> getRegisteredConnectors() {
        return providers.keySet();
    }

}
