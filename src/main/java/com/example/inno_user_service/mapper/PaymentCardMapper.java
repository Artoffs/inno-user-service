package com.example.inno_user_service.mapper;

import com.example.inno_user_service.dto.payment_card.PaymentCardRequest;
import com.example.inno_user_service.dto.payment_card.PaymentCardResponse;
import com.example.inno_user_service.entity.PaymentCard;
import com.example.inno_user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "userId", source = "user.id")
    PaymentCardResponse toResponse(PaymentCard card);

    List<PaymentCardResponse> toResponseList(List<PaymentCard> paymentCards);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "active", source = "request.active")
    PaymentCard toEntity(PaymentCardRequest request, User user);
}
