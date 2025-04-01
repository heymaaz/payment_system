package com.maazchowdhry.payment_system.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UserRegistrationRequest {
    @NotNull(message = "initial balance is required")
    @DecimalMin(value = "0.0", message = "initial balance cannot be negative")
    private BigDecimal initialBalance;
}
