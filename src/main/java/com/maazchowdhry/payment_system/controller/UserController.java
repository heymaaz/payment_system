package com.maazchowdhry.payment_system.controller;

import com.maazchowdhry.payment_system.PaymentService;
import com.maazchowdhry.payment_system.User;
import com.maazchowdhry.payment_system.UserService;
import com.maazchowdhry.payment_system.dto.request.UserRegistrationRequest;
import com.maazchowdhry.payment_system.dto.response.PaymentDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PaymentService paymentService;

    public UserController(UserService userService, PaymentService paymentService) {
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @PostMapping("/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.save(new User(request.getInitialBalance())));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable UUID userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<Page<PaymentDTO>> getPaymentsforUserId(
            @PathVariable UUID userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate
    ) {
        Page<PaymentDTO> results = paymentService.findFilteredPaymentsForUserId(userId, page, pageSize, startDate, endDate);
        return ResponseEntity.ok(results);
    }
}
