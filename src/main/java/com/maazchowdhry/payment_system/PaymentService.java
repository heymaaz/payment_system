package com.maazchowdhry.payment_system;

import com.maazchowdhry.payment_system.dto.response.PaymentDTO;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final EntityManager entityManager;


    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserService userService, EntityManager entityManager) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.entityManager = entityManager;
    }

    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findById(UUID paymentId) {
        return paymentRepository.findById(paymentId).map(PaymentDTO::fromEntity);
    }

    @Transactional
    public PaymentDTO processPayment(UUID senderId, UUID receiverId, BigDecimal amount) {
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

        Payment savedPayment = save(payment);

        entityManager.flush(); // Force flush to get timestamp of the payment

        return PaymentDTO.fromEntity(savedPayment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> findFilteredPayments(UUID senderId, UUID receiverId, int page, int pageSize, LocalDateTime startDate, LocalDateTime endDate) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Specification<Payment> spec = PaymentSpecification.withFilters(senderId,receiverId,startDate,endDate);

        Page<Payment> paymentPage = paymentRepository.findAll(spec, pageable);

        return paymentPage.map(PaymentDTO::fromEntity);
    }
}
