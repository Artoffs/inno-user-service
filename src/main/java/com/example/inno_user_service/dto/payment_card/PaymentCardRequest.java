package com.example.inno_user_service.dto.payment_card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String number;

    @NotBlank
    private String holder;

    private LocalDate expirationDate;

    private Boolean active;
}