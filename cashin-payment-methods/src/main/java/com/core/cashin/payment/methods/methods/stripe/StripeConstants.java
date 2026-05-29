package com.core.cashin.payment.methods.methods.stripe;

public class StripeConstants {

    private StripeConstants() {}

    public static final String AUTH_URL = "https://connect.stripe.com/oauth/authorize";
    public static final String SCOPE = "read_write";
    public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
    public static final String CHECKOUT_TYPE = "STRIPE_CARD";

    // Payment methods
    public static final String PAYMENT_METHOD_TYPE_CARD = "card";
}
