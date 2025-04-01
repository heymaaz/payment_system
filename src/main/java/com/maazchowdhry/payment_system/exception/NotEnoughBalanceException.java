package com.maazchowdhry.payment_system.exception;

public class NotEnoughBalanceException extends RuntimeException{
    public NotEnoughBalanceException() {
        super("Not enough balance");
    }
}
