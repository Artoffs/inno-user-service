package com.example.inno_user_service.dao;

import com.example.inno_user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Long> {
}
