package com.core.cashin.commons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_fee")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantFeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_fee_id")
    private Long id;

    @Column(name = "gross_amount")
    private String grossAmount;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "net_amount")
    private String netAmount;

    private String currency;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "fk_mf_payment_id")
    private PaymentEntity paymentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_mf_merchant_id", nullable = false)
    private Merchant merchant;

}
