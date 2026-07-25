package com.example.inno_user_service;

import com.example.inno_user_service.dao.PaymentCardDao;
import com.example.inno_user_service.dto.payment_card.PaymentCardRequest;
import com.example.inno_user_service.dto.payment_card.PaymentCardResponse;
import com.example.inno_user_service.dto.user.UserResponse;
import com.example.inno_user_service.entity.PaymentCard;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.exceptions.MaxCardAmountException;
import com.example.inno_user_service.exceptions.ResourceNotFoundException;
import com.example.inno_user_service.mapper.PaymentCardMapper;
import com.example.inno_user_service.service.PaymentCardService;
import com.example.inno_user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.smartcardio.Card;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceTest {

    @Mock
    private PaymentCardDao cardDao;

    @Mock
    private PaymentCardMapper mapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentCardService cardService;

    @Test
    void createPaymentCard_shouldReturnPaymentCardResponse() {

        User user = new User();
        user.setId(1L);

        PaymentCardRequest request = new PaymentCardRequest();
        request.setNumber("123456789");
        request.setUserId(1L);

        PaymentCard card = new PaymentCard();
        card.setId(1L);

        PaymentCardResponse response = new PaymentCardResponse();
        response.setNumber("123456789");
        response.setUserId(1L);

        when(mapper.toEntity(any(), any())).thenReturn(card);
        when(cardDao.save(any())).thenReturn(card);
        when(mapper.toResponse(any())).thenReturn(response);
        when(userService.findById(request.getUserId())).thenReturn(Optional.of(user));

        PaymentCardResponse paymentCard = cardService.createPaymentCard(request);


        assertThat(paymentCard).isNotNull();
        assertThat(paymentCard.getNumber()).isEqualTo("123456789");
    }

    @Test
    void createPaymentCard_shouldThrowResourceNotFound() {

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(999L);


        when(userService.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.createPaymentCard(request));
    }

    @Test
    void createPaymentCard_shouldThrowMaxCardAmountException() {

        User user = new User();
        user.setId(1L);

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(cardDao.countByUserId(1L)).thenReturn(5L);

        assertThrows(MaxCardAmountException.class, () -> cardService.createPaymentCard(request));
    }

    @Test
    void deleteCard_ShouldCallDaoDelete() {
        cardService.deletePaymentCard(1L);

        verify(cardDao, times(1)).deleteById(1L);
    }

    @Test
    void activateCard_ShouldSetActiveTrue() {
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        card.setActive(false);

        PaymentCardResponse response = new PaymentCardResponse();
        response.setActive(true);

        when(cardDao.findById(1L)).thenReturn(Optional.of(card));
        when(mapper.toResponse(any())).thenReturn(response);

        PaymentCardResponse result = cardService.activatePaymentCard(1L);

        assertThat(result.getActive()).isTrue();
    }

    @Test
    void activateUser_ShouldThrowResourceNotFound() {
        when(cardDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.activatePaymentCard(1L));
    }

    @Test
    void deactivateCard_ShouldSetActiveFalse() {
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        card.setActive(true);

        PaymentCardResponse response = new PaymentCardResponse();
        response.setActive(false);

        when(cardDao.findById(1L)).thenReturn(Optional.of(card));
        when(mapper.toResponse(any())).thenReturn(response);

        PaymentCardResponse result = cardService.deactivatePaymentCard(1L);

        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deactivateCard_ShouldThrowResourceNotFound() {
        when(cardDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.deactivatePaymentCard(1L));
    }

    @Test
    void updateCard_shouldReturnPaymentCardResponse() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setNumber("123456789");
        paymentCard.setHolder("Holder");
        paymentCard.setExpirationDate(LocalDate.of(2027, 12, 12));
        paymentCard.setActive(true);

        User user = new User();
        user.setId(1L);

        paymentCard.setUser(user);

        PaymentCardRequest request = new PaymentCardRequest();
        request.setNumber("987654321");
        request.setUserId(1L);

        PaymentCardResponse response = new PaymentCardResponse();
        response.setNumber("987654321");
        response.setHolder("Holder");
        response.setExpirationDate(LocalDate.of(2027, 12, 12));
        response.setActive(true);

        when(cardDao.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(mapper.toResponse(any())).thenReturn(response);

        PaymentCardResponse result = cardService.updatePaymentCard(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo("987654321");
        assertThat(result.getHolder()).isEqualTo("Holder");
        assertThat(result.getExpirationDate()).isEqualTo(LocalDate.of(2027, 12, 12));
        assertThat(result.getActive()).isEqualTo(true);
    }

    @Test
    void updateCard_shouldThrowCardResourceNotFound() {
        when(cardDao.findById(999L)).thenReturn(Optional.empty());

        PaymentCardRequest request = new PaymentCardRequest();

        assertThrows(ResourceNotFoundException.class, () -> cardService.updatePaymentCard(999L, request));
    }

    @Test
    void updateCard_shouldThrowUserResourceNotFound() {

        PaymentCard paymentCard = new PaymentCard();

        User user = new User();
        user.setId(1L);

        paymentCard.setUser(user);

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(2L);

        when(cardDao.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(userService.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.updatePaymentCard(1L, request));
    }

    @Test
    void findByIdOrThrow_WhenCardExists_ShouldReturnCardResponse() {
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        card.setNumber("123456789");

        PaymentCardResponse response = new PaymentCardResponse();
        response.setId(1L);
        response.setNumber("123456789");

        when(cardDao.findById(1L)).thenReturn(Optional.of(card));
        when(mapper.toResponse(any(PaymentCard.class))).thenReturn(response);

        PaymentCardResponse result = cardService.findByIdOrThrow(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumber()).isEqualTo("123456789");
    }

    @Test
    void findByIdOrThrow_WhenCardNotFound_ShouldThrowResourceNotFoundException() {
        // Подготовка
        when(cardDao.findById(999L)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(ResourceNotFoundException.class, () -> {
            cardService.findByIdOrThrow(999L);
        });
    }

    @Test
    void findAllByUserId_ShouldReturnListOfCardResponses() {
        User user = new User();
        user.setId(1L);

        PaymentCard card1 = new PaymentCard();
        card1.setId(1L);
        card1.setNumber("123456789");
        card1.setUser(user);

        PaymentCard card2 = new PaymentCard();
        card2.setId(2L);
        card2.setNumber("987654321");
        card2.setUser(user);

        PaymentCardResponse response1 = new PaymentCardResponse();
        response1.setId(1L);
        response1.setNumber("123456789");

        PaymentCardResponse response2 = new PaymentCardResponse();
        response2.setId(2L);
        response2.setNumber("987654321");

        when(cardDao.findByUserId(1L)).thenReturn(List.of(card1, card2));
        when(mapper.toResponse(card1)).thenReturn(response1);
        when(mapper.toResponse(card2)).thenReturn(response2);

        List<PaymentCardResponse> result = cardService.findAllByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNumber()).isEqualTo("123456789");
        assertThat(result.get(1).getNumber()).isEqualTo("987654321");
        verify(cardDao, times(1)).findByUserId(1L);
    }

    @Test
    void findAllByUserId_WhenNoCards_ShouldReturnEmptyList() {
        when(cardDao.findByUserId(1L)).thenReturn(List.of());

        List<PaymentCardResponse> result = cardService.findAllByUserId(1L);

        assertThat(result).isEmpty();
        verify(cardDao, times(1)).findByUserId(1L);

    }
}
