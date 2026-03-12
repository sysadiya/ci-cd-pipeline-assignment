package com.msc.cloud.customer_order_api.service;

import com.msc.cloud.customer_order_api.dto.OrderDTO;
import com.msc.cloud.customer_order_api.entity.Order;
import com.msc.cloud.customer_order_api.exception.ResourceNotFoundException;
import com.msc.cloud.customer_order_api.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ✅ Convert Entity → DTO
    private OrderDTO convertToDTO(Order order) {

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setAmount(order.getAmount());

        return dto;
    }

    // ✅ CREATE order
    public OrderDTO save(Order order) {

        Order saved = orderRepository.save(order);
        return convertToDTO(saved);
    }

    // ✅ GET all orders
    public List<OrderDTO> getAll() {

        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ GET orders pagination
    public Page<OrderDTO> getAllWithPagination(Pageable pageable) {

        return orderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    // ✅ GET order by ID (DTO version for Controller)
    public OrderDTO getById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + id));

        return convertToDTO(order);
    }

    // ✅ INTERNAL use (Entity version)
    public Order getEntityById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + id));
    }

    // ✅ DELETE order
    public void delete(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + id));

        orderRepository.delete(order);
    }

    // ✅ DATE RANGE filter
    public Page<OrderDTO> getOrdersBetweenDates(
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        return orderRepository
                .findByOrderDateBetween(start, end, pageable)
                .map(this::convertToDTO);
    }
}