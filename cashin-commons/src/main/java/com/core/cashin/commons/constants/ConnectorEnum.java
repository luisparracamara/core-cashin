package com.core.cashin.commons.constants;

import com.core.cashin.commons.exception.NotFoundException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum ConnectorEnum {

    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    MERCADOPAGO("Mercado Pago"),
    CONNECTOR("connector"),
    MERCADO_PAGO_CHECKOUT_PRO("MercadoPagoCheckoutPro"),
    MERCADO_PAGO_CHECKOUT_API("MercadoPagoCheckoutApi");

    private final String name;

    public static final Set<ConnectorEnum> OAUTH_CAPABLE = Set.of(
            MERCADO_PAGO_CHECKOUT_PRO,
            MERCADO_PAGO_CHECKOUT_API,
            STRIPE
    );

    ConnectorEnum(String name) {
        this.name = name;
    }

    public static ConnectorEnum fromDisplayName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unknown GatewayConnector: " + name));
    }

    public static Set<String> getOAuthCapableNames() {
        return OAUTH_CAPABLE.stream()
                .map(ConnectorEnum::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

}
