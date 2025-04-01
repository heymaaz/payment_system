package com.maazchowdhry.payment_system.dto.request;

import com.maazchowdhry.payment_system.validation.NotSameUserIds;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NotSameUserIds
public class PaymentRequest {
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount should be at least 0.01")
    private BigDecimal amount;

    @NotNull(message = "senderUserId is required")
    private UUID senderUserId;

    @NotNull(message = "receiverUserId is required")
    private UUID receiverUserId;
}