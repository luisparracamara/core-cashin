package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.model.RoutingResultProjection;
import com.core.cashin.routing.mapper.RoutingMapper;
import com.core.cashin.routing.repository.RoutingRepository;
import com.core.cashin.routing.service.RoutingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final RoutingMapper routingMapper;

    private final RoutingRepository routingRepository;

    private final PaymentRedirectorResolver resolver;

    public RoutingServiceImpl(RoutingMapper routingMapper, RoutingRepository routingRepository, PaymentRedirectorResolver resolver) {
        this.routingMapper = routingMapper;
        this.routingRepository = routingRepository;
        this.resolver = resolver;
    }

    @Override
    public DepositResponse createDeposit(DepositRequest request, Map<String, String> headers) {
        DepositRequest depositRequest = retrieveCashinRoutingRule(request, headers);
        ConnectorEnum connector = ConnectorEnum.fromDisplayName(depositRequest.getConnectorName());
        log.debug("[RoutingServiceImpl] ConnectorEnum result {}", connector);
        return routeDeposit(connector, request);
    }

    private DepositRequest retrieveCashinRoutingRule(DepositRequest request, Map<String, String> headers) {
        List<RoutingResultProjection> routingResultDTO = routingRepository.resolveRouting(request.getCountry(),
                request.getPaymentMethod(), headers.get("login-id"), headers.get("secret-key"), request.getCurrency());

        if (routingResultDTO.isEmpty()) {
            throw new NotFoundException("Cashing routing rule was not found");
        }

        Map<String, String> metadata = routingResultDTO.stream()
                .filter(r -> r.getMetadataKey() != null)
                .collect(Collectors.toMap(
                        RoutingResultProjection::getMetadataKey,
                        RoutingResultProjection::getMetadataValue
                ));

        log.debug("[RoutingServiceImpl] metadata {}", metadata);

        DepositRequest depositRequest = routingMapper.depositRequestMapper(request, routingResultDTO.get(0), metadata);
        log.debug("[RoutingServiceImpl] depositRequest {}", depositRequest);

        return depositRequest;
    }

    //PaymentRedirector es capaz de usar la interfaz por polimorfismo
    public DepositResponse routeDeposit(ConnectorEnum connector, DepositRequest request) {
         return resolver
                 .resolve(connector) // devuelve a donde se redirige
                 .create(request); // redirige
    }

}
