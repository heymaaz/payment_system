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
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="payment_id", updatable = false, columnDefinition = "UUID")
    private UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id", nullable = false, updatable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "user_id", nullable = false, updatable = false)
    private User receiver;

    @Column(name="amount", updatable = false, nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name="timestamp", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime timestamp;


    public Payment(User sender, User receiver, BigDecimal amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = Objects.requireNonNullElse(amount, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void setAmount(BigDecimal newAmount) {
        this.amount = Objects.requireNonNullElse(newAmount, BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + "'" +
                ", senderId=" + sender +
                ", receiverId=" + receiver +
                ", timestamp=" + timestamp +
                '}';
    }
}