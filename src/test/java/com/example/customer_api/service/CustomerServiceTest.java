package com.example.customer_api.service;

import com.example.customer_api.dto.CustomerRequestDTO;
import com.example.customer_api.dto.CustomerResponseDTO;
import com.example.customer_api.exception.CustomerNotFoundException;
import com.example.customer_api.model.Customer;
import com.example.customer_api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer1;
    private Customer customer2;
    private CustomerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        customer1 = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customer2 = Customer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("+1987654321")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        requestDTO = CustomerRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .build();
    }

    @Test
    void getAllCustomers_ShouldReturnList() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Customer> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(customer1, customer2));
        when(customerRepository.findAll(pageable)).thenReturn(page);

        org.springframework.data.domain.Page<CustomerResponseDTO> result = customerService.getAllCustomers(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("john.doe@example.com", result.getContent().get(0).getEmail());
        assertEquals("jane.smith@example.com", result.getContent().get(1).getEmail());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    void getCustomerById_WhenExists_ShouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer1));

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getCustomerById_WhenNotExists_ShouldThrowException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(1L));
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void createCustomer_WhenEmailUnique_ShouldCreateCustomer() {
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer1);

        CustomerResponseDTO result = customerService.createCustomer(requestDTO);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(customerRepository, times(1)).existsByEmail(requestDTO.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_WhenEmailExists_ShouldThrowIllegalArgumentException() {
        when(customerRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(requestDTO));
        verify(customerRepository, times(1)).existsByEmail(requestDTO.getEmail());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_WhenCustomerExistsAndEmailUnique_ShouldUpdateCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer1));
        when(customerRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer1);

        CustomerResponseDTO result = customerService.updateCustomer(1L, requestDTO);

        assertNotNull(result);
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).existsByEmailAndIdNot(requestDTO.getEmail(), 1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenEmailExistsForOther_ShouldThrowIllegalArgumentException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer1));
        when(customerRepository.existsByEmailAndIdNot(any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> customerService.updateCustomer(1L, requestDTO));
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).existsByEmailAndIdNot(requestDTO.getEmail(), 1L);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomer_WhenExists_ShouldDelete() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));

        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_WhenNotExists_ShouldThrowCustomerNotFoundException() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer(1L));

        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, never()).deleteById(anyLong());
    }
}
