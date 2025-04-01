package com.maazchowdhry.payment_system.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotSameUserIdsValidator.class)
public @interface NotSameUserIds {
    String message() default "Sender and receiver user IDs must be different";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
