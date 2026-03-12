package com.msc.cloud.customer_order_api.controller;

import com.msc.cloud.customer_order_api.dto.OrderDTO;
import com.msc.cloud.customer_order_api.entity.Order;
import com.msc.cloud.customer_order_api.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // CREATE order
    @PostMapping
    public ResponseEntity<OrderDTO> create(@Valid @RequestBody Order order) {

        OrderDTO saved = orderService.save(order);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // GET all orders (without pagination)
    @GetMapping("/all")
    public List<OrderDTO> getAll() {

        return orderService.getAll();
    }

    // GET orders with pagination
    @GetMapping
    public Page<OrderDTO> getAllWithPagination(Pageable pageable) {

        return orderService.getAllWithPagination(pageable);
    }

    // GET order by id
    @GetMapping("/{id}")
    public OrderDTO getById(@PathVariable Long id) {

        return orderService.getById(id);
    }

    // DELETE order
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        orderService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // DATE RANGE filter
    @GetMapping("/date-range")
    public Page<OrderDTO> getOrdersBetweenDates(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            Pageable pageable) {

        return orderService.getOrdersBetweenDates(start, end, pageable);
    }
}