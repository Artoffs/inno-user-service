package com.example.inno_user_service.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String name, Long id) {
        super(name + " с id=" + id + " не найден");
    }
}
