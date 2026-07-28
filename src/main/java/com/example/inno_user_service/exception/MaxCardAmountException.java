package com.example.inno_user_service.exception;

public class MaxCardAmountException extends RuntimeException {
    public MaxCardAmountException(Long id) {
        super("У пользователя с id=" + id + " больше пяти карт");
    }
}
