package com.core.cashin.commons.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequest {

    @NotBlank(message = "Amount value is needed for a deposit")
    private String amount;

    @NotBlank(message = "Country value is needed for a deposit")
    private String country;

    @NotBlank(message = "Currency value is needed for a deposit")
    private String currency;

    @NotBlank(message = "paymentMethod value is needed for a deposit")
    private String paymentMethod;

    @Valid
    private Payer payer;

    private String description;
    private String backUrl;
    private String successUrl;
    private String errorUrl;
    private String notificationUrl;

    private MerchantRequest merchant;
    private String connectorName;
    private Map<String, String> gatewayMetadata;

}
