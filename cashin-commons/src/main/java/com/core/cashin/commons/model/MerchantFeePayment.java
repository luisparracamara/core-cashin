package com.core.cashin.commons.model;

import com.core.cashin.commons.entity.PaymentFeeEntity;
import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantFeePayment {
    private PaymentFeeEntity merchantFee;
    private PaymentEntity payment;
    private PayerEntity payerEntity;
}
