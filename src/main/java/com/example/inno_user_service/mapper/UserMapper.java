package com.example.inno_user_service.mapper;

import com.example.inno_user_service.dto.user.UserRequest;
import com.example.inno_user_service.dto.user.UserResponse;
import com.example.inno_user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    User toEntity(UserRequest request);
}
