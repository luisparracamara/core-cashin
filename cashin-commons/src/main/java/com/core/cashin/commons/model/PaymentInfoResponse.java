package com.core.cashin.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInfoResponse {

    private String type;
    private String paymentMethod;
    private String paymentMethodName;
    private BigDecimal amount;
    private String currency;
    private String createdAt;
    private DepositMetadataResponse metadata;

}
