package com.core.cashin.routing.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "routing_gateway")
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routing_gateway_id")
    @ToString.Include
    private Long id;

    // 🔹 Merchant
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_rg_merchant_id", nullable = false)
    private Merchant merchant;

    // 🔹 Routing Rule
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_rg_routing_rule_id", nullable = false)
    private RoutingRule routingRule;

    // 🔹 Gateway
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_rg_gateway_id", nullable = false)
    private Gateway gateway;

    private String status;

}
