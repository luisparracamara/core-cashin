package com.core.cashin.commons.model;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.entity.PayerEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckStatusResponse {

    private Long depositId;
    private String country;
    private String currency;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentType;
    private PaymentStatusEnum status;
    private PayerEntity payer;
    private BigDecimal feeAmount;
    private String feeCurrency;

}
