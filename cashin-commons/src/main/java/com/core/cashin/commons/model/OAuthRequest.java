package com.core.cashin.commons.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuthRequest {

    private String redirectUri;
    private String state;
    private String connectorName;
    private Long merchantId;

}
