package com.example.inno_user_service.entity;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(name = "surname", length = 100)
    @ToString.Include
    private String surname;

    @Column(name = "birth_date")
    @ToString.Include
    private LocalDate birthDate;

    @Column(name = "email", nullable = false, unique = true)
    @ToString.Include
    private String email;

    @Column(name = "active")
    @ToString.Include
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    @ToString.Include
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @ToString.Include
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentCard> paymentCards = new ArrayList<>();

    public void addPaymentCard(PaymentCard card) {
        paymentCards.add(card);
        card.setUser(this);
    }

    public void removePaymentCard(PaymentCard card) {
        paymentCards.remove(card);
        card.setUser(null);
    }
}
