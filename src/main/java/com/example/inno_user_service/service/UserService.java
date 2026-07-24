package com.example.inno_user_service.service;

import com.example.inno_user_service.dao.UserDao;
import com.example.inno_user_service.dto.user.UserRequest;
import com.example.inno_user_service.dto.user.UserResponse;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.exceptions.ResourceNotFoundException;
import com.example.inno_user_service.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserDao userDao;
    private final UserMapper mapper;

    public Page<UserResponse> findAll(Pageable pageable) {
        return userDao.findAll(pageable).map(mapper::toResponse);
    }

    public Page<UserResponse> findAll(Pageable pageable, Specification<User> specification) {
        return userDao.findAll(specification, pageable).map(mapper::toResponse);
    }

    public Optional<User> findById(Long id) {
        return userDao.findById(id);
    }

    public UserResponse findByIdOrThrow(Long id) {
        Optional<User> byId = findById(id);
        return byId.map(mapper::toResponse).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));
    }

    public UserResponse createUser(UserRequest request) {
        User entity = mapper.toEntity(request);
        User save = userDao.save(entity);
        return mapper.toResponse(save);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));

        if (!user.getEmail().equals(request.getEmail())) {
            if (userDao.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Ты лох");
            }
            user.setEmail(request.getEmail());
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setBirthDate(request.getBirthDate());
        user.setActive(request.getIsActive());

        return mapper.toResponse(user);
    }

    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    public UserResponse deactivateUser(Long id) {
        User user = findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));

        user.setActive(false);
        return mapper.toResponse(user);
    }

    public UserResponse activateUser(Long id) {
        User user = findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));

        user.setActive(true);
        return mapper.toResponse(user);
    }
}
