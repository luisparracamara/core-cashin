package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

public final class MercadoPagoCheckoutApiConstants {

    private MercadoPagoCheckoutApiConstants() {}

    // Order
    public static final String ORDER_TYPE = "online";
    public static final String PROCESSING_MODE = "automatic";
    public static final String CHECKOUT_TYPE = "DIRECT";

    // Payment types
    public static final String PAYMENT_TYPE_CREDIT = "credit_card";
    public static final String PAYMENT_TYPE_DEBIT = "debit_card";
    public static final String PAYMENT_TYPE_ACCOUNT_MONEY = "account_money";
    public static final String PAYMENT_TYPE_BANK_TRANSFER = "bank_transfer";
    public static final String PAYMENT_TYPE_TICKET = "ticket";
    // Payment method IDs
    public static final String PAYMENT_METHOD_CLABE = "clabe";
    public static final String PAYMENT_METHOD_OXXO = "oxxo";

    // Defaults
    public static final int DEFAULT_INSTALLMENTS = 1;

    // paymentData keys
    public static final String CARD_TOKEN = "cardToken";
    public static final String CARD_TYPE = "cardType";
    public static final String CARD_BRAND = "id";

    // Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_PLATFORM_ID = "X-Platform-Id";
    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

}
