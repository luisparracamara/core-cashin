package com.core.cashin.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponse {

    private String depositId;
    private String checkoutType;
    private String redirectUrl;
    private String redirectUrlSandbox;
    private PaymentInfoResponse paymentInfo;

}
