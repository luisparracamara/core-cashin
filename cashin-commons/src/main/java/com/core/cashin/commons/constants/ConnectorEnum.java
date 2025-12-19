package com.core.cashin.commons.constants;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ConnectorEnum {

    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    MERCADOPAGO("Mercado Pago"),
    CONNECTOR("conenctor");

    private final String name;

    ConnectorEnum(String name) {
        this.name = name;
    }

    public static ConnectorEnum fromDisplayName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown GatewayConnector displayName: " + name));
    }

}
