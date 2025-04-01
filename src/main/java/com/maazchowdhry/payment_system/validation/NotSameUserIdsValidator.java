package com.maazchowdhry.payment_system.validation;

import com.maazchowdhry.payment_system.dto.request.PaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class NotSameUserIdsValidator implements ConstraintValidator<NotSameUserIds, PaymentRequest> {
    @Override
    public boolean isValid(PaymentRequest paymentRequest, ConstraintValidatorContext context) {
        if (paymentRequest == null || paymentRequest.getSenderUserId() == null || paymentRequest.getReceiverUserId() == null) {
            return true;
        }
        return !Objects.equals(paymentRequest.getSenderUserId(), paymentRequest.getReceiverUserId());
    }
}
