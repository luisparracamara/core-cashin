package com.core.cashin.commons.service;

import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;

public interface PaymentOperationService {

    void savePaymentCashin(PaymentEntity paymentEntity, PaymentCashinEntity paymentCashinEntity, PayerEntity payerEntity);

    void savePaymentCashin(PaymentEntity paymentEntity, PayerEntity payerEntity);

    void savePaymentCashinPending(PaymentEntity paymentEntity, PaymentCashinEntity paymentCashinEntity);

}
