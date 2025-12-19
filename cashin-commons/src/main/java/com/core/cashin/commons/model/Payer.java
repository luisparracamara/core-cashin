package com.core.cashin.commons.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payer {

    private String document;

    private String documentType;

    @NotBlank(message = "First name is needed for a payer")
    private String firstName;

    @NotBlank(message = "Last name is needed for a payer")
    private String lastName;

    private String email;
}
