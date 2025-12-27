package com.core.cashin.routing.mapper;

import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.MerchantRequest;
import com.core.cashin.commons.model.RoutingResultProjection;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RoutingMapper {

    public DepositRequest depositRequestMapper(DepositRequest request, RoutingResultProjection
                                                       routingResultDTO, Map<String, String> metadata, Map<String, String> headers) {
        MerchantRequest merchantRequest  = MerchantRequest.builder()
                .merchantId(routingResultDTO.getMerchantId())
                .merchantName(routingResultDTO.getMerchantName())
                .build();

        request.setMerchant(merchantRequest);
        request.setConnectorName(routingResultDTO.getConnectorName());
        request.setGatewayMetadata(metadata);
        request.setDate(headers.get("x-date"));
        request.setPaymentMethodName(routingResultDTO.getPaymentMethodName());
        request.setGatewayId(routingResultDTO.getGatewayId());

        return request;
    }
}
