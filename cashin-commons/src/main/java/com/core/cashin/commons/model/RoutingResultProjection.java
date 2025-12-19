package com.core.cashin.commons.model;

public interface RoutingResultProjection {

    Long getPaymentMethodId();
    String getPaymentCode();

    Long getMerchantId();
    String getMerchantName();

    Long getGatewayId();
    String getConnectorName();

    String getMetadataKey();
    String getMetadataValue();
}
