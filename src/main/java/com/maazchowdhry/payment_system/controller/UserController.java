package com.maazchowdhry.payment_system.controller;

import com.maazchowdhry.payment_system.User;
import com.maazchowdhry.payment_system.UserService;
import com.maazchowdhry.payment_system.dto.request.UserRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
