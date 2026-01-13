package com.core.cashin.commons.service.impl;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import com.core.cashin.commons.entity.PayerEntity;
import com.core.cashin.commons.entity.PaymentCashinEntity;
import com.core.cashin.commons.entity.PaymentEntity;
import com.core.cashin.commons.entity.PaymentFeeEntity;
import com.core.cashin.commons.repository.PayerRepository;
import com.core.cashin.commons.repository.PaymentCashinRepository;
import com.core.cashin.commons.repository.PaymentFeeRepository;
import com.core.cashin.commons.repository.PaymentRepository;
import com.core.cashin.commons.service.PaymentOperationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentOperationServiceImpl implements PaymentOperationService {

    private final PayerRepository payerRepository;
    private final PaymentCashinRepository paymentCashinRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentFeeRepository paymentFeeRepository;

    public PaymentOperationServiceImpl(PayerRepository payerRepository, PaymentCashinRepository paymentCashinRepository, PaymentRepository paymentRepository, PaymentFeeRepository paymentFeeRepository) {
        this.payerRepository = payerRepository;
        this.paymentCashinRepository = paymentCashinRepository;
        this.paymentRepository = paymentRepository;
        this.paymentFeeRepository = paymentFeeRepository;
    }

    @Override
    @Transactional
    public void savePaymentCashin(PaymentEntity paymentEntity, PaymentCashinEntity paymentCashinEntity, PayerEntity payerEntity) {
        paymentEntity.setPayerEntity(savePayer(payerEntity));
        paymentRepository.save(paymentEntity);
        paymentFeeRepository.save(paymentEntity.getPaymentFeeEntity());
        paymentCashinRepository.save(paymentCashinEntity);
    }

    @Override
    @Transactional
    public void savePaymentCashin(PaymentEntity paymentEntity, PayerEntity payerEntity) {
        paymentEntity.setPayerEntity(savePayer(payerEntity));
        paymentEntity.setPaymentFeeEntity(savePaymentFee(paymentEntity.getPaymentFeeEntity()));
        paymentRepository.save(paymentEntity);
    }

    @Override
    @Transactional
    public void savePaymentCashinPending(PaymentEntity paymentEntity, PaymentCashinEntity paymentCashinEntity) {
        paymentEntity.setStatus(PaymentStatusEnum.PENDING);
        paymentEntity.setUpdatedAt(LocalDateTime.now());
        paymentCashinRepository.save(paymentCashinEntity);
        paymentRepository.save(paymentEntity);
    }

    private PayerEntity savePayer(PayerEntity payerEntity) {
        return payerRepository
                .findByDocument(payerEntity.getDocument())
                .orElseGet(() -> payerRepository.save(payerEntity));
    }

    private PaymentFeeEntity savePaymentFee(PaymentFeeEntity paymentFeeEntity) {
        return paymentFeeRepository.save(paymentFeeEntity);
    }

}
