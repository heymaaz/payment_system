package com.maazchowdhry.payment_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Transactional
    public Payment processPayment(UUID senderId, UUID receiverId, BigDecimal amount) {
        //check sender and receiver exist
        User sender = userService.findById(senderId).orElseThrow(() -> new UserDoesntExistException("Sender user with id " + senderId + " does not exist"));
        User receiver = userService.findById(receiverId).orElseThrow(() -> new UserDoesntExistException("Receiver user with id " + receiverId + " does not exist"));

        //check sender has sufficient balance to cover the transaction
        if(sender.getBalance().compareTo(amount)<0) {
            throw new NotEnoughBalanceException();
        }

        userService.addToUserBalance(sender, amount.negate());
        userService.addToUserBalance(receiver, amount);

        Payment payment = new Payment(sender, receiver, amount);

        return paymentRepository.save(payment);
    }
}
