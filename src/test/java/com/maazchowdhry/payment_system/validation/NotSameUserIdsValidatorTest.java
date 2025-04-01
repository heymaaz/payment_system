package com.maazchowdhry.payment_system.validation;

import com.maazchowdhry.payment_system.dto.request.PaymentRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotSameUserIdsValidatorTest {

    private NotSameUserIdsValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        validator = new NotSameUserIdsValidator();
    }

    private PaymentRequest createRequest(UUID senderId, UUID receiverId) {
        PaymentRequest request = new PaymentRequest();
        request.setSenderUserId(senderId);
        request.setReceiverUserId(receiverId);
        request.setAmount(BigDecimal.TEN);
        return request;
    }

    @Test
    void isValid_whenSenderAndReceiverAreDifferent_shouldReturnTrue() {
        PaymentRequest request = createRequest(UUID.randomUUID(), UUID.randomUUID());
        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void isValid_whenSenderAndReceiverAreSame_shouldReturnFalse() {
        UUID commonId = UUID.randomUUID();
        PaymentRequest request = createRequest(commonId, commonId);
        assertFalse(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void isValid_whenSenderIdIsNull_shouldReturnTrue() {
        PaymentRequest request = createRequest(null, UUID.randomUUID());
        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void isValid_whenReceiverIdIsNull_shouldReturnTrue() {
        PaymentRequest request = createRequest(UUID.randomUUID(), null);
        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void isValid_whenBothIdsAreNull_shouldReturnTrue() {
        PaymentRequest request = createRequest(null, null);
        assertTrue(validator.isValid(request, constraintValidatorContext));
    }

    @Test
    void isValid_whenPaymentRequestIsNull_shouldReturnTrue() {
        assertTrue(validator.isValid(null, constraintValidatorContext));
    }
}
