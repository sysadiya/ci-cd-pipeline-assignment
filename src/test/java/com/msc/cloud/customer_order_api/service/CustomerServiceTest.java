package com.msc.cloud.customer_order_api.service;

import com.msc.cloud.customer_order_api.dto.CustomerDTO;
import com.msc.cloud.customer_order_api.entity.Customer;
import com.msc.cloud.customer_order_api.exception.ResourceNotFoundException;
import com.msc.cloud.customer_order_api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // ===================== save =====================
    // Example 2 – AI-generated unit test for CustomerService.save(), later modified
    // to correctly mock repository behavior and verify DTO-to-entity conversion.
    /**
     * ========================================================================
     * THIS TEST WAS MODIFIED FROM AI OUTPUT
     * ========================================================================
     *
     * ORIGINAL AI ERROR:
     *   when(customerRepository.save(inputDTO)).thenReturn(savedEntity);
     *
     * CORRECTED VERSION:
     *   when(customerRepository.save(any(Customer.class))).thenReturn(savedEntity);
     *
     * ADDED BY HUMAN:
     *   ArgumentCaptor to verify the actual data being saved
     * ========================================================================
     */

    @Test
    void save_shouldSaveAndReturnCustomerDTO() {
        // Arrange
        CustomerDTO inputDTO = new CustomerDTO();
        inputDTO.setName("Alice");
        inputDTO.setEmail("alice@example.com");

        Customer savedEntity = new Customer();
        savedEntity.setId(1L);
        savedEntity.setName("Alice");
        savedEntity.setEmail("alice@example.com");

        // CORRECTED: Use any(Customer.class) because service creates new entity
        when(customerRepository.save(any(Customer.class))).thenReturn(savedEntity);

        // Act
        CustomerDTO result = customerService.save(inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());

        // ADDED BY HUMAN: ArgumentCaptor to verify what was actually saved
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals("Alice", captor.getValue().getName());
        assertEquals("alice@example.com", captor.getValue().getEmail());
    }

    // ===================== getById =====================

    @Test
    void getById_shouldReturnCustomerDTO_whenCustomerExists() {
        // Arrange
        Customer entity = new Customer();
        entity.setId(5L);
        entity.setName("Bob");
        entity.setEmail("bob@example.com");

        when(customerRepository.findById(5L)).thenReturn(Optional.of(entity));

        // Act
        CustomerDTO result = customerService.getById(5L);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Bob", result.getName());
        assertEquals("bob@example.com", result.getEmail());
        verify(customerRepository).findById(5L);
    }

    @Test
    void getById_shouldThrowException_whenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getById(99L)
        );

        assertEquals("Customer not found with id: 99", exception.getMessage());
        verify(customerRepository).findById(99L);
    }

    // ===================== getAll =====================

    @Test
    void getAll_shouldReturnListOfCustomerDTOs() {
        // Arrange
        Customer first = new Customer();
        first.setId(1L);
        first.setName("Carol");
        first.setEmail("carol@example.com");

        Customer second = new Customer();
        second.setId(2L);
        second.setName("Dave");
        second.setEmail("dave@example.com");

        when(customerRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<CustomerDTO> result = customerService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Carol", result.get(0).getName());
        assertEquals("Dave", result.get(1).getName());
        verify(customerRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoCustomersExist() {
        // Arrange
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CustomerDTO> result = customerService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(customerRepository).findAll();
    }

    // ===================== delete =====================

    @Test
    void delete_shouldRemoveCustomer_whenCustomerExists() {
        // Arrange
        Customer entity = new Customer();
        entity.setId(10L);
        entity.setName("Eve");
        entity.setEmail("eve@example.com");

        when(customerRepository.findById(10L)).thenReturn(Optional.of(entity));

        // Act
        customerService.delete(10L);

        // Assert
        verify(customerRepository).findById(10L);
        verify(customerRepository).delete(entity);
    }

    @Test
    void delete_shouldThrowException_whenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(77L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.delete(77L)
        );

        assertEquals("Customer not found with id: 77", exception.getMessage());
        verify(customerRepository).findById(77L);
        verify(customerRepository, never()).delete(any(Customer.class));
    }



}

