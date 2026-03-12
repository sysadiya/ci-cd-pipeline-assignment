package com.msc.cloud.customer_order_api.service;

import com.msc.cloud.customer_order_api.dto.CustomerDTO;
import com.msc.cloud.customer_order_api.entity.Customer;
import com.msc.cloud.customer_order_api.exception.ResourceNotFoundException;
import com.msc.cloud.customer_order_api.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ✅ Convert Entity → DTO
    private CustomerDTO convertToDTO(Customer customer) {

        CustomerDTO dto = new CustomerDTO();

        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());

        if (customer.getOrders() != null)
            dto.setTotalOrders(customer.getOrders().size());
        else
            dto.setTotalOrders(0);

        return dto;
    }

    // ✅ Convert DTO → Entity
    private Customer convertToEntity(CustomerDTO dto) {

        Customer customer = new Customer();

        // DO NOT set ID for new entity unless updating
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());

        return customer;
    }

    // ✅ CREATE customer
    public CustomerDTO save(CustomerDTO dto) {

        Customer customer = convertToEntity(dto);

        Customer saved = customerRepository.save(customer);

        return convertToDTO(saved);
    }

    // ✅ GET all customers
    public List<CustomerDTO> getAll() {

        return customerRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ GET customer by ID (DTO version for Controller)
    public CustomerDTO getById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id));

        return convertToDTO(customer);
    }

    // ✅ INTERNAL use only (Entity version)
    public Customer getEntityById(Long id) {

        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id));
    }

    // ✅ DELETE customer
    public void delete(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id));

        customerRepository.delete(customer);
    }
}
