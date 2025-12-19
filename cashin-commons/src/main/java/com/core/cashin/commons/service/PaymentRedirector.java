package com.core.cashin.commons.service;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.DepositRequest;

public interface PaymentRedirector {

    ConnectorEnum getConnector();
    void redirect(DepositRequest request);

}
