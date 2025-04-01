package com.maazchowdhry.payment_system.controller;

import com.maazchowdhry.payment_system.PaymentDTO;
import com.maazchowdhry.payment_system.PaymentService;
import jakarta.validation.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = NotSameUserIdsValidator.class)
    @interface NotSameUserIds {
        String message() default "Sender and receiver user IDs must be different";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }


    static class NotSameUserIdsValidator implements ConstraintValidator<NotSameUserIds, PaymentRequest> {
        @Override
        public boolean isValid(PaymentRequest paymentRequest, ConstraintValidatorContext context) {
            if (paymentRequest == null || paymentRequest.getSenderUserId() == null || paymentRequest.getReceiverUserId() == null) {
                return true;
            }
            return !Objects.equals(paymentRequest.getSenderUserId(), paymentRequest.getReceiverUserId());
        }
    }

    @Getter
    @Setter
    @NotSameUserIds
    public static class PaymentRequest {
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount should be at least 0.01")
        private BigDecimal amount;

        @NotNull(message = "senderUserId is required")
        private UUID senderUserId;

        @NotNull(message = "receiverUserId is required")
        private UUID receiverUserId;
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(
                                request.getSenderUserId(),
                                request.getReceiverUserId(),
                                request.amount
                        )
                );
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentByPaymentId(@PathVariable UUID paymentId) {
        return paymentService.findById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/payment")
    public ResponseEntity<Page<PaymentDTO>> getPayments(
            @RequestParam(required = false) UUID senderId,
            @RequestParam(required = false) UUID receiverId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        Page<PaymentDTO> results = paymentService.findFilteredPayments(senderId, receiverId, page, pageSize, startDate, endDate);
        return ResponseEntity.ok(results);
    }
}
