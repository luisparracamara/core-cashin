package com.core.cashin.commons.entity;

import com.core.cashin.commons.constants.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private String amount;
    private String country;
    private String currency;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_method_name")
    private String paymentMethodName;

    private String description;

    @Column(name = "notification_url")
    private String notificationUrl;

    @Column(name = "success_url")
    private String successUrl;

    @Column(name = "error_url")
    private String errorUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    private String ip;

    @Column(name = "transaction_id")
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "fk_pm_gateway_id")
    private Gateway gateway;

    @ManyToOne
    @JoinColumn(name = "fk_pm_merchant_id")
    private Merchant merchant;

    @ManyToOne
    @JoinColumn(name = "fk_pm_payer")
    private PayerEntity payerEntity;

}
