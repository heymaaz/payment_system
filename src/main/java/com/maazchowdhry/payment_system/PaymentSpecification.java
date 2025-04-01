package com.maazchowdhry.payment_system;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentSpecification {

    public static Specification<Payment> withFilters(UUID senderId, UUID receiverId, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (senderId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sender").get("userId"), senderId));
            }

            if (receiverId != null) {
                predicates.add(criteriaBuilder.equal(root.get("receiver").get("userId"), receiverId));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Payment> withFilters(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> finalPredicates = new ArrayList<>();

            if (userId != null) {
                Predicate senderIsUser = criteriaBuilder.equal(root.get("sender").get("userId"), userId);
                Predicate receiverIsUser = criteriaBuilder.equal(root.get("receiver").get("userId"), userId);

                Predicate userIsSenderOrReceiver = criteriaBuilder.or(senderIsUser, receiverIsUser);

                finalPredicates.add(userIsSenderOrReceiver);
            }

            if (startDate != null) {
                finalPredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }

            if (endDate != null) {
                finalPredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            return criteriaBuilder.and(finalPredicates.toArray(new Predicate[0]));
        };
    }
}
