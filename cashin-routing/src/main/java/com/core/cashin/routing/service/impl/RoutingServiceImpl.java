package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.mapper.CashinMapper;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.model.RoutingResultProjection;
import com.core.cashin.commons.service.PaymentOperationService;
import com.core.cashin.routing.mapper.RoutingMapper;
import com.core.cashin.routing.repository.RoutingRepository;
import com.core.cashin.routing.service.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final RoutingMapper routingMapper;

    private final RoutingRepository routingRepository;

    private final PaymentRedirectorResolver resolver;

    private final PaymentOperationService paymentOperationService;

    private final CashinMapper cashinMapper;

    public RoutingServiceImpl(RoutingMapper routingMapper, RoutingRepository routingRepository, PaymentRedirectorResolver resolver, PaymentOperationService paymentOperationService, CashinMapper cashinMapper) {
        this.routingMapper = routingMapper;
        this.routingRepository = routingRepository;
        this.resolver = resolver;
        this.paymentOperationService = paymentOperationService;
        this.cashinMapper = cashinMapper;
    }

    @Override
    public DepositResponse createDeposit(DepositRequest request, HttpServletRequest httpServletRequest, Map<String, String> headers) {
        DepositRequest depositRequest = retrieveCashinRoutingRule(request, headers);
        ConnectorEnum connector = ConnectorEnum.fromDisplayName(depositRequest.getConnectorName());
        log.debug("[RoutingServiceImpl] ConnectorEnum result {}", connector);
        return routeDeposit(connector, request, httpServletRequest);
    }

    @Override
    public boolean checkStatusDeposit(ConnectorEnum connector, String id) {
        return routeCheckStatus(connector, id);
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

        DepositRequest depositRequest = routingMapper.depositRequestMapper(request, routingResultDTO.get(0), metadata, headers);
        log.debug("[RoutingServiceImpl] depositRequest {}", depositRequest);

        return depositRequest;
    }

    //PaymentRedirector es capaz de usar la interfaz por polimorfismo
    private DepositResponse routeDeposit(ConnectorEnum connector, DepositRequest request, HttpServletRequest httpServletRequest) {
        PayerEntity payerEntity = cashinMapper.buildPayerEntity(request);
        PaymentEntity paymentEntity = cashinMapper.buildPaymentEntity(request, payerEntity, UUID.randomUUID().toString(),
                LocalDateTime.now(), httpServletRequest);
        paymentOperationService.savePaymentCashin(paymentEntity, payerEntity);
        return resolver
                .resolve(connector) // devuelve a donde se redirige
                .create(request, paymentEntity); // redirige
    }

    private boolean routeCheckStatus(ConnectorEnum connector, String id) {
        return resolver
                .resolve(connector)
                .checkStatus(id);
    }

}
