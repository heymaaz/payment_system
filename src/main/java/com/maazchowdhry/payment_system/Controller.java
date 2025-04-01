package com.maazchowdhry.payment_system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

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

//    TODO: Better Error Handling
//    TODO: Get("/api/v1/user/{userId}")
//    TODO: Post("/api/v1/payment")
//    TODO: Get("api/v1/payment/{paymentId}")
//    TODO: Get("api/v1/payment")
}
