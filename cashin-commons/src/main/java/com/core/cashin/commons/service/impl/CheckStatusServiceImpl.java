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
        log.debug("[CheckStatus] checking payment paymentId={}", id);
        MerchantFeePayment feePayment = paymentRepository.findByPaymentId(id)
                .orElseThrow(() -> {
                    log.warn("[CheckStatus] payment not found paymentId={}", id);
                    return new NotFoundException("Payment was not found with id: " + id);
                });
        log.debug("[CheckStatus] payment found paymentId={} status={}", id, feePayment.getPayment().getStatus());
        return checkStatusMapper.buildStatusResponse(feePayment);
    }

}
