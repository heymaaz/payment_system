package com.maazchowdhry.payment_system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="user_id", updatable = false, columnDefinition = "UUID")
    private UUID userId;

    @Column(name="initial_balance", updatable = false, nullable = false, precision = 12, scale = 2)
    private BigDecimal initialBalance;

    @Column(name="balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name="created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public User(BigDecimal initialBalance) {
        this.initialBalance = Objects.requireNonNullElse(initialBalance, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        this.balance = this.initialBalance;
    }

    public void setBalance(BigDecimal newBalance) {
        this.balance = Objects.requireNonNullElse(newBalance, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + "'" +
                ", initialBalance=" + initialBalance +
                ", balance=" + balance +
                ", createdAt=" + createdAt +
                '}';
    }
}
