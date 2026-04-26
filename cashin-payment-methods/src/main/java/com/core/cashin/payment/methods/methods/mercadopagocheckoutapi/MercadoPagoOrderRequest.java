package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MercadoPagoOrderRequest(
        @JsonProperty("type") String type,
        @JsonProperty("processing_mode") String processingMode,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("total_amount") String totalAmount,
        @JsonProperty("description") String description,
        @JsonProperty("payer") OrderPayer payer,
        @JsonProperty("transactions") OrderTransactions transactions
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderPayer(
            @JsonProperty("email") String email
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderTransactions(
            @JsonProperty("payments") List<OrderPayment> payments
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderPayment(
            @JsonProperty("amount") String amount,
            @JsonProperty("payment_method") OrderPaymentMethod paymentMethod
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderPaymentMethod(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("token") String token,
            @JsonProperty("installments") Integer installments
    ) {}

}
