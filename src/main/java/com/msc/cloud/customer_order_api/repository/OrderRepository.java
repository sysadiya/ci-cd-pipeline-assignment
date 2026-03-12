
package com.msc.cloud.customer_order_api.repository;

import com.msc.cloud.customer_order_api.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByOrderDateBetween(
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );
}


