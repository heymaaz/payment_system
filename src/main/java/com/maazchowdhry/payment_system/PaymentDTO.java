package com.maazchowdhry.payment_system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDTO {
    private UUID paymentId;
    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    public static PaymentDTO fromEntity(Payment payment) {
        return new PaymentDTO(
                payment.getPaymentId(),
                payment.getSender().getUserId(),
                payment.getReceiver().getUserId(),
                payment.getAmount(),
                payment.getTimestamp()
        );
    }

}
