package com.maazchowdhry.payment_system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User(new BigDecimal("100.00"));
    }

    @Test
    void save_shouldCallRepositorySaveAndReturnUser() {
        User userToSave = new User(new BigDecimal("50.00"));
        User savedUser = new User(new BigDecimal("50.00"));

        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = userService.save(userToSave);

        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(userRepository, times(1)).save(userToSave);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_whenUserExists_shouldReturnOptionalWithUser() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(testUserId);

        assertTrue(result.isPresent(), "User should be found");
        assertEquals(testUser, result.get(), "Returned user should match the test user");
        verify(userRepository, times(1)).findById(testUserId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_whenUserDoesNotExist_shouldReturnEmptyOptional() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(nonExistentId);

        assertTrue(result.isEmpty(), "Optional should be empty for non-existent user");
        verify(userRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void addToUserBalance_shouldUpdateBalanceAndCallSave() {
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal amountToAdd = new BigDecimal("25.50");
        BigDecimal expectedBalance = new BigDecimal("125.50").setScale(2, RoundingMode.HALF_UP);

        User userToUpdate = new User(initialBalance);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class); // To capture the user passed to save

        userService.addToUserBalance(userToUpdate, amountToAdd);

        assertEquals(expectedBalance, userToUpdate.getBalance(), "User balance should be updated locally");

        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(expectedBalance, savedUser.getBalance(), "Balance of the user passed to repository save should be correct");
        assertEquals(userToUpdate, savedUser, "The same user instance should have been saved");

        verifyNoMoreInteractions(userRepository);
    }
}