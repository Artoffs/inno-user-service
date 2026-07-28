package com.example.inno_user_service.service;

import com.example.inno_user_service.dao.UserDao;
import com.example.inno_user_service.dto.user.UserRequest;
import com.example.inno_user_service.dto.user.UserResponse;
import com.example.inno_user_service.dto.user.UserWithCardsResponse;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.exception.EmailAlreadyExistsException;
import com.example.inno_user_service.exception.ResourceNotFoundException;
import com.example.inno_user_service.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper mapper;


    public Page<UserResponse> findAll(Pageable pageable) {
        return userDao.findAll(pageable).map(mapper::toResponse);
    }

    public Page<UserResponse> findAll(Pageable pageable, Specification<User> specification) {
        return userDao.findAll(specification, pageable).map(mapper::toResponse);
    }


    @Cacheable(value = "users", key = "#id")
    public UserWithCardsResponse findByIdOrThrow(Long id) {
        Optional<User> byId = userDao.findById(id);
        return byId.map(mapper::toResponseWithCards).orElseThrow(() ->
                new ResourceNotFoundException("User", id));
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userDao.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("User with this email already exists");
        }

        User entity = mapper.toEntity(request);
        User save = userDao.save(entity);
        return mapper.toResponse(save);
    }

    @Transactional
    @CachePut(value = "users", key = "#result.id")
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userDao.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User", id));

        if (!user.getEmail().equals(request.getEmail())) {
            if (userDao.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("User with this email already exists");
            }
            user.setEmail(request.getEmail());
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setBirthDate(request.getBirthDate());
        user.setActive(request.getIsActive());

        return mapper.toResponse(user);
    }

    @CacheEvict(value = "users", key = "#id", beforeInvocation = true)
    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    @CachePut(value = "users", key = "#id")
    public UserResponse deactivateUser(Long id) {
        User user = userDao.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));



        user.setActive(false);
        return mapper.toResponse(user);
    }

    @CachePut(value = "users", key = "#id")
    public UserResponse activateUser(Long id) {
        User user = userDao.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Пользователь", id));

        user.setActive(true);
        return mapper.toResponse(user);
    }
}
