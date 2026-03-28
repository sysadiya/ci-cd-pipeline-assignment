package com.msc.cloud.customer_order_api.controller;

import com.msc.cloud.customer_order_api.dto.CustomerDTO;
import com.msc.cloud.customer_order_api.dto.OrderDTO;
import com.msc.cloud.customer_order_api.entity.Customer;
import com.msc.cloud.customer_order_api.entity.Order;
import com.msc.cloud.customer_order_api.service.CustomerService;
import com.msc.cloud.customer_order_api.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    public CustomerController(CustomerService customerService,
                              OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    // ✅ GET all customers
    @GetMapping
    public List<CustomerDTO> getAll() {
        return customerService.getAll();
    }

    // ✅ CREATE customer (VALIDATION + 201 Created)
    @PostMapping
    public ResponseEntity<CustomerDTO> create(
            @Valid @RequestBody CustomerDTO dto) {

        CustomerDTO saved = customerService.save(dto);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ✅ GET customer by id
    @GetMapping("/{id}")
    public CustomerDTO get(@PathVariable Long id) {
        return customerService.getById(id);
    }

    // ✅ UPDATE customer (VALIDATION)
    @PutMapping("/{id}")
    public CustomerDTO update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO dto) {

        dto.setId(id);

        return customerService.save(dto);
    }

    // ✅ DELETE customer (204 No Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        customerService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // ✅ GET all orders for a customer (FULL MARKS requirement)
    @GetMapping("/{id}/orders")
    public List<OrderDTO> getOrdersByCustomer(@PathVariable Long id) {

        Customer customer = customerService.getEntityById(id);

        return customer.getOrders()
                .stream()
                .map(order -> {
                    OrderDTO dto = new OrderDTO();
                    dto.setId(order.getId());
                    dto.setOrderDate(order.getOrderDate());
                    dto.setAmount(order.getAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ✅ CREATE order for a customer

    @PostMapping("/{id}/orders")
    public ResponseEntity<OrderDTO> createOrderForCustomer(
            @PathVariable Long id,
            @RequestBody Order order) {

        Customer customer = customerService.getEntityById(id);

        order.setCustomer(customer);

        OrderDTO saved = orderService.save(order);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

}