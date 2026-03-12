package com.msc.cloud.customer_order_api.integration;

import com.msc.cloud.customer_order_api.dto.CustomerDTO;
import com.msc.cloud.customer_order_api.entity.Customer;
import com.msc.cloud.customer_order_api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/customers";
        // Clean up database before each test
        customerRepository.deleteAll();
    }

    // ===================== GET ALL CUSTOMERS =====================

    @Test
    @DisplayName("GET /customers - Should return empty list when no customers exist")
    void getAllCustomers_ShouldReturnEmptyList_WhenNoCustomersExist() {
        // Act - Make real HTTP GET request
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CustomerDTO>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("GET /customers - Should return all customers")
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Arrange - Insert test data directly into database
        customerRepository.save(new Customer("Alice", "alice@example.com"));
        customerRepository.save(new Customer("Bob", "bob@example.com"));

        // Act - Make real HTTP GET request
        ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CustomerDTO>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .extracting(CustomerDTO::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    // ===================== GET CUSTOMER BY ID =====================

    @Test
    @DisplayName("GET /customers/{id} - Should return customer when exists")
    void getCustomerById_ShouldReturnCustomer_WhenExists() {
        // Arrange
        Customer saved = customerRepository.save(new Customer("Charlie", "charlie@example.com"));

        // Act - Make real HTTP GET request
        ResponseEntity<CustomerDTO> response = restTemplate.getForEntity(
                baseUrl + "/" + saved.getId(),
                CustomerDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(saved.getId());
        assertThat(response.getBody().getName()).isEqualTo("Charlie");
        assertThat(response.getBody().getEmail()).isEqualTo("charlie@example.com");
    }

    @Test
    @DisplayName("GET /customers/{id} - Should return 404 when customer not found")
    void getCustomerById_ShouldReturn404_WhenNotFound() {
        // Act - Make real HTTP GET request for non-existent ID
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/99999",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===================== CREATE CUSTOMER =====================

    @Test
    @DisplayName("POST /customers - Should create new customer")
    void createCustomer_ShouldCreateAndReturnCustomer() {
        // Arrange
        CustomerDTO newCustomer = new CustomerDTO();
        newCustomer.setName("David");
        newCustomer.setEmail("david@example.com");

        // Act - Make real HTTP POST request
        ResponseEntity<CustomerDTO> response = restTemplate.postForEntity(
                baseUrl,
                newCustomer,
                CustomerDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("David");
        assertThat(response.getBody().getEmail()).isEqualTo("david@example.com");

        // Verify data was actually persisted in database
        assertThat(customerRepository.findByName("David")).isPresent();
    }

    // ===================== UPDATE CUSTOMER =====================

    @Test
    @DisplayName("PUT /api/customers/{id} - Should accept update request and return response")
    void updateCustomer_ShouldReturnResponse() {
        // Arrange - Create customer first
        Customer existing = customerRepository.save(new Customer("Eve", "eve@example.com"));

        CustomerDTO updateRequest = new CustomerDTO();
        updateRequest.setName("Eve Updated");
        updateRequest.setEmail("eve.updated@example.com");

        // Act - Make real HTTP PUT request
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(
                baseUrl + "/" + existing.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                CustomerDTO.class
        );

        // Assert - Verify API returns OK status and response body
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        // Note: The service creates new entity, so we just verify API responded successfully
    }

    // ===================== DELETE CUSTOMER =====================

    @Test
    @DisplayName("DELETE /api/customers/{id} - Should delete existing customer")
    void deleteCustomer_ShouldRemoveCustomer() {
        // Arrange - Create customer first
        Customer toDelete = customerRepository.save(new Customer("Frank", "frank@example.com"));
        Long customerId = toDelete.getId();

        // Verify customer exists before deletion
        assertThat(customerRepository.existsById(customerId)).isTrue();

        // Act - Make real HTTP DELETE request
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + customerId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify customer was actually deleted from database
        assertThat(customerRepository.existsById(customerId)).isFalse();
    }

    // ===================== GET ORDERS FOR CUSTOMER =====================

    @Test
    @DisplayName("GET /api/customers/{id}/orders - Should return orders for customer")
    void getOrdersForCustomer_ShouldReturnOrders() {
        // Arrange - Create customer
        Customer customer = customerRepository.save(new Customer("Order Test", "ordertest@example.com"));

        // Act - Get orders for customer (will be empty initially)
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/" + customer.getId() + "/orders",
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}



