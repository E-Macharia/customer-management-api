package com.example.customer_api.repository;

import com.example.customer_api.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testSaveAndFindCustomer() {
        Customer customer = Customer.builder()
                .firstName("Alice")
                .lastName("Wonderland")
                .email("alice@example.com")
                .phoneNumber("+123456789")
                .build();

        Customer saved = customerRepository.save(customer);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("alice@example.com", found.get().getEmail());
    }

    @Test
    void testExistsByEmail() {
        Customer customer = Customer.builder()
                .firstName("Bob")
                .lastName("Builder")
                .email("bob@example.com")
                .phoneNumber("+987654321")
                .build();

        customerRepository.save(customer);

        assertTrue(customerRepository.existsByEmail("bob@example.com"));
        assertFalse(customerRepository.existsByEmail("notfound@example.com"));
    }

    @Test
    void testExistsByEmailAndIdNot() {
        Customer customer1 = Customer.builder()
                .firstName("Bob")
                .lastName("Builder")
                .email("bob@example.com")
                .phoneNumber("+987654321")
                .build();

        Customer saved1 = customerRepository.save(customer1);

        Customer customer2 = Customer.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie@example.com")
                .phoneNumber("+111222333")
                .build();

        Customer saved2 = customerRepository.save(customer2);

        // bob@example.com belongs to saved1, so it exists for another ID (saved2)
        assertTrue(customerRepository.existsByEmailAndIdNot("bob@example.com", saved2.getId()));
        // bob@example.com belongs to saved1, so it does NOT exist for another ID (saved1)
        assertFalse(customerRepository.existsByEmailAndIdNot("bob@example.com", saved1.getId()));
    }
}
