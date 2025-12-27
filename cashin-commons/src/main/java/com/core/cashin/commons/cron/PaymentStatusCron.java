package com.core.cashin.commons.cron;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PaymentStatusCron {

    private final PaymentRepository paymentRepository;

    public PaymentStatusCron(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    @Scheduled(cron = "${jobs.created-to-cancelled.cron}")
    public void expireCreatedPayments() {
        int updated = paymentRepository.updatePaymentStatus(
                PaymentStatusEnum.CANCELLED,
                LocalDateTime.now().minusMinutes(30),
                PaymentStatusEnum.CREATED
        );
        log.debug("[CRON-PAYMENT] {} records were updated from status {} to status {}",
                updated, PaymentStatusEnum.CREATED.name(), PaymentStatusEnum.CANCELLED.name());

    }

}
