package com.core.cashin.commons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_balance")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_balance_id")
    private Long id;

    private BigDecimal amount;

    @Column(name = "reference_fee_id")
    private String referenceFeeId;

    private String currency;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_mb_merchant_id", nullable = false)
    private Merchant merchant;

}
