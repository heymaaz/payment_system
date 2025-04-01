package com.maazchowdhry.payment_system;

public class UserDoesntExistException extends RuntimeException{
    UserDoesntExistException(String message) {
        super(message);
    }
}
