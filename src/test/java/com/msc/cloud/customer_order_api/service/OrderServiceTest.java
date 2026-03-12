package com.msc.cloud.customer_order_api.service;

import com.msc.cloud.customer_order_api.dto.OrderDTO;
import com.msc.cloud.customer_order_api.entity.Order;
import com.msc.cloud.customer_order_api.exception.ResourceNotFoundException;
import com.msc.cloud.customer_order_api.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ============================================================================
 * EXAMPLE 5 – AI OUTPUT CONSTRAINED BY EXPLICIT RULES
 * ============================================================================

  * PROMPT USED:
 * "Generate a unit test for OrderService but do NOT use @SpringBootTest
 *  and do NOT load the Spring context."
 *
 * CONSTRAINTS APPLIED:
 * 1. NO @SpringBootTest annotation
 * 2. NO Spring context loading
 * 3. Use pure Mockito for mocking dependencies

 *  This test demonstrates that explicit constraints in prompts lead to
 * better, more efficient test code that aligns with testing best practices.
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)  // ← KEY: Uses Mockito extension, NOT Spring
class OrderServiceTest {

    @Mock  // ← KEY: Mocked dependency, no real database connection
    private OrderRepository orderRepository;

    @InjectMocks  // ← KEY: Service under test with injected mocks
    private OrderService orderService;

    // ===================== save =====================

    @Test
    void save_shouldSaveAndReturnOrderDTO() {
        // Arrange
        Order inputOrder = new Order();
        inputOrder.setAmount(150.75);
        inputOrder.setOrderDate(LocalDate.of(2025, 6, 15));

        Order savedEntity = new Order();
        savedEntity.setId(1L);
        savedEntity.setAmount(150.75);
        savedEntity.setOrderDate(LocalDate.of(2025, 6, 15));

        when(orderRepository.save(inputOrder)).thenReturn(savedEntity);

        // Act
        OrderDTO result = orderService.save(inputOrder);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(150.75, result.getAmount());
        assertEquals(LocalDate.of(2025, 6, 15), result.getOrderDate());
        verify(orderRepository).save(inputOrder);
    }

    // ===================== getById =====================

    @Test
    void getById_shouldReturnOrderDTO_whenOrderExists() {
        // Arrange
        Order entity = new Order();
        entity.setId(5L);
        entity.setAmount(99.99);
        entity.setOrderDate(LocalDate.of(2025, 3, 10));

        when(orderRepository.findById(5L)).thenReturn(Optional.of(entity));

        // Act
        OrderDTO result = orderService.getById(5L);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(99.99, result.getAmount());
        assertEquals(LocalDate.of(2025, 3, 10), result.getOrderDate());
        verify(orderRepository).findById(5L);
    }

    @Test
    void getById_shouldThrowException_whenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getById(99L)
        );

        assertEquals("Order not found with id: 99", exception.getMessage());
        verify(orderRepository).findById(99L);
    }

    // ===================== getAll =====================

    @Test
    void getAll_shouldReturnListOfOrderDTOs() {
        // Arrange
        Order first = new Order();
        first.setId(1L);
        first.setAmount(50.00);
        first.setOrderDate(LocalDate.of(2025, 1, 5));

        Order second = new Order();
        second.setId(2L);
        second.setAmount(75.50);
        second.setOrderDate(LocalDate.of(2025, 2, 10));

        when(orderRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<OrderDTO> result = orderService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(50.00, result.get(0).getAmount());
        assertEquals(2L, result.get(1).getId());
        assertEquals(75.50, result.get(1).getAmount());
        verify(orderRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoOrdersExist() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<OrderDTO> result = orderService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAll();
    }

    // ===================== getAllWithPagination =====================

    @Test
    void getAllWithPagination_shouldReturnPageOfOrderDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        Order first = new Order();
        first.setId(10L);
        first.setAmount(200.00);
        first.setOrderDate(LocalDate.of(2025, 4, 1));

        Order second = new Order();
        second.setId(11L);
        second.setAmount(250.00);
        second.setOrderDate(LocalDate.of(2025, 4, 2));

        Page<Order> orderPage = new PageImpl<>(List.of(first, second), pageable, 2);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // Act
        Page<OrderDTO> result = orderService.getAllWithPagination(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals(200.00, result.getContent().get(0).getAmount());
        assertEquals(11L, result.getContent().get(1).getId());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getAllWithPagination_shouldReturnEmptyPage_whenNoOrdersExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<OrderDTO> result = orderService.getAllWithPagination(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(orderRepository).findAll(pageable);
    }

    // ===================== getOrdersBetweenDates =====================

    @Test
    void getOrdersBetweenDates_shouldReturnPageOfOrderDTOs() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 30);
        Pageable pageable = PageRequest.of(0, 10);

        Order first = new Order();
        first.setId(20L);
        first.setAmount(300.00);
        first.setOrderDate(LocalDate.of(2025, 2, 15));

        Order second = new Order();
        second.setId(21L);
        second.setAmount(400.00);
        second.setOrderDate(LocalDate.of(2025, 5, 20));

        Page<Order> orderPage = new PageImpl<>(List.of(first, second), pageable, 2);
        when(orderRepository.findByOrderDateBetween(start, end, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderDTO> result = orderService.getOrdersBetweenDates(start, end, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(20L, result.getContent().get(0).getId());
        assertEquals(LocalDate.of(2025, 2, 15), result.getContent().get(0).getOrderDate());
        assertEquals(21L, result.getContent().get(1).getId());
        assertEquals(LocalDate.of(2025, 5, 20), result.getContent().get(1).getOrderDate());
        verify(orderRepository).findByOrderDateBetween(start, end, pageable);
    }

    @Test
    void getOrdersBetweenDates_shouldReturnEmptyPage_whenNoOrdersInRange() {
        // Arrange
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.of(2020, 12, 31);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(orderRepository.findByOrderDateBetween(start, end, pageable)).thenReturn(emptyPage);

        // Act
        Page<OrderDTO> result = orderService.getOrdersBetweenDates(start, end, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(orderRepository).findByOrderDateBetween(start, end, pageable);
    }

    // ===================== delete =====================

    @Test
    void delete_shouldRemoveOrder_whenOrderExists() {
        // Arrange
        Order entity = new Order();
        entity.setId(30L);
        entity.setAmount(500.00);
        entity.setOrderDate(LocalDate.of(2025, 7, 1));

        when(orderRepository.findById(30L)).thenReturn(Optional.of(entity));

        // Act
        orderService.delete(30L);

        // Assert
        verify(orderRepository).findById(30L);
        verify(orderRepository).delete(entity);
    }

    @Test
    void delete_shouldThrowException_whenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(88L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.delete(88L)
        );

        assertEquals("Order not found with id: 88", exception.getMessage());
        verify(orderRepository).findById(88L);
        verify(orderRepository, never()).delete(any(Order.class));
    }
}

