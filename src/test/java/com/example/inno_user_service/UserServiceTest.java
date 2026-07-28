package com.example.inno_user_service;

import com.example.inno_user_service.dao.UserDao;
import com.example.inno_user_service.dto.user.UserRequest;
import com.example.inno_user_service.dto.user.UserResponse;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.exception.ResourceNotFoundException;
import com.example.inno_user_service.mapper.UserMapper;
import com.example.inno_user_service.exception.EmailAlreadyExistsException;
import com.example.inno_user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldReturnUserResponse() {
        UserRequest request = new UserRequest();
        request.setEmail("test@mail.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@mail.com");

        when(mapper.toEntity(any())).thenReturn(user);
        when(userDao.save(any())).thenReturn(user);
        when(mapper.toResponse(any())).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@mail.com");
        verify(userDao, times(1)).save(any());
    }


    @Test
    void findByIdOrThrow_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        User result = userDao.findById(1L).orElse(null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdOrThrow_WhenUserNotFound_ShouldThrowException() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findByIdOrThrow(999L);
        });
    }

    @Test
    void findById_ShouldReturnEmptyOptional() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThat(userDao.findById(1L)).isEmpty();
        assertThat(userDao.findById(1L)).isNotPresent();
    }

    @Test
    void findById_ShouldReturnOptionalWithUser() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userDao.findById(1L)).isPresent();
        assertThat(userDao.findById(1L).get().getId()).isEqualTo(1L);
    }


    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@mail.com");

        UserRequest request = new UserRequest();
        request.setName("Новое имя");
        request.setEmail("old@mail.com"); // email не меняется

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("Новое имя");

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(mapper.toResponse(any())).thenReturn(response);

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новое имя");
    }

    @Test
    void updateUser_ShouldThrowResourceNotFound() {

        when(userDao.findById(999L)).thenReturn(Optional.empty());
        UserRequest request = new UserRequest();

        assertThrows(ResourceNotFoundException.class,() -> userService.updateUser(999L, request));
    }

    @Test
    void updateUser_ShouldUpdateAndThrow() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@mail.com");

        UserRequest request = new UserRequest();
        request.setName("Новое имя");
        request.setEmail("old1@mail.com");

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("Новое имя");

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.updateUser(existingUser.getId(), request);
        });
    }

    @Test
    void updateUser_ShouldUpdateSuccessful() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@mail.com");
        existingUser.setName("Старое имя");

        UserRequest request = new UserRequest();
        request.setName("Новое имя");
        request.setEmail("new@mail.com");
        request.setIsActive(true);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("Новое имя");
        response.setEmail("new@mail.com");
        response.setActive(true);

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.existsByEmail("new@mail.com")).thenReturn(false);
        when(mapper.toResponse(any(User.class))).thenReturn(response);

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Новое имя");
        assertThat(result.getEmail()).isEqualTo("new@mail.com");
        assertThat(result.getActive()).isTrue();

        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).existsByEmail("new@mail.com");
        verify(mapper, times(1)).toResponse(any(User.class));
    }

    @Test
    void deleteUser_ShouldCallDaoDelete() {
        userService.deleteUser(1L);

        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    void activateUser_ShouldSetActiveTrue() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setActive(true);

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(any())).thenReturn(response);

        UserResponse result = userService.activateUser(1L);

        assertThat(result.getActive()).isTrue();
    }

    @Test
    void activateUser_ShouldThrowResourceNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.activateUser(1L));
    }

    @Test
    void deactivateUser_ShouldSetActiveFalse() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setActive(false);

        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(any())).thenReturn(response);

        UserResponse result = userService.deactivateUser(1L);

        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deactivateUser_ShouldThrowResourceNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.deactivateUser(1L));
    }
}
