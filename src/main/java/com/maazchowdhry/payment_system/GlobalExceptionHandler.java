package com.maazchowdhry.payment_system;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public Map<String, Object> handleExceptions(Exception ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            FieldError firstError = validationEx.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .orElse(null);

            if (firstError != null) {
                response.put("message", firstError.getDefaultMessage());
                response.put("field", firstError.getField());
            }
        }
        else {
            response.put("message", ex.getMessage());
        }
        return response;
    }
}
