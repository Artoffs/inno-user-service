package com.example.inno_user_service.dao;

import com.example.inno_user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserDao extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    @Modifying
    @Query("UPDATE User u SET u.active = true WHERE u.id = :id")
    int activateUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :id")
    int deactivateUser(@Param("id") Long id);

}
