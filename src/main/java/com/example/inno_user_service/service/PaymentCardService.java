package com.example.inno_user_service.service;

import com.example.inno_user_service.dao.PaymentCardDao;
import com.example.inno_user_service.dto.payment_card.PaymentCardRequest;
import com.example.inno_user_service.dto.payment_card.PaymentCardResponse;
import com.example.inno_user_service.entity.PaymentCard;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.exceptions.MaxCardAmountException;
import com.example.inno_user_service.exceptions.ResourceNotFoundException;
import com.example.inno_user_service.mapper.PaymentCardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCardService {

    private final PaymentCardDao paymentCardDao;
    private final UserService userService;
    private final PaymentCardMapper mapper;

    public Page<PaymentCardResponse> findAll(Pageable pageable) {
        return paymentCardDao.findAll(pageable).map(mapper::toResponse);
    }

    public Page<PaymentCardResponse> findAll(Pageable pageable, Specification<PaymentCard> specification) {
        return paymentCardDao.findAll(specification, pageable).map(mapper::toResponse);
    }

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

        if (paymentCardDao.countByUserId(user.getId()) >= 5) {
            throw new MaxCardAmountException(user.getId());
        }

        PaymentCard entity = mapper.toEntity(request, user);

        PaymentCard save = paymentCardDao.save(entity);

        return mapper.toResponse(save);
    }

    public PaymentCardResponse updatePaymentCard(Long id, PaymentCardRequest request) {

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

    public List<PaymentCardResponse> findAllByUserId(Long id) {
        return paymentCardDao.findByUserId(id)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PaymentCardResponse deactivatePaymentCard(Long id) {
        PaymentCard paymentCard = findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));

        paymentCard.setActive(false);
        return mapper.toResponse(paymentCard);
    }

    public PaymentCardResponse activatePaymentCard(Long id) {
        PaymentCard paymentCard = findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));

        paymentCard.setActive(true);
        return mapper.toResponse(paymentCard);
    }
}
