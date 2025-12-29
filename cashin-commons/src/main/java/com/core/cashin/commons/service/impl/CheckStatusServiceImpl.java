package com.core.cashin.commons.service.impl;

import com.core.cashin.commons.exception.NotFoundException;
import com.core.cashin.commons.mapper.CheckStatusMapper;
import com.core.cashin.commons.model.CheckStatusResponse;
import com.core.cashin.commons.model.MerchantFeePayment;
import com.core.cashin.commons.repository.PaymentRepository;
import com.core.cashin.commons.service.CheckStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CheckStatusServiceImpl implements CheckStatusService {

    private final CheckStatusMapper checkStatusMapper;

    private final PaymentRepository paymentRepository;

    public CheckStatusServiceImpl(CheckStatusMapper checkStatusMapper, PaymentRepository paymentRepository) {
        this.checkStatusMapper = checkStatusMapper;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public CheckStatusResponse checkStatusDeposit(long id) {
        MerchantFeePayment feePayment = paymentRepository.findByPaymentId(id)
                        .orElseThrow(() -> new NotFoundException("Payment was not found with id: "+id));
        return checkStatusMapper.buildStatusResponse(feePayment);

    }

}
