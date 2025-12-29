package com.core.cashin.commons.mapper;

import com.core.cashin.commons.model.CheckStatusResponse;
import com.core.cashin.commons.model.MerchantFeePayment;
import org.springframework.stereotype.Component;

@Component
public class CheckStatusMapper {

    public CheckStatusResponse buildStatusResponse(MerchantFeePayment merchantFeePayment) {
        return CheckStatusResponse.builder()
                .depositId(merchantFeePayment.getPayment().getId())
                .country(merchantFeePayment.getPayment().getCountry())
                .currency(merchantFeePayment.getPayment().getCurrency())
                .amount(merchantFeePayment.getPayment().getAmount())
                .paymentMethod(merchantFeePayment.getPayment().getPaymentMethod())
                .paymentType(merchantFeePayment.getPayment().getPaymentType())
                .status(merchantFeePayment.getPayment().getStatus())
                .payer(merchantFeePayment.getPayerEntity())
                .feeAmount(merchantFeePayment.getMerchantFee() != null ? merchantFeePayment.getMerchantFee().getFeeAmount() : null)
                .feeCurrency(merchantFeePayment.getMerchantFee() != null ? merchantFeePayment.getMerchantFee().getCurrency() : null)
                .build();
    }
}
