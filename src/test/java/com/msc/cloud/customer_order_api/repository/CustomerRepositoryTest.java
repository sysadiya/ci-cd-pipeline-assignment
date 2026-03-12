package com.msc.cloud.customer_order_api.repository;

import com.msc.cloud.customer_order_api.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Example 3 – AI-generated repository test approach reviewed and rejected.
// Replaced with integration tests using a real database to validate JPA behavior.
@DataJpaTest  // CORRECT: Uses real H2 database, not mocks
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;  // CORRECT: Real entity persistence

    @Autowired
    private CustomerRepository customerRepository;  // CORRECT: Real repository, not mocked

    @Test
    @DisplayName("Should find customer by name when customer exists")
    void findByName_ShouldReturnCustomer_WhenCustomerExists() {

        // CORRECT: Persist REAL entity to H2 database (not mocked!)
        Customer customer = new Customer("John Doe", "john.doe@example.com");
        entityManager.persistAndFlush(customer);  // Actually writes to database


        // CORRECT: Call REAL repository method (executes actual SQL)
        Optional<Customer> result = customerRepository.findByName("John Doe");

        // CORRECT: Verify data retrieved from REAL database
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty when customer name does not exist")
    void findByName_ShouldReturnEmpty_WhenCustomerDoesNotExist() {
        // Arrange
        Customer customer = new Customer("Jane Doe", "jane.doe@example.com");
        entityManager.persistAndFlush(customer);

        // Act
        Optional<Customer> result = customerRepository.findByName("Non Existent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all customers with findAll")
    void findAll_ShouldReturnAllCustomers() {
        // Arrange
        Customer customer1 = new Customer("Alice Smith", "alice@example.com");
        Customer customer2 = new Customer("Bob Johnson", "bob@example.com");
        Customer customer3 = new Customer("Charlie Brown", "charlie@example.com");

        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.persist(customer3);
        entityManager.flush();

        // Act
        List<Customer> result = customerRepository.findAll();

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Customer::getName)
                .containsExactlyInAnyOrder("Alice Smith", "Bob Johnson", "Charlie Brown");
    }

    @Test
    @DisplayName("Should return empty list when no customers exist")
    void findAll_ShouldReturnEmptyList_WhenNoCustomersExist() {
        // Arrange - no customers persisted

        // Act
        List<Customer> result = customerRepository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }
    //Example 4 – AI-Identified Gap or Improvement
    // ============================================================================
    // PAGINATION TESTS - ADDED BASED ON AI SUGGESTION (EXAMPLE 4)
    // ============================================================================
    // These tests were added after AI analysis identified a gap in pagination
    // test coverage. The original AI-generated code required corrections to
    // align with the actual repository methods available in the project.
    // ============================================================================


    @Test
    @DisplayName("Should return paginated results - first page")
    void findAll_WithPageable_ShouldReturnFirstPage() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Customer customer = new Customer("Customer " + i, "customer" + i + "@example.com");
            entityManager.persist(customer);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2); // First page, 2 items per page

        // Act
        Page<Customer> result = customerRepository.findAll(pageable);

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
    @DisplayName("Should return paginated results - second page")
    void findAll_WithPageable_ShouldReturnSecondPage() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Customer customer = new Customer("Customer " + i, "customer" + i + "@example.com");
            entityManager.persist(customer);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page

        // Act
        Page<Customer> result = customerRepository.findAll(pageable);

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
    @DisplayName("Should return paginated results - last page with remaining items")
    void findAll_WithPageable_ShouldReturnLastPage() {
        // Arrange
        for (int i = 1; i <= 5; i++) {
            Customer customer = new Customer("Customer " + i, "customer" + i + "@example.com");
            entityManager.persist(customer);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(2, 2); // Third (last) page, 2 items per page

        // Act
        Page<Customer> result = customerRepository.findAll(pageable);

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
    @DisplayName("Should return sorted results when sort is specified")
    void findAll_WithPageable_ShouldReturnSortedResults() {
        // Arrange
        Customer customerC = new Customer("Charlie", "charlie@example.com");
        Customer customerA = new Customer("Alice", "alice@example.com");
        Customer customerB = new Customer("Bob", "bob@example.com");

        entityManager.persist(customerC);
        entityManager.persist(customerA);
        entityManager.persist(customerB);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));

        // Act
        Page<Customer> result = customerRepository.findAll(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Bob");
        assertThat(result.getContent().get(2).getName()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("Should return sorted results in descending order")
    void findAll_WithPageable_ShouldReturnDescendingSortedResults() {
        // Arrange
        Customer customerA = new Customer("Alice", "alice@example.com");
        Customer customerB = new Customer("Bob", "bob@example.com");
        Customer customerC = new Customer("Charlie", "charlie@example.com");

        entityManager.persist(customerA);
        entityManager.persist(customerB);
        entityManager.persist(customerC);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));

        // Act
        Page<Customer> result = customerRepository.findAll(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Charlie");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Bob");
        assertThat(result.getContent().get(2).getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should return empty page when no customers exist")
    void findAll_WithPageable_ShouldReturnEmptyPage_WhenNoCustomersExist() {
        // Arrange - no customers persisted
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Customer> result = customerRepository.findAll(pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("Should find customer by id when customer exists")
    void findById_ShouldReturnCustomer_WhenCustomerExists() {
        // Arrange
        Customer customer = new Customer("Test User", "test@example.com");
        Customer savedCustomer = entityManager.persistAndFlush(customer);

        // Act
        Optional<Customer> result = customerRepository.findById(savedCustomer.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test User");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when customer id does not exist")
    void findById_ShouldReturnEmpty_WhenCustomerDoesNotExist() {
        // Arrange - no customer with id 999L

        // Act
        Optional<Customer> result = customerRepository.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should save and retrieve customer correctly")
    void save_ShouldPersistCustomer() {
        // Arrange
        Customer customer = new Customer("New Customer", "new@example.com");

        // Act
        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        Optional<Customer> result = customerRepository.findById(savedCustomer.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getName()).isEqualTo("New Customer");
        assertThat(result.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Should delete customer by id")
    void deleteById_ShouldRemoveCustomer() {
        // Arrange
        Customer customer = new Customer("To Be Deleted", "delete@example.com");
        Customer savedCustomer = entityManager.persistAndFlush(customer);
        Long customerId = savedCustomer.getId();

        // Act
        customerRepository.deleteById(customerId);
        entityManager.flush();

        // Assert
        Optional<Customer> result = customerRepository.findById(customerId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should count customers correctly")
    void count_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persist(new Customer("Customer 1", "c1@example.com"));
        entityManager.persist(new Customer("Customer 2", "c2@example.com"));
        entityManager.persist(new Customer("Customer 3", "c3@example.com"));
        entityManager.flush();

        // Act
        long count = customerRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero count when no customers exist")
    void count_ShouldReturnZero_WhenNoCustomersExist() {
        // Arrange - no customers persisted

        // Act
        long count = customerRepository.count();

        // Assert
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Should check if customer exists by id")
    void existsById_ShouldReturnTrue_WhenCustomerExists() {
        // Arrange
        Customer customer = new Customer("Existing", "existing@example.com");
        Customer savedCustomer = entityManager.persistAndFlush(customer);

        // Act
        boolean exists = customerRepository.existsById(savedCustomer.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when customer does not exist by id")
    void existsById_ShouldReturnFalse_WhenCustomerDoesNotExist() {
        // Arrange - no customer with id 999L

        // Act
        boolean exists = customerRepository.existsById(999L);

        // Assert
        assertThat(exists).isFalse();
    }


}

