package com.core.cashin.commons.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequest {

    @NotNull(message = "Amount value is needed for a deposit")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;

    @NotBlank(message = "Country value is needed for a deposit")
    private String country;

    @NotBlank(message = "Currency value is needed for a deposit")
    private String currency;

    @NotBlank(message = "paymentMethod value is needed for a deposit")
    private String paymentMethod;

    private String paymentMethodName;

    @Valid
    private Payer payer;

    private String description;
    private String backUrl;
    private String successUrl;
    private String errorUrl;
    private String ip;
    private String notificationUrl;

    @NotNull(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$",
             message = "Date must follow format: yyyy-MM-ddTHH:mm:ssZ (example: 2025-12-20T15:57:25Z)")
    private String date;

    private Long gatewayId;

    private MerchantRequest merchant;
    private String connectorName;
    private Map<String, String> gatewayMetadata;
    private Map<String, String> paymentData;

}
