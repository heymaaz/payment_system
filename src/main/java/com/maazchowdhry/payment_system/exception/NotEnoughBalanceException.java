package com.maazchowdhry.payment_system.exception;

public class NotEnoughBalanceException extends RuntimeException{
    public NotEnoughBalanceException() {
        super("You don't have enough funds to complete this transaction");
    }
}
