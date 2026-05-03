package com.core.cashin.routing.service.impl;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.constants.HttpHeaders;
import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.entity.PaymentFeeEntity;
import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.mapper.CashinMapper;
import com.core.cashin.commons.model.CheckStatusResponse;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.model.RoutingResultProjection;
import com.core.cashin.commons.service.CheckStatusService;
import com.core.cashin.commons.service.PaymentOperationService;
import com.core.cashin.commons.utils.Utils;
import com.core.cashin.routing.mapper.RoutingMapper;
import com.core.cashin.routing.repository.RoutingRepository;
import com.core.cashin.routing.service.PaymentFeeService;
import com.core.cashin.routing.service.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final CheckStatusService checkStatusService;
    private final PaymentFeeService paymentFeeService;
    private final Utils utils;

    public RoutingServiceImpl(RoutingMapper routingMapper, RoutingRepository routingRepository, PaymentRedirectorResolver resolver,
                              PaymentOperationService paymentOperationService, CashinMapper cashinMapper,
                              CheckStatusService checkStatusService, PaymentFeeService paymentFeeService, Utils utils) {
        this.routingMapper = routingMapper;
        this.routingRepository = routingRepository;
        this.resolver = resolver;
        this.paymentOperationService = paymentOperationService;
        this.cashinMapper = cashinMapper;
        this.checkStatusService = checkStatusService;
        this.paymentFeeService = paymentFeeService;
        this.utils = utils;
    }

    @Override
    public DepositResponse createDeposit(DepositRequest request, HttpServletRequest httpServletRequest, Map<String, String> headers) {
        DepositRequest depositRequest = retrieveCashinRoutingRule(request, headers);
        ConnectorEnum connector = ConnectorEnum.fromDisplayName(depositRequest.getConnectorName());
        log.debug("[RoutingService] connector resolved connector={} gatewayId={} merchantId={}",
                connector, depositRequest.getGatewayId(), depositRequest.getMerchant().getMerchantId());
        return routeDeposit(connector, depositRequest, httpServletRequest);
    }

    @Override
    public boolean checkExternalStatusDeposit(ConnectorEnum connector, String id, Long merchantId) {
        log.debug("[RoutingService] checkExternalStatus paymentId={} connector={} merchantId={}", id, connector, merchantId);
        return routeCheckStatus(connector, id, merchantId);
    }

    @Override
    public CheckStatusResponse checkStatusDeposit(long id) {
        log.debug("[RoutingService] checkStatus paymentId={}", id);
        return checkStatusService.checkStatusDeposit(id);
    }

    private DepositRequest retrieveCashinRoutingRule(DepositRequest request, Map<String, String> headers) {
        List<RoutingResultProjection> routingResultDTO = routingRepository.resolveRouting(request.getCountry(),
                request.getPaymentMethod(), headers.get(HttpHeaders.LOGIN_ID), headers.get(HttpHeaders.SECRET_KEY), request.getCurrency());

        if (routingResultDTO.isEmpty()) {
            log.warn("[RoutingService] no routing rule found country={} paymentMethod={} currency={} loginId={}",
                    request.getCountry(), request.getPaymentMethod(), request.getCurrency(), headers.get(HttpHeaders.LOGIN_ID));
            throw new NotFoundException("Cashing routing rule was not found");
        }

        Map<String, String> metadata = routingResultDTO.stream()
                .filter(r -> r.getMetadataKey() != null)
                .collect(Collectors.toMap(
                        RoutingResultProjection::getMetadataKey,
                        RoutingResultProjection::getMetadataValue
                ));

        DepositRequest depositRequest = routingMapper.depositRequestMapper(request, routingResultDTO.get(0), metadata, headers);
        log.debug("[RoutingService] depositRequest={}", utils.toJsonSafe(depositRequest));

        return depositRequest;
    }

    private DepositResponse routeDeposit(ConnectorEnum connector, DepositRequest request, HttpServletRequest httpServletRequest) {
        PayerEntity payerEntity = cashinMapper.buildPayerEntity(request);
        PaymentFeeEntity paymentFeeEntity = paymentFeeService.calculatePaymentFee(request);
        PaymentEntity paymentEntity = cashinMapper.buildPaymentEntity(request, payerEntity, paymentFeeEntity,
                UUID.randomUUID().toString(), httpServletRequest);
        paymentOperationService.savePaymentCashin(paymentEntity, payerEntity);
        return resolver
                .resolve(connector)
                .create(request, paymentEntity);
    }

    private boolean routeCheckStatus(ConnectorEnum connector, String id, Long merchantId) {
        return resolver
                .resolve(connector)
                .checkStatus(id, merchantId);
    }

}
