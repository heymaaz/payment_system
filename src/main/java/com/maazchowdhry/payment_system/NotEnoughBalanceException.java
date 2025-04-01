package com.maazchowdhry.payment_system;

public class NotEnoughBalanceException extends RuntimeException{
    NotEnoughBalanceException() {
        super("You don't have enough funds to complete this transaction");
    }
}
