package com.example.inno_user_service.dao;

import com.example.inno_user_service.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCardDao extends JpaRepository<PaymentCard, Long> {
}
