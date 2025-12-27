package com.core.cashin.payment.methods.methods;

import com.core.cashin.commons.constants.CashInMethod;
import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.constants.GatewayMetadataEnum;
import com.core.cashin.commons.entity.GatewayMetadataEntity;
import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.exception.BadRequestException;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import com.core.cashin.commons.repository.MetadataRepository;
import com.core.cashin.commons.service.PaymentOperationService;
import com.core.cashin.commons.service.PaymentRedirector;
import com.core.cashin.commons.utils.Utils;
import com.core.cashin.payment.methods.mapper.MercadoPagoCheckoutProMapper;
import com.core.cashin.payment.methods.model.MercadoPagoCheckoutProPreferenceResponse;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResultsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MercadoPagoCheckoutPro implements PaymentRedirector {

    private final Utils utils;

    private final MercadoPagoCheckoutProMapper mercadoPagoCheckoutProMapper;

    private final PaymentOperationService paymentOperationService;

    private final MetadataRepository metadataRepository;

    public MercadoPagoCheckoutPro(Utils utils, MercadoPagoCheckoutProMapper mercadoPagoCheckoutProMapper, PaymentOperationService paymentOperationService, MetadataRepository metadataRepository) {
        this.utils = utils;
        this.mercadoPagoCheckoutProMapper = mercadoPagoCheckoutProMapper;
        this.paymentOperationService = paymentOperationService;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }

    @Override
    public CashInMethod getCashInMethod() {
        return CashInMethod.VOUCHER;
    }

    @Override
    public DepositResponse create(DepositRequest request, PaymentEntity paymentEntity) {
        DepositResponse response;
        MercadoPagoCheckoutProPreferenceResponse mercadoPagoResponse = null;

        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro {}", getConnector());
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro request {}", utils.toJson(request));

        MPRequestOptions mpRequestOptions = mercadoPagoCheckoutProMapper.buildMPRequestOptions(
                request.getGatewayMetadata().get(GatewayMetadataEnum.ACCESS_TOKEN.name()),
                request.getGatewayMetadata().get(GatewayMetadataEnum.PLATFORM_ID.name()));
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro mpRequestOptions {}", utils.toJson(mpRequestOptions));

        PreferenceRequest preferenceRequest = mercadoPagoCheckoutProMapper.buildPreferenceRequest(request, paymentEntity.getTransactionId());
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro preferenceRequest {}", utils.toJson(preferenceRequest));

        PreferenceClient client = new PreferenceClient();
        try {
            Preference preference = client.create(preferenceRequest, mpRequestOptions);

            log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro preference getCollectorId {}", preference.getCollectorId());
            log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro preference getId {}", preference.getId());
            log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro preference  getExternalReference{}", preference.getExternalReference());

            mercadoPagoResponse = mercadoPagoCheckoutProMapper
                    .buildMercadoPagoCheckoutProPreferenceResponse(preference);
            log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro mercadoPagoResponse {}",
                    utils.toJson(mercadoPagoResponse));

            response = mercadoPagoCheckoutProMapper.buildDepositResponse(request, mercadoPagoResponse, paymentEntity.getTransactionId(),
                    getCashInMethod().name());

        } catch (MPException | MPApiException e) {
            throw new BadRequestException("PROVIDER ERROR: " + getConnector().name(), e);
        }

        PaymentCashinEntity paymentCashinEntity = mercadoPagoCheckoutProMapper.buildPaymentCashinEntity(
                mercadoPagoResponse, paymentEntity, paymentEntity.getCreatedAt(), utils.toJson(mercadoPagoResponse));
        paymentOperationService.savePaymentCashinPending(paymentEntity, paymentCashinEntity);
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro operation saved in database idPayment: {}",
                paymentEntity.getTransactionId());

        return response;
    }

    @Override
    public boolean checkStatus(String id) {
        PaymentClient client = new PaymentClient();

        Map<String, String> gatewayMetadata = metadataRepository.findByGatewayConnectorName(getConnector().getName()).stream()
                .collect(Collectors.toMap(
                        GatewayMetadataEntity::getMetaKey,
                        GatewayMetadataEntity::getMetaValue
                ));

        MPRequestOptions mpRequestOptions = mercadoPagoCheckoutProMapper.buildMPRequestOptions(
                gatewayMetadata.get(GatewayMetadataEnum.ACCESS_TOKEN.name()));

        Map<String, Object> filters = new HashMap<>();
        filters.put("external_reference", id);
        MPSearchRequest searchRequest = MPSearchRequest.builder()
                .offset(0)
                .limit(10)
                .filters(filters)
                .build();

        try {
            MPResultsResourcesPage<Payment> status = client.search(searchRequest, mpRequestOptions);
            log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro checkStatus result {} payments found", status.getResults().size());
            return !status.getResults().isEmpty();
        } catch (MPException | MPApiException e) {
            throw new BadRequestException("PROVIDER ERROR CHECK STATUS WITH ID: "+id+ " " + getConnector().name(), e);
        }
    }
//TODO cambiar el nombre a entitities pero uno por uno y ver como afecta a consulta de ruteo
    //refactgor codigo
    //revisar IP del ruteo, nosotros detectamos la ip o tambien nos la envian en request
    //revisar pasar de pending a cancelled con scheduler en chatgpt
    //poner valores del scheduler en yml
    //revisar esto para checkstatus ya mejor estructurado https://docs.d24.com/api-reference/deposits-api/manage-payments/get-deposit-status
    //calccuar fee
    //webhook
    //sistema de wallet para fees de pagos, acumulables y que luego salden la deuda despues de confirmar el pago en webhook

}
