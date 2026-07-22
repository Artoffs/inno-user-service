package com.example.inno_user_service.service;

import com.example.inno_user_service.dao.PaymentCardDao;
import com.example.inno_user_service.dto.payment_card.PaymentCardRequest;
import com.example.inno_user_service.dto.payment_card.PaymentCardResponse;
import com.example.inno_user_service.entity.PaymentCard;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.exceptions.ResourceNotFoundException;
import com.example.inno_user_service.mapper.PaymentCardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCardService {

    private final PaymentCardDao paymentCardDao;
    private final UserService userService;
    private final PaymentCardMapper mapper;

    public Optional<PaymentCard> findById(Long id) {
        return paymentCardDao.findById(id);
    }

    public PaymentCardResponse findByIdOrThrow(Long id) {
        Optional<PaymentCard> byId = findById(id);
        return byId.map(mapper::toResponse).orElseThrow(() ->
                new ResourceNotFoundException("Продукт", id));
    }

    public PaymentCardResponse createPaymentCard(PaymentCardRequest request) {
        User user = userService.findById(request.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("Пользователь", request.getUserId())
        );

        PaymentCard entity = mapper.toEntity(request, user);
        user.getPaymentCards().add(entity);

        PaymentCard save = paymentCardDao.save(entity);

        return mapper.toResponse(save);
    }

    public PaymentCardResponse updateCard(Long id, PaymentCardRequest request) {

        PaymentCard paymentCard = findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Карта", id));

        if (!paymentCard.getUser().getId().equals(request.getUserId())) {
            User user = userService.findById(request.getUserId()).orElseThrow(() ->
                    new ResourceNotFoundException("Пользователь", request.getUserId()));
            paymentCard.setUser(user);
        }

        paymentCard.setNumber(request.getNumber());
        paymentCard.setHolder(request.getHolder());
        paymentCard.setExpirationDate(request.getExpirationDate());
        paymentCard.setActive(request.getActive());

        return mapper.toResponse(paymentCard);
    }

    public void deletePaymentCard(Long id) {
        paymentCardDao.deleteById(id);
    }
}
