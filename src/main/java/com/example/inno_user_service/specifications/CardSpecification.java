package com.example.inno_user_service.specifications;


import com.example.inno_user_service.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;


@Component
public class CardSpecification {
    public static Specification<PaymentCard> holderContains(String holder) {

        return (root, query, criteriaBuilder) -> {

            if (holder == null || holder.isEmpty()) {
                return criteriaBuilder.conjunction();
            }


            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("holder")),
                    "%" + holder.toLowerCase() + "%"
            );
        };
    }
}
