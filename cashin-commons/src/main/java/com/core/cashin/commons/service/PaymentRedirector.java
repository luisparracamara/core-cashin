package com.core.cashin.commons.service;

import com.core.cashin.commons.constants.CashInMethod;
import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;

public interface PaymentRedirector {

    ConnectorEnum getConnector();

    CashInMethod getCashInMethod();

    DepositResponse create(DepositRequest request);

}
