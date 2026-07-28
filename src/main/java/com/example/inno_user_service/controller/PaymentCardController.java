package com.example.inno_user_service.controller;

import com.example.inno_user_service.dto.payment_card.PaymentCardRequest;
import com.example.inno_user_service.dto.payment_card.PaymentCardResponse;
import com.example.inno_user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cards")
public class PaymentCardController {
    
    public final PaymentCardService paymentCardService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PaymentCardResponse getCard(@PathVariable Long id) {
        return paymentCardService.findByIdOrThrow(id);
    }

    @GetMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentCardResponse> getUserCards(@PathVariable Long id) {
        return paymentCardService.findAllByUserId(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<PaymentCardResponse> getCards(@PageableDefault Pageable pageable) {
        return paymentCardService.findAll(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentCardResponse createCard(@RequestBody @Valid PaymentCardRequest request) {
        return paymentCardService.createPaymentCard(request);
    }

    @PutMapping("/{id}")
    public PaymentCardResponse updateCard(@PathVariable Long id, @RequestBody @Valid PaymentCardRequest request) {
        return paymentCardService.updatePaymentCard(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id) {
        paymentCardService.deletePaymentCard(id);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public PaymentCardResponse deactivateUser(@PathVariable Long id) {
        return paymentCardService.deactivatePaymentCard(id);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public PaymentCardResponse activateUser(@PathVariable Long id) {
        return paymentCardService.activatePaymentCard(id);
    }
}
