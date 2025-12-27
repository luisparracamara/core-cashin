package com.core.cashin.routing.service;

import com.core.cashin.commons.constants.ConnectorEnum;
import com.core.cashin.commons.model.DepositRequest;
import com.core.cashin.commons.model.DepositResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface RoutingService {

    DepositResponse createDeposit(DepositRequest depositRequest, HttpServletRequest httpServletRequest, Map<String, String> headers);

    boolean checkStatusDeposit(ConnectorEnum connector, String id);
}
