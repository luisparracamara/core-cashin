package com.core.cashin.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInfoResponse {

    private String type;
    private String result;
    private String paymentMethod;
    private String paymentMethodName;
    private String amount;
    private String currency;
    private String createdAt;

}
