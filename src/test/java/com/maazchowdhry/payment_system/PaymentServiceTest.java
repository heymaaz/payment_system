package com.maazchowdhry.payment_system;

import com.maazchowdhry.payment_system.dto.response.PaymentDTO;
import com.maazchowdhry.payment_system.exception.NotEnoughBalanceException;
import com.maazchowdhry.payment_system.exception.UserDoesntExistException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @Mock
    private EntityManager entityManager; // Mock EntityManager for flush verification

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<Specification<Payment>> specificationCaptor;

    private User sender;
    private User receiver;
    private UUID senderId;
    private UUID receiverId;
    private BigDecimal paymentAmount;
    private Payment testPayment;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        paymentAmount = new BigDecimal("100.00");

        sender = new User(new BigDecimal("200.00"));

        receiver = new User(new BigDecimal("50.00"));

        // Simulate userId being set after save
        ReflectionTestUtils.setField(sender, "userId", senderId);
        ReflectionTestUtils.setField(receiver, "userId", receiverId);

        testPayment = new Payment(sender, receiver, paymentAmount);

        // Simulate ID and timestamp being set after save
        ReflectionTestUtils.setField(testPayment, "paymentId", paymentId);
        ReflectionTestUtils.setField(testPayment, "timestamp", LocalDateTime.now());
    }

    @Test
    void save_shouldCallRepositorySaveAndReturnPayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        Payment result = paymentService.save(testPayment);

        assertNotNull(result);
        assertEquals(testPayment, result);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        assertEquals(testPayment, paymentCaptor.getValue());
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(userService, entityManager);
    }

    @Test
    void findById_whenPaymentExists_shouldReturnOptionalWithPaymentDTO() {
        testPayment = spy(testPayment); // Using spy to allow setting fields to simulate the payment having an ID and timestamp for the DTO conversion
        when(testPayment.getPaymentId()).thenReturn(paymentId);
        when(testPayment.getTimestamp()).thenReturn(LocalDateTime.now());

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        Optional<PaymentDTO> result = paymentService.findById(paymentId);

        assertTrue(result.isPresent());
        PaymentDTO dto = result.get();
        assertEquals(testPayment.getPaymentId(), dto.getPaymentId());
        assertEquals(testPayment.getSender().getUserId(), dto.getSenderId());
        assertEquals(testPayment.getReceiver().getUserId(), dto.getReceiverId());
        assertEquals(0, testPayment.getAmount().compareTo(dto.getAmount()));
        assertNotNull(dto.getTimestamp());

        verify(paymentRepository, times(1)).findById(paymentId);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(userService, entityManager);
    }

    @Test
    void findById_whenPaymentDoesNotExist_shouldReturnEmptyOptional() {
        UUID nonExistentId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<PaymentDTO> result = paymentService.findById(nonExistentId);

        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(userService, entityManager);
    }

    @Test
    void processPayment_success() {
        when(userService.findById(senderId)).thenReturn(Optional.of(sender));
        when(userService.findById(receiverId)).thenReturn(Optional.of(receiver));
        doNothing().when(entityManager).flush();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment paymentToSave = invocation.getArgument(0);

            UUID generatedPaymentId = UUID.randomUUID();
            LocalDateTime generatedTimestamp = LocalDateTime.now();
            ReflectionTestUtils.setField(paymentToSave, "paymentId", generatedPaymentId);
            ReflectionTestUtils.setField(paymentToSave, "timestamp", generatedTimestamp);

            return paymentToSave;
        });

        PaymentDTO result = paymentService.processPayment(senderId, receiverId, paymentAmount);

        assertNotNull(result);

        assertEquals(senderId, result.getSenderId());
        assertEquals(receiverId, result.getReceiverId());
        assertEquals(0, paymentAmount.compareTo(result.getAmount()));
        assertNotNull(result.getAmount());
        assertNotNull(result.getTimestamp());

        verify(userService, times(1)).findById(senderId);
        verify(userService, times(1)).findById(receiverId);

        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userService, times(2)).addToUserBalance(any(User.class), amountCaptor.capture());
        List<BigDecimal> capturedAmounts = amountCaptor.getAllValues();
        assertEquals(0, paymentAmount.negate().compareTo(capturedAmounts.get(0)));
        assertEquals(0, paymentAmount.compareTo(capturedAmounts.get(1)));

        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(sender, savedPayment.getSender());
        assertEquals(receiver, savedPayment.getReceiver());
        assertEquals(0, paymentAmount.compareTo(savedPayment.getAmount()));

        verify(entityManager, times(1)).flush();

        verifyNoMoreInteractions(userService, paymentRepository, entityManager);
    }

    @Test
    void processPayment_senderDoesNotExist_shouldThrowUserDoesntExistException() {
        when(userService.findById(senderId)).thenReturn(Optional.empty());

        UserDoesntExistException exception = assertThrows(UserDoesntExistException.class, () -> {
            paymentService.processPayment(senderId, receiverId, paymentAmount);
        });
        assertTrue(exception.getMessage().contains("Sender user"));
        assertTrue(exception.getMessage().contains(senderId.toString()));

        verify(userService, times(1)).findById(senderId);
        verify(userService, never()).findById(receiverId);
        verifyNoInteractions(paymentRepository, entityManager);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void processPayment_receiverDoesNotExist_shouldThrowUserDoesntExistException() {
        when(userService.findById(senderId)).thenReturn(Optional.of(sender));
        when(userService.findById(receiverId)).thenReturn(Optional.empty());

        UserDoesntExistException exception = assertThrows(UserDoesntExistException.class, () -> {
            paymentService.processPayment(senderId, receiverId, paymentAmount);
        });
        assertTrue(exception.getMessage().contains("Receiver user"));
        assertTrue(exception.getMessage().contains(receiverId.toString()));


        verify(userService, times(1)).findById(senderId);
        verify(userService, times(1)).findById(receiverId);
        verifyNoInteractions(paymentRepository, entityManager);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void processPayment_insufficientBalance_shouldThrowNotEnoughBalanceException() {
        BigDecimal highAmount = new BigDecimal("500.00");
        when(userService.findById(senderId)).thenReturn(Optional.of(sender));
        when(userService.findById(receiverId)).thenReturn(Optional.of(receiver));

        assertThrows(NotEnoughBalanceException.class, () -> {
            paymentService.processPayment(senderId, receiverId, highAmount);
        });

        verify(userService, times(1)).findById(senderId);
        verify(userService, times(1)).findById(receiverId);
        verifyNoInteractions(paymentRepository, entityManager);
        verify(userService, never()).addToUserBalance(any(User.class), any(BigDecimal.class));
    }

    @Test
    void findFilteredPayments_shouldCallRepositoryWithCorrectSpecAndPageable() {
        int page = 0;
        int pageSize = 10;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        Pageable expectedPageable = PageRequest.of(page, pageSize, sort);

        Payment payment1 = new Payment(sender, receiver, BigDecimal.TEN);
        Payment payment2 = new Payment(sender, receiver, BigDecimal.ONE);
        List<Payment> paymentList = List.of(payment1, payment2);
        Page<Payment> mockResultPage = new PageImpl<>(paymentList, expectedPageable, paymentList.size());

        when(paymentRepository.findAll(any(Specification.class), eq(expectedPageable)))
                .thenReturn(mockResultPage);

        Page<PaymentDTO> resultPage = paymentService.findFilteredPayments(senderId, receiverId, page, pageSize, startDate, endDate);

        assertNotNull(resultPage);
        assertEquals(paymentList.size(), resultPage.getContent().size());
        assertEquals(page, resultPage.getNumber());
        assertEquals(pageSize, resultPage.getSize());
        assertEquals(paymentList.size(), resultPage.getTotalElements());

        assertEquals(payment1.getAmount(), resultPage.getContent().get(0).getAmount());
        assertEquals(payment2.getAmount(), resultPage.getContent().get(1).getAmount());

        verify(paymentRepository, times(1)).findAll(specificationCaptor.capture(), eq(expectedPageable));

        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(userService, entityManager);
    }
}