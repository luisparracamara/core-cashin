package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.service.PaymentRedirector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentRedirectorResolver {

    private final Map<ConnectorEnum, PaymentRedirector> redirectors;

    //esto sirve para inicializar todas las clases que implementan paymentdirector, la lista spring la genera sola
    public PaymentRedirectorResolver(List<PaymentRedirector> beans) {
        log.debug("[PaymentRedirectorResolver] List of PaymentRedirector {}", beans);
        this.redirectors = beans.stream()
                .collect(Collectors.toMap(
                        PaymentRedirector::getConnector,
                        Function.identity()
                ));

        log.debug("[PaymentRedirectorResolver] Map of PaymentRedirector {}", redirectors);
    }

    public PaymentRedirector resolve(ConnectorEnum connector) {
        PaymentRedirector redirector = redirectors.get(connector);

        if (redirector == null) {
            throw new NotFoundException("No redirector found for connector: " + connector);
        }

        return redirector;
    }

}
