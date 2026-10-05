package org.shopwave.orderservice.repositories;


import org.shopwave.orderservice.entities.Order;
import org.shopwave.orderservice.entities.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByCustomerIdAndId(UUID customerId, UUID orderId);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status NOT IN ('DELIVERED', 'CANCELLED', 'REFUNDED')")
    Page<Order> findActiveOrders(@Param("customerId") UUID customerId, Pageable pageable);

    long countByCustomerIdAndStatus(UUID customerId, OrderStatus status);
}