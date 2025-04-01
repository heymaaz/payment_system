package com.maazchowdhry.payment_system.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private PaymentRequest createValidRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setSenderUserId(UUID.randomUUID());
        request.setReceiverUserId(UUID.randomUUID());
        request.setAmount(new BigDecimal("10.50"));
        return request;
    }

    private boolean hasViolationForProperty(Set<? extends ConstraintViolation<?>> violations, String propertyPath) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(propertyPath));
    }

    @Test
    void whenRequestIsValid_shouldHaveNoViolations() {
        PaymentRequest request = createValidRequest();
        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenAmountIsNull_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        request.setAmount(null);

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, "amount"));
        assertEquals("amount is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenAmountIsZero_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        request.setAmount(BigDecimal.ZERO); // 0.0 is not > 0.0

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, "amount"));
        // Optional: Check message
        assertEquals("amount should be at least 0.01", violations.iterator().next().getMessage());
    }

    @Test
    void whenAmountIsExactlyZeroPointZero_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        request.setAmount(new BigDecimal("0.0"));

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, "amount"));
    }

    @Test
    void whenAmountIsNegative_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        request.setAmount(new BigDecimal("-10.00"));

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, "amount"));
    }

    @Test
    void whenAmountIsPositiveAndMinimal_shouldHaveNoViolation() {
        PaymentRequest request = createValidRequest();
        request.setAmount(new BigDecimal("0.01"));

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenSenderIdIsNull_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        request.setSenderUserId(null);

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, "senderUserId"));
        assertEquals("senderUserId is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenReceiverIdIsNull_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        request.setReceiverUserId(null);

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, "receiverUserId"));
    }

    @Test
    void whenSenderAndReceiverIdsAreSame_shouldHaveViolation() {
        PaymentRequest request = createValidRequest();
        UUID commonId = UUID.randomUUID();
        request.setSenderUserId(commonId);
        request.setReceiverUserId(commonId);

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(hasViolationForProperty(violations, ""));
        assertEquals("Sender and receiver user IDs must be different", violations.iterator().next().getMessage());
    }

    @Test
    void whenMultipleFieldsAreInvalid_shouldHaveMultipleViolations() {
        PaymentRequest request = createValidRequest();
        UUID commonId = UUID.randomUUID();
        request.setSenderUserId(commonId);
        request.setReceiverUserId(commonId);
        request.setAmount(null);

        Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        Set<String> propertyPaths = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertTrue(propertyPaths.contains("amount"));
        assertTrue(propertyPaths.contains(""));
    }
}