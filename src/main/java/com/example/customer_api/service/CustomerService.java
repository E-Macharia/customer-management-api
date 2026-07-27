package com.example.customer_api.service;

import com.example.customer_api.dto.CustomerResponseDTO;
import com.example.customer_api.dto.CustomerRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CustomerService {
    Page<CustomerResponseDTO> getAllCustomers(Pageable pageable);
    CustomerResponseDTO getCustomerById(Long id);
    CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO);
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO);
    void deleteCustomer(Long id);
}
