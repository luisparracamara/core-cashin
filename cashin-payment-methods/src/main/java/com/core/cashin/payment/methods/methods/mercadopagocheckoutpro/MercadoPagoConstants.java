package com.core.cashin.payment.methods.methods.mercadopagocheckoutpro;

public final class MercadoPagoConstants {

    private MercadoPagoConstants() {}

    // Preference
    public static final String CATEGORY_ID = "services";
    public static final String AUTO_RETURN = "approved";
    public static final String CHECKOUT_TYPE = "ONE_SHOT";
    public static final String DESCRIPTION_PREFIX = "Reference: ";
    public static final String EXTERNAL_REFERENCE_KEY = "external_reference";

    // Payment method restrictions
    public static final String EXCLUDED_PAYMENT_TYPE_TICKET = "ticket";
    public static final String EXCLUDED_PAYMENT_TYPE_ATM = "atm";
    public static final int MAX_INSTALLMENTS = 1;

    // OAuth
    public static final String PLATFORM_ID = "mp";
    public static final String AUTH_URL = "https://auth.mercadopago.com/authorization";
    public static final String GRANT_TYPE = "grant_type";
    public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
    public static final String GRANT_TYPE_REFRESH = "refresh_token";
    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_CLIENT_SECRET = "client_secret";
    public static final String PARAM_CODE = "code";
    public static final String PARAM_REDIRECT_URI = "redirect_uri";
    public static final String PARAM_REFRESH_TOKEN = "refresh_token";

    // Headers
    public static final String HEADER_PLATFORM_ID = "X-Platform-Id";

}
