package com.core.cashin.commons.model;

import com.core.cashin.commons.entity.MerchantFeeEntity;
import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing both MerchantFeeEntity and PaymentEntity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantFeePayment {
    private MerchantFeeEntity merchantFee;
    private PaymentEntity payment;
    private PayerEntity payerEntity;
}
