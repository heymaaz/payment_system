package com.maazchowdhry.payment_system.controller;

import com.maazchowdhry.payment_system.User;
import com.maazchowdhry.payment_system.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Getter
    @Setter
    public static class UserRegistrationRequest {
        @NotNull(message = "initial balance is required")
        @DecimalMin(value = "0.0", message = "initial balance cannot be negative")
        private BigDecimal initialBalance;
    }

    @PostMapping("/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.save(new User(request.initialBalance)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserByUserId(@PathVariable UUID userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
