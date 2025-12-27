package com.core.cashin.commons.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gateway_payment_method")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gateway_pm_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_gpm_payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "fk_gpm_gateway_id")
    private Gateway gateway;

    private String currency;

}
