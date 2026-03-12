package com.msc.cloud.customer_order_api.repository;

import com.msc.cloud.customer_order_api.entity.Customer;
import com.msc.cloud.customer_order_api.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        // Arrange: Create and persist a customer (required due to foreign key constraint)
        customer = new Customer("John Doe", "john.doe@example.com");
        entityManager.persistAndFlush(customer);
    }

    @Test
    @DisplayName("Should return only orders within the specified date range")
    void findByOrderDateBetween_ShouldReturnOrdersWithinDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        Order orderBeforeRange = createOrder(100.0, LocalDate.of(2025, 12, 15));
        Order orderAtStartDate = createOrder(200.0, LocalDate.of(2026, 1, 1));
        Order orderInMiddle = createOrder(300.0, LocalDate.of(2026, 1, 15));
        Order orderAtEndDate = createOrder(400.0, LocalDate.of(2026, 1, 31));
        Order orderAfterRange = createOrder(500.0, LocalDate.of(2026, 2, 10));

        entityManager.persist(orderBeforeRange);
        entityManager.persist(orderAtStartDate);
        entityManager.persist(orderInMiddle);
        entityManager.persist(orderAtEndDate);
        entityManager.persist(orderAfterRange);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Order::getAmount)
                .containsExactlyInAnyOrder(200.0, 300.0, 400.0);
        assertThat(result.getContent())
                .allMatch(order -> !order.getOrderDate().isBefore(startDate) && !order.getOrderDate().isAfter(endDate));
    }

    @Test
    @DisplayName("Should return empty page when no orders exist within date range")
    void findByOrderDateBetween_ShouldReturnEmptyPage_WhenNoOrdersInRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        Order orderOutsideRange1 = createOrder(100.0, LocalDate.of(2026, 1, 15));
        Order orderOutsideRange2 = createOrder(200.0, LocalDate.of(2026, 7, 20));

        entityManager.persist(orderOutsideRange1);
        entityManager.persist(orderOutsideRange2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should return paginated results correctly - first page")
    void findByOrderDateBetween_ShouldReturnFirstPage_WithCorrectPagination() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        // Create 5 orders within the date range
        for (int i = 1; i <= 5; i++) {
            Order order = createOrder(i * 100.0, LocalDate.of(2026, i, 10));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2); // First page, 2 items per page

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isZero();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Should return paginated results correctly - second page")
    void findByOrderDateBetween_ShouldReturnSecondPage_WithCorrectPagination() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        // Create 5 orders within the date range
        for (int i = 1; i <= 5; i++) {
            Order order = createOrder(i * 100.0, LocalDate.of(2026, i, 10));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isFalse();
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should return paginated results correctly - last page")
    void findByOrderDateBetween_ShouldReturnLastPage_WithCorrectPagination() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        // Create 5 orders within the date range
        for (int i = 1; i <= 5; i++) {
            Order order = createOrder(i * 100.0, LocalDate.of(2026, i, 10));
            entityManager.persist(order);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(2, 2); // Third (last) page, 2 items per page

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1); // Only 1 item on last page
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should return sorted results when sort is specified in pageable")
    void findByOrderDateBetween_ShouldReturnSortedResults_WhenSortSpecified() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        Order order1 = createOrder(300.0, LocalDate.of(2026, 3, 10));
        Order order2 = createOrder(100.0, LocalDate.of(2026, 1, 10));
        Order order3 = createOrder(200.0, LocalDate.of(2026, 2, 10));

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "orderDate"));

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getOrderDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(result.getContent().get(1).getOrderDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(result.getContent().get(2).getOrderDate()).isEqualTo(LocalDate.of(2026, 3, 10));
    }

    @Test
    @DisplayName("Should include boundary dates in results (inclusive range)")
    void findByOrderDateBetween_ShouldIncludeBoundaryDates() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 28);

        Order orderAtStart = createOrder(100.0, startDate);
        Order orderAtEnd = createOrder(200.0, endDate);
        Order orderInMiddle = createOrder(150.0, LocalDate.of(2026, 2, 15));

        entityManager.persist(orderAtStart);
        entityManager.persist(orderAtEnd);
        entityManager.persist(orderInMiddle);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Order> result = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Order::getOrderDate)
                .containsExactlyInAnyOrder(startDate, endDate, LocalDate.of(2026, 2, 15));
    }

    /**
     * Helper method to create an Order entity linked to the test customer.
     */
    private Order createOrder(Double amount, LocalDate orderDate) {
        Order order = new Order();
        order.setAmount(amount);
        order.setOrderDate(orderDate);
        order.setCustomer(customer);
        return order;
    }
}

