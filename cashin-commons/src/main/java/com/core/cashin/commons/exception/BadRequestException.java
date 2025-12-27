package com.core.cashin.commons.exception;

public class BadRequestException extends DomainException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable ex) {
        super(message, ex);
    }
}
