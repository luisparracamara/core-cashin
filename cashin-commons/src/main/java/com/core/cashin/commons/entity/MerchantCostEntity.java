package com.core.cashin.commons.entity;

import com.core.cashin.commons.constants.MerchantCostEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "merchant_cost")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantCostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_cost_id")
    private Long id;

    @Column(name = "fee_type")
    private MerchantCostEnum feeType;

    private String currency;

    @Column(name = "fee_rate")
    private Integer feeRate;

    @Column(name = "fix_rate")
    private BigDecimal fixRate;

    @OneToOne
    @JoinColumn(name = "fk_mc_merchant_id")
    private Merchant merchant;

}
