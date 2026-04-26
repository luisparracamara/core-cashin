package com.core.cashin.payment.methods.methods.mercadopagocheckoutapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MercadoPagoOrderResponse(
        @JsonProperty("id") String id,
        @JsonProperty("status") String status,
        @JsonProperty("status_detail") String statusDetail,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("total_amount") String totalAmount,
        @JsonProperty("created_date") String createdDate,
        @JsonProperty("transactions") OrderTransactions transactions
) {

    public record OrderTransactions(
            @JsonProperty("payments") List<OrderPayment> payments
    ) {}

    public record OrderPayment(
            @JsonProperty("id") String id,
            @JsonProperty("status") String status,
            @JsonProperty("status_detail") String statusDetail
    ) {}

}
