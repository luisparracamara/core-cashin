package com.core.cashin.commons.model;

import com.core.cashin.commons.entity.PaymentCashinEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentCashinWithGateway {
    private PaymentCashinEntity paymentCashin;
    private String connectorName;
}