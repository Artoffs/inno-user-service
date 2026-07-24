package com.example.inno_user_service.mapper;

import com.example.inno_user_service.dto.user.UserWithCardsResponse;
import com.example.inno_user_service.dto.user.UserRequest;
import com.example.inno_user_service.dto.user.UserResponse;
import com.example.inno_user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    UserResponse toResponse(User user);

    @Mapping(target = "cards", source = "paymentCards")
    UserWithCardsResponse toResponseWithCards(User user);

    List<UserResponse> toResponseList(List<User> users);

    User toEntity(UserRequest request);

}
