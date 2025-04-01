package com.maazchowdhry.payment_system;

import com.maazchowdhry.payment_system.exception.NotEnoughBalanceException;
import com.maazchowdhry.payment_system.exception.UserDoesntExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            NotEnoughBalanceException.class,
            UserDoesntExistException.class
    })
    @ResponseBody
    public Map<String, Object> handleExceptions(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            log.warn("Validation failed: {}", validationEx.getMessage());

            Optional<FieldError> fieldErrorOptional = validationEx.getBindingResult().getFieldErrors().stream().findFirst();

            if (fieldErrorOptional.isPresent()) {
                FieldError fieldError = fieldErrorOptional.get();
                message = fieldError.getDefaultMessage();
                response.put("field", fieldError.getField());
                log.warn("Validation error - Field: '{}', Message: '{}'", fieldError.getField(), message);
            } else {
                Optional<ObjectError> globalErrorOptional = validationEx.getBindingResult().getGlobalErrors().stream().findFirst();
                if (globalErrorOptional.isPresent()) {
                    ObjectError globalError = globalErrorOptional.get();
                    message = globalError.getDefaultMessage();
                    log.warn("Validation error - Object: '{}', Message: '{}'", globalError.getObjectName(), message);
                }
            }
        }
        response.put("message", message);
        return response;
    }
}
