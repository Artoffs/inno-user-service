package com.example.inno_user_service.specifications;

import com.example.inno_user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecification {

    public static Specification<User> firstNameContains(String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }


            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + firstName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> surnameContains(String surname) {
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }
}
