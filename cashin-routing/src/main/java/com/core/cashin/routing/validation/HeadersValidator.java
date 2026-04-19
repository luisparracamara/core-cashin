package com.core.cashin.routing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeadersValidator implements ConstraintValidator<ValidHeaders, Map<String, String>> {

    private String[] required;

    @Override
    public void initialize(ValidHeaders annotation) {
        this.required = annotation.required();
    }

    @Override
    public boolean isValid(Map<String, String> headers, ConstraintValidatorContext context) {
        if (headers == null) return false;

        List<String> missing = new ArrayList<>();
        for (String header : required) {
            if (!headers.containsKey(header) || headers.get(header).isBlank()) {
                missing.add(header);
            }
        }

        if (!missing.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Missing required headers: " + String.join(", ", missing)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

}
