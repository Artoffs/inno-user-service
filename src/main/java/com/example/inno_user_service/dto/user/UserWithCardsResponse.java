package com.example.inno_user_service.dto.user;

import com.example.inno_user_service.dto.payment_card.PaymentCardResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithCardsResponse {

    private Long id;

    private String name;

    private String surname;

    private LocalDate birthDate;

    private String email;

    private Boolean active;

    private LocalDateTime createdAt;

    private List<PaymentCardResponse> cards;
}
