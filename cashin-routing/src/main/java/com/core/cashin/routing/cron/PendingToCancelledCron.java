package com.core.cashin.routing.cron;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.model.PaymentCashinWithGateway;
import com.core.cashin.commons.repository.PaymentCashinRepository;
import com.core.cashin.commons.repository.PaymentRepository;
import com.core.cashin.routing.service.RoutingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PendingToCancelledCron {

    private final PaymentCashinRepository paymentCashinRepository;

    private final RoutingService routingService;

    private final PaymentRepository paymentRepository;

    public PendingToCancelledCron(PaymentCashinRepository paymentCashinRepository, RoutingService routingService, PaymentRepository paymentRepository) {
        this.paymentCashinRepository = paymentCashinRepository;
        this.routingService = routingService;
        this.paymentRepository = paymentRepository;
    }

    // Se ejecuta cada 3 días, a las 12am, cada 10 min por 1 hora
    @Scheduled( cron = "${jobs.pending-to-cancel.cron}", zone = "${jobs.pending-to-cancel.zone}")
    @Transactional
    public void checkPendingPayments() {
        log.info("[CRON-PAYMENT-PENDING-TO-CANCELLED] Starting cron");

        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusHours(72);
        List<PaymentCashinWithGateway> paymentCashinWithGateways = paymentCashinRepository.findPendingPaymentCashinForReconciliation(
                        cutoff,
                        PageRequest.of(0, 20)
        );

        List<Long> toCancel = new ArrayList<>();
        paymentCashinWithGateways.forEach(payment -> {
            boolean isDepositCompleted = routingService.checkStatusDeposit(ConnectorEnum.fromDisplayName(payment.getConnectorName()),
                    payment.getPaymentCashin().getExternalReference());

            if (!isDepositCompleted) {
                toCancel.add(payment.getPaymentCashin().getPaymentEntity().getId());
            }
        });

        if (!toCancel.isEmpty()) {
            paymentRepository.cancelPaymentStatus(PaymentStatusEnum.CANCELLED, toCancel);

            log.debug("[CRON-PAYMENT-PENDING-TO-CANCELLED] records with id {} were updated from status {} to status {}",
                    toCancel,
                    PaymentStatusEnum.PENDING.name(), PaymentStatusEnum.CANCELLED.name());
        }

    }

}
