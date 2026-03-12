package com.msc.cloud.customer_order_api.integration;

import com.msc.cloud.customer_order_api.dto.OrderDTO;
import com.msc.cloud.customer_order_api.entity.Customer;
import com.msc.cloud.customer_order_api.entity.Order;
import com.msc.cloud.customer_order_api.repository.CustomerRepository;
import com.msc.cloud.customer_order_api.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private String baseUrl;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/orders";

        // Clean up database before each test
        orderRepository.deleteAll();
        customerRepository.deleteAll();

        // Create a test customer for orders
        testCustomer = customerRepository.save(new Customer("Test Customer", "test@example.com"));
    }

    // ===================== GET ALL ORDERS =====================

    @Test
    @DisplayName("GET /api/orders/all - Should return empty list when no orders exist")
    void getAllOrders_ShouldReturnEmptyList_WhenNoOrdersExist() {
        // Act - Use /all endpoint for list response
        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
                baseUrl + "/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OrderDTO>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/orders/all - Should return all orders")
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        Order order1 = new Order();
        order1.setAmount(100.0);
        order1.setOrderDate(LocalDate.of(2026, 3, 1));
        order1.setCustomer(testCustomer);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setAmount(200.0);
        order2.setOrderDate(LocalDate.of(2026, 3, 2));
        order2.setCustomer(testCustomer);
        orderRepository.save(order2);

        // Act - Use /all endpoint for list response
        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
                baseUrl + "/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OrderDTO>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    // ===================== GET ORDER BY ID =====================

    @Test
    @DisplayName("GET /orders/{id} - Should return order when exists")
    void getOrderById_ShouldReturnOrder_WhenExists() {
        // Arrange
        Order order = new Order();
        order.setAmount(150.50);
        order.setOrderDate(LocalDate.of(2026, 3, 15));
        order.setCustomer(testCustomer);
        Order saved = orderRepository.save(order);

        // Act
        ResponseEntity<OrderDTO> response = restTemplate.getForEntity(
                baseUrl + "/" + saved.getId(),
                OrderDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(saved.getId());
        assertThat(response.getBody().getAmount()).isEqualTo(150.50);
    }

    @Test
    @DisplayName("GET /orders/{id} - Should return 404 when order not found")
    void getOrderById_ShouldReturn404_WhenNotFound() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/99999",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===================== CREATE ORDER =====================

    @Test
    @DisplayName("POST /api/customers/{id}/orders - Should create new order for customer")
    void createOrder_ShouldCreateAndReturnOrder() {
        // Arrange - Create order via customer endpoint (proper way in this API)
        Order newOrder = new Order();
        newOrder.setAmount(299.99);
        newOrder.setOrderDate(LocalDate.of(2026, 4, 1));
        // Customer is set by the API endpoint, not by the request body

        String customerOrderUrl = "http://localhost:" + port + "/api/customers/" + testCustomer.getId() + "/orders";

        // Act - Create order through customer endpoint
        ResponseEntity<OrderDTO> response = restTemplate.postForEntity(
                customerOrderUrl,
                newOrder,
                OrderDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(299.99);

        // Verify persisted in database
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    // ===================== DELETE ORDER =====================

    @Test
    @DisplayName("DELETE /orders/{id} - Should delete existing order")
    void deleteOrder_ShouldRemoveOrder() {
        // Arrange
        Order order = new Order();
        order.setAmount(50.0);
        order.setOrderDate(LocalDate.now());
        order.setCustomer(testCustomer);
        Order saved = orderRepository.save(order);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + saved.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(orderRepository.existsById(saved.getId())).isFalse();
    }

    // ===================== PAGINATION TEST =====================

    @Test
    @DisplayName("GET /api/orders - Should return paginated results")
    void getOrdersWithPagination_ShouldReturnPaginatedResults() {
        // Arrange - Create multiple orders
        for (int i = 1; i <= 5; i++) {
            Order order = new Order();
            order.setAmount(i * 10.0);
            order.setOrderDate(LocalDate.of(2026, 3, i));
            order.setCustomer(testCustomer);
            orderRepository.save(order);
        }

        // Act - The default GET /api/orders returns paginated results
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "?page=0&size=2",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
        assertThat(response.getBody()).contains("totalElements");
    }

    // ===================== DATE RANGE FILTER TEST =====================

    @Test
    @DisplayName("GET /api/orders/date-range - Should return orders within date range")
    void getOrdersBetweenDates_ShouldReturnFilteredOrders() {
        // Arrange
        Order order1 = new Order();
        order1.setAmount(100.0);
        order1.setOrderDate(LocalDate.of(2026, 1, 15));
        order1.setCustomer(testCustomer);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setAmount(200.0);
        order2.setOrderDate(LocalDate.of(2026, 6, 15));
        order2.setCustomer(testCustomer);
        orderRepository.save(order2);

        // Act - Query for orders in first half of year
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/date-range?start=2026-01-01&end=2026-03-31&page=0&size=10",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }
}

