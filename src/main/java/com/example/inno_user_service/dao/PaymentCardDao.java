package com.example.inno_user_service.dao;

import com.example.inno_user_service.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardDao extends JpaRepository<PaymentCard, Long>,
        JpaSpecificationExecutor<PaymentCardDao> {

    List<PaymentCard> findByUserId(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE PaymentCard p SET p.active = true WHERE p.id = :id")
    int activateCard(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PaymentCard p SET p.active = false WHERE p.id = :id")
    int deactivateCard(@Param("id") Long id);

}
