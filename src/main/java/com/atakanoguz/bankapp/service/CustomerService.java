package com.atakanoguz.bankapp.service;

import com.atakanoguz.bankapp.dto.CreateCustomerRequest;
import com.atakanoguz.bankapp.dto.CustomerDto;
import com.atakanoguz.bankapp.dto.CustomerDtoConverter;
import com.atakanoguz.bankapp.dto.UpdateCustomerRequest;
import com.atakanoguz.bankapp.model.City;
import com.atakanoguz.bankapp.model.Customer;
import com.atakanoguz.bankapp.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerDtoConverter customerDtoConverter;

    public CustomerService(CustomerRepository customerRepository, CustomerDtoConverter customerDtoConverter) {
        this.customerRepository = customerRepository;
        this.customerDtoConverter = customerDtoConverter;
    }

    public CustomerDto createCustomer(CreateCustomerRequest customerRequest) {

        Customer customer = new Customer();

        customer.setId(customerRequest.getId());
        customer.setName(customerRequest.getName());
        customer.setDateOfBirth(customerRequest.getDateOfBirth());
        customer.setCity(City.valueOf(customerRequest.getCity().name()));

        customerRepository.save(customer);

        return customerDtoConverter.convert(customer);
    }

    public List<CustomerDto> getAllCustomers() {
        List<Customer> customerList = customerRepository.findAll();

        List<CustomerDto> customerDtoList = new ArrayList<>();

        for (Customer customer : customerList){
            customerDtoList.add(customerDtoConverter.convert(customer));
        }

        return customerDtoList;
    }

    @Transactional
    public CustomerDto getCustomerDtoById(String id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);

        return customerOptional.map(customerDtoConverter::convert).orElse(new CustomerDto());
    }

    public void deleteCustomer(String id) {
        customerRepository.deleteById(id);
    }

    public CustomerDto updateCustomer(String id, UpdateCustomerRequest customerRequest) {
        Optional<Customer> customerOptional = customerRepository.findById(id);

        customerOptional.ifPresent(customer -> {
            customer.setName(customerRequest.getName());
            customer.setCity(City.valueOf(customerRequest.getCity().name()));
            customer.setDateOfBirth(customerRequest.getDateOfBirth());
            customerRepository.save(customer);
        });

        return customerOptional.map(customerDtoConverter::convert).orElse(new CustomerDto());
    }

    protected Customer getCustomerById(String id) {
        return customerRepository.findById(id).orElse(new Customer());
    }
}
