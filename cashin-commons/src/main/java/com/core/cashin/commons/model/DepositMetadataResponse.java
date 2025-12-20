package com.core.cashin.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMetadataResponse {

    private String beneficiaryName;
    private String payerName;
    private String clabe;

}
