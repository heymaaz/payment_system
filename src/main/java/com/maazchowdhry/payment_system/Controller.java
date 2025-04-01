package com.maazchowdhry.payment_system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class Controller {
    private final UserService userService;
    private final PaymentService paymentService;

    public Controller(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @Getter
    @Setter
    public static class UserRegistrationRequest {
        @NotNull(message = "initial balance is required")
        @DecimalMin(value = "0.0", message = "initial balance cannot be negative")
        private BigDecimal initialBalance;
    }

    @PostMapping("api/v1/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.save(new User(request.initialBalance)));
    }

    @GetMapping("/api/v1/user/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable UUID userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Getter
    @Setter
    public static class PaymentRequest {
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount should be at least 0.01")
        private BigDecimal amount;

        @NotNull(message = "senderUserId is required")
        private UUID senderUserId;

        @NotNull(message = "receiverUserId is required")
        private UUID receiverUserId;
    }

    @PostMapping("api/v1/payment")
    public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(
                                request.getSenderUserId(),
                                request.getReceiverUserId(),
                                request.amount
                        )
                );
    }

    @GetMapping("api/v1/payment/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentByPaymentId(@PathVariable UUID paymentId) {
        return paymentService.findById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("api/v1/payment")
    public ResponseEntity<Page<PaymentDTO>> getPayments(
            @RequestParam(required = false) UUID senderId,
            @RequestParam(required = false) UUID receiverId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        return paymentService.findFilteredPayments(senderId, receiverId, page, pageSize, startDate, endDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
