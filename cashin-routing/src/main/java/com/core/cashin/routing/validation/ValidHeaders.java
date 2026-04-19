package com.core.cashin.routing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = HeadersValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHeaders {
    String[] required() default {"uuid"};
    String message() default "Missing required headers";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
