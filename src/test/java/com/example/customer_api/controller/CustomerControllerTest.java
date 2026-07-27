package com.example.customer_api.controller;

import com.example.customer_api.config.SecurityConfig;
import com.example.customer_api.dto.CustomerRequestDTO;
import com.example.customer_api.dto.CustomerResponseDTO;
import com.example.customer_api.exception.CustomerNotFoundException;
import com.example.customer_api.security.JwtAuthenticationFilter;
import com.example.customer_api.security.JwtTokenProvider;
import com.example.customer_api.service.CustomUserDetailsService;
import com.example.customer_api.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private CustomerResponseDTO responseDTO;
    private CustomerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = CustomerResponseDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
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
    void getAllCustomers_ShouldReturnCustomersList() throws Exception {
        Page<CustomerResponseDTO> page = new PageImpl<>(Arrays.asList(responseDTO));
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/customers")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"));

        verify(customerService, times(1)).getAllCustomers(any(Pageable.class));
    }

    @Test
    void getCustomerById_WhenExists_ShouldReturnCustomer() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/customers/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).getCustomerById(1L);
    }

    @Test
    void getCustomerById_WhenNotExists_ShouldReturn404() throws Exception {
        when(customerService.getCustomerById(1L)).thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(customerService, times(1)).getCustomerById(1L);
    }

    @Test
    void createCustomer_WithValidInput_ShouldReturn201() throws Exception {
        when(customerService.createCustomer(any(CustomerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/customers")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).createCustomer(any(CustomerRequestDTO.class));
    }

    @Test
    void createCustomer_WithInvalidInput_ShouldReturn400() throws Exception {
        CustomerRequestDTO invalidRequest = CustomerRequestDTO.builder()
                .firstName("") // Blank
                .lastName("Doe")
                .email("not-an-email") // Invalid email
                .phoneNumber("invalid-phone") // Invalid phone
                .build();

        mockMvc.perform(post("/api/customers")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void updateCustomer_WithValidInput_ShouldReturn200() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/customers/1")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).updateCustomer(eq(1L), any(CustomerRequestDTO.class));
    }

    @Test
    void deleteCustomer_WhenExists_ShouldReturn204() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(1L);
    }

    @Test
    void deleteCustomer_WhenNotAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/customers/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

        verify(customerService, never()).deleteCustomer(anyLong());
    }
}
