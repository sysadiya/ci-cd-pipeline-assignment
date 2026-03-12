package com.msc.cloud.customer_order_api.controller;

import com.msc.cloud.customer_order_api.dto.OrderDTO;
import com.msc.cloud.customer_order_api.entity.Order;
import com.msc.cloud.customer_order_api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.msc.cloud.customer_order_api.controller.OrderController;


@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;


    @Test
    void create_returnsCreatedOrder() {
        // ===================== ARRANGE =====================
        // Create the input Order entity (what the API receives)
        Order order = new Order();
        order.setAmount(125.50);
        order.setOrderDate(LocalDate.of(2025, 5, 20));

        // Create the expected DTO response (what the service returns)
        OrderDTO saved = new OrderDTO();
        saved.setId(1L);
        saved.setAmount(125.50);
        saved.setOrderDate(LocalDate.of(2025, 5, 20));

        // Configure mock behavior - when save() is called, return the DTO
        when(orderService.save(order)).thenReturn(saved);

        // ===================== ACT =====================
        // Call the controller method under test
        ResponseEntity<OrderDTO> response = orderController.create(order);

        // ===================== ASSERT =====================
        // Verify response is not null
        assertNotNull(response);
        // Verify HTTP 201 Created status
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // Verify response body exists
        assertNotNull(response.getBody());
        // Verify response body contains correct data
        assertEquals(1L, response.getBody().getId());
        assertEquals(125.50, response.getBody().getAmount());
        assertEquals(LocalDate.of(2025, 5, 20), response.getBody().getOrderDate());
        // Verify mock interaction occurred
        verify(orderService).save(order);
    }

    @Test
    void getAll_returnsOrders() {
        OrderDTO first = new OrderDTO();
        first.setId(10L);
        first.setAmount(55.00);
        first.setOrderDate(LocalDate.of(2025, 1, 15));

        OrderDTO second = new OrderDTO();
        second.setId(11L);
        second.setAmount(75.25);
        second.setOrderDate(LocalDate.of(2025, 2, 10));

        List<OrderDTO> expected = List.of(first, second);
        when(orderService.getAll()).thenReturn(expected);

        List<OrderDTO> result = orderController.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        verify(orderService).getAll();
    }

    @Test
    void getAllWithPagination_returnsPage() {
        Pageable pageable = PageRequest.of(0, 2);

        OrderDTO first = new OrderDTO();
        first.setId(21L);
        first.setAmount(10.00);
        first.setOrderDate(LocalDate.of(2025, 3, 1));

        OrderDTO second = new OrderDTO();
        second.setId(22L);
        second.setAmount(20.00);
        second.setOrderDate(LocalDate.of(2025, 3, 2));

        Page<OrderDTO> page = new PageImpl<>(List.of(first, second), pageable, 2);
        when(orderService.getAllWithPagination(pageable)).thenReturn(page);

        Page<OrderDTO> result = orderController.getAllWithPagination(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(21L, result.getContent().get(0).getId());
        verify(orderService).getAllWithPagination(pageable);
    }

    @Test
    void getById_returnsOrder() {
        OrderDTO expected = new OrderDTO();
        expected.setId(99L);
        expected.setAmount(199.99);
        expected.setOrderDate(LocalDate.of(2025, 6, 30));

        when(orderService.getById(99L)).thenReturn(expected);

        OrderDTO result = orderController.getById(99L);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(199.99, result.getAmount());
        assertEquals(LocalDate.of(2025, 6, 30), result.getOrderDate());
        verify(orderService).getById(99L);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = orderController.delete(7L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderService).delete(7L);
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() {
        // Arrange
        Long orderId = 99L;

        when(orderService.getById(orderId))
                .thenThrow(new RuntimeException("Order not found"));

        // Act & Assert - getById method throws exception when not found
        try {
            orderController.getById(orderId);
        } catch (RuntimeException e) {
            assertEquals("Order not found", e.getMessage());
        }
        verify(orderService).getById(orderId);
    }

    @Test
    void shouldCreateOrderWithValidData() {
        // Arrange
        Order order = new Order();
        order.setAmount(100.0);
        order.setOrderDate(LocalDate.of(2025, 5, 20));

        OrderDTO savedOrder = new OrderDTO();
        savedOrder.setId(1L);
        savedOrder.setAmount(100.0);
        savedOrder.setOrderDate(LocalDate.of(2025, 5, 20));

        when(orderService.save(order)).thenReturn(savedOrder);

        // Act
        ResponseEntity<OrderDTO> response = orderController.create(order);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(orderService).save(order);
    }
}

