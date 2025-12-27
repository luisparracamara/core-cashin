package com.core.cashin.commons.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routing_rule")
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routing_rule_id")
    @ToString.Include
    private Long id;

    // 🔹 Payment Method
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_rr_payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    // 🔹 País
    @Column(name = "country", nullable = false, length = 2)
    @ToString.Include
    private String country;

}
