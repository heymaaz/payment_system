package com.maazchowdhry.payment_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }
}
