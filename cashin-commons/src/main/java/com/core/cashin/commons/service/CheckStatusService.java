package com.core.cashin.commons.service;

import com.core.cashin.commons.model.CheckStatusResponse;

public interface CheckStatusService {
    CheckStatusResponse checkStatusDeposit(long id);
}
