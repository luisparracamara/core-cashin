package com.core.cashin.routing.service;

import com.core.cashin.commons.entity.PaymentFeeEntity;
import com.core.cashin.commons.model.DepositRequest;

public interface PaymentFeeService {

    PaymentFeeEntity calculatePaymentFee(DepositRequest request);

}
