package com.maazchowdhry.payment_system.controller;

import com.maazchowdhry.payment_system.dto.response.PaymentDTO;
import com.maazchowdhry.payment_system.PaymentService;
import com.maazchowdhry.payment_system.dto.request.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(
                                request.getSenderUserId(),
                                request.getReceiverUserId(),
                        request.getAmount()
                        )
                );
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentByPaymentId(@PathVariable UUID paymentId) {
        return paymentService.findById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/payments")
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
