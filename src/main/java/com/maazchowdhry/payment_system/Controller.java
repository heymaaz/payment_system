package com.maazchowdhry.payment_system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
public class Controller {
    private final UserRepository userRepository;

    public Controller(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Getter
    @Setter
    public static class UserRegistrationRequest {
        @NotNull(message = "initial balance is required")
        @DecimalMin(value = "0.0", message = "initial balance cannot be negative")
        private BigDecimal initialBalance;
    }

    @PostMapping("api/v1/user")
    public User registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return userRepository.save(new User(request.initialBalance));
    }

    @GetMapping("/api/v1/user/{userId}")
    public Optional<User> getUserByUserID(@PathVariable UUID userId) {
//    TODO: return 404 not found if not found.
        return userRepository.findById(userId);
    }
//    TODO: Post("/api/v1/payment")
//    TODO: Get("api/v1/payment/{paymentId}")
//    TODO: Get("api/v1/payment")
}
