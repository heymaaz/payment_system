package com.maazchowdhry.payment_system;

import com.maazchowdhry.payment_system.dto.request.PaymentRequest;
import com.maazchowdhry.payment_system.dto.request.UserRegistrationRequest;
import com.maazchowdhry.payment_system.dto.response.PaymentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User registerUserViaApi(String initialBalance) {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setInitialBalance(new BigDecimal(initialBalance));
        ResponseEntity<User> response = restTemplate.postForEntity(
                baseUrl + "/users", req, User.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getUserId());
        return response.getBody();
    }

    private PaymentDTO processPaymentViaApi(User sender, User receiver, BigDecimal amount) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSenderUserId(sender.getUserId());
        paymentRequest.setReceiverUserId(receiver.getUserId());
        paymentRequest.setAmount(amount);

        ResponseEntity<PaymentDTO> response = restTemplate.postForEntity(baseUrl + "/payments", paymentRequest, PaymentDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getPaymentId());
        return response.getBody();
    }

    @Test
    void testUserRegistrationAndRetrieval() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        BigDecimal initialBalance = new BigDecimal("100.50");
        request.setInitialBalance(initialBalance);

        ResponseEntity<User> registrationResponse = restTemplate.postForEntity(
                baseUrl + "/users", request, User.class);

        assertEquals(HttpStatus.CREATED, registrationResponse.getStatusCode());
        User createdUser = registrationResponse.getBody();
        assertNotNull(createdUser);
        assertNotNull(createdUser.getUserId());
        assertEquals(0, initialBalance.compareTo(createdUser.getInitialBalance()));
        assertEquals(0, initialBalance.compareTo(createdUser.getBalance()));
        assertNotNull(createdUser.getCreatedAt());

        UUID createdUserId = createdUser.getUserId();
        ResponseEntity<User> retrievalResponse = restTemplate.getForEntity(
                baseUrl + "/users/" + createdUserId, User.class);

        assertEquals(HttpStatus.OK, retrievalResponse.getStatusCode());
        User retrievedUser = retrievalResponse.getBody();
        assertNotNull(retrievedUser);
        assertEquals(createdUserId, retrievedUser.getUserId());
        assertEquals(0, initialBalance.compareTo(retrievedUser.getBalance()));
    }

    @Test
    void testGetUser_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<User> response = restTemplate.getForEntity(
                baseUrl + "/users/" + nonExistentId, User.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testProcessPayment_Success() {
        User sender = registerUserViaApi("200.00");
        User receiver = registerUserViaApi("50.00");
        BigDecimal amount = new BigDecimal("75.25");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSenderUserId(sender.getUserId());
        paymentRequest.setReceiverUserId(receiver.getUserId());
        paymentRequest.setAmount(amount);

        ResponseEntity<PaymentDTO> paymentResponse = restTemplate.postForEntity(baseUrl + "/payments", paymentRequest, PaymentDTO.class);

        assertEquals(HttpStatus.CREATED, paymentResponse.getStatusCode());
        PaymentDTO paymentDTO = paymentResponse.getBody();
        assertNotNull(paymentDTO);
        assertNotNull(paymentDTO.getPaymentId());
        assertNotNull(paymentDTO.getTimestamp());
        assertEquals(sender.getUserId(), paymentDTO.getSenderId());
        assertEquals(receiver.getUserId(), paymentDTO.getReceiverId());
        assertEquals(0, amount.compareTo(paymentDTO.getAmount()));

        User updatedSender = userRepository.findById(sender.getUserId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getUserId()).orElseThrow();
        Payment savedPayment = paymentRepository.findById(paymentDTO.getPaymentId()).orElseThrow();

        assertEquals(0, new BigDecimal("124.75").compareTo(updatedSender.getBalance()));
        assertEquals(0, new BigDecimal("125.25").compareTo(updatedReceiver.getBalance()));

        assertEquals(savedPayment.getPaymentId(), paymentDTO.getPaymentId());
        assertEquals(sender.getUserId(), savedPayment.getSender().getUserId());
        assertEquals(receiver.getUserId(), savedPayment.getReceiver().getUserId());
        assertEquals(0, amount.compareTo(savedPayment.getAmount()));
    }

    @Test
    void testProcessPayment_InsufficientFunds() {
        User sender = registerUserViaApi("50.00");
        User receiver = registerUserViaApi("100.00");
        BigDecimal amount = new BigDecimal("75.00");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSenderUserId(sender.getUserId());
        paymentRequest.setReceiverUserId(receiver.getUserId());
        paymentRequest.setAmount(amount);

        ResponseEntity<String> paymentResponse = restTemplate.postForEntity(
                baseUrl + "/payments", paymentRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponse.getStatusCode()); // ADJUST if your exception handling differs

        User finalSender = userRepository.findById(sender.getUserId()).orElseThrow();
        User finalReceiver = userRepository.findById(receiver.getUserId()).orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(finalSender.getBalance())); // Balance unchanged
        assertEquals(0, new BigDecimal("100.00").compareTo(finalReceiver.getBalance())); // Balance unchanged
        assertEquals(0, paymentRepository.count()); // No payment saved
    }


    @Test
    void testProcessPayment_SenderNotFound() {
        User receiver = registerUserViaApi("100.00");
        UUID fakeSenderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("20.00");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSenderUserId(fakeSenderId);
        paymentRequest.setReceiverUserId(receiver.getUserId());
        paymentRequest.setAmount(amount);

        ResponseEntity<String> paymentResponse = restTemplate.postForEntity(baseUrl + "/payments", paymentRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponse.getStatusCode());

        User finalReceiver = userRepository.findById(receiver.getUserId()).orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(finalReceiver.getBalance()));
        assertEquals(0, paymentRepository.count());
    }

    @Test
    void testProcessPayment_ReceiverNotFound() {
        User sender = registerUserViaApi("100.00");
        UUID fakeReceiverId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("20.00");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSenderUserId(sender.getUserId());
        paymentRequest.setReceiverUserId(fakeReceiverId);
        paymentRequest.setAmount(amount);

        ResponseEntity<String> paymentResponse = restTemplate.postForEntity(baseUrl + "/payments", paymentRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponse.getStatusCode());

        User finalReceiver = userRepository.findById(sender.getUserId()).orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(finalReceiver.getBalance()));
        assertEquals(0, paymentRepository.count());
    }


    @Test
    void testGetPaymentById_Success() {
        User sender = registerUserViaApi("200.00");
        User receiver = registerUserViaApi("50.00");
        BigDecimal amount = new BigDecimal("75.25");
        PaymentDTO createdPayment = processPaymentViaApi(sender, receiver, amount);

        ResponseEntity<PaymentDTO> retrievalResponse = restTemplate.getForEntity(
                baseUrl + "/payments/" + createdPayment.getPaymentId(), PaymentDTO.class);

        assertEquals(HttpStatus.OK, retrievalResponse.getStatusCode());
        PaymentDTO retrievedPayment = retrievalResponse.getBody();
        assertNotNull(retrievedPayment);
        assertEquals(createdPayment.getPaymentId(), retrievedPayment.getPaymentId());
        assertEquals(0, createdPayment.getAmount().compareTo(retrievedPayment.getAmount()));
    }

    @Test
    void testGetPaymentById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<PaymentDTO> response = restTemplate.getForEntity(
                baseUrl + "/payments/" + nonExistentId, PaymentDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}