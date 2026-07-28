package com.example.inno_user_service.dto.payment_card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(min = 20, max = 20)
    @NotBlank
    private String number;

    @NotBlank
    private String holder;

    @Future
    private LocalDate expirationDate;

    @NotNull
    private Boolean active;
}