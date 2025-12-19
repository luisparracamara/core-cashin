package com.core.cashin.routing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_method")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @Column(name = "payment_method_id")
    private Long id;

    private String code;
    private String logo;

    @Column(name = "payment_type")
    private Long paymentType;

    private String country;
    private String description;
    private String status;
    private String name;

}
