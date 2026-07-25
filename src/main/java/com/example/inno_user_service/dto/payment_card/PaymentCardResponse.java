package com.example.inno_user_service.dto.payment_card;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCardResponse {

    private Long id;

    private Long userId;

    private String number;

    private String holder;

    private LocalDate expirationDate;

    private Boolean active = true;

    private LocalDateTime createdAt;

}
