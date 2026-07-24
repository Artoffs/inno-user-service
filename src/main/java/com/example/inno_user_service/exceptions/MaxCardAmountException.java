package com.example.inno_user_service.exceptions;

public class MaxCardAmountException extends RuntimeException {
    public MaxCardAmountException(Long id) {
        super("У пользователя с id=" + id + " больше пяти карт");
    }
}
