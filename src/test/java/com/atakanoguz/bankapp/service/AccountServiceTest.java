package com.atakanoguz.bankapp.service;

import com.atakanoguz.bankapp.dto.AccountDtoConverter;
import com.atakanoguz.bankapp.dto.CreateAccountRequest;
import com.atakanoguz.bankapp.model.Address;
import com.atakanoguz.bankapp.model.City;
import com.atakanoguz.bankapp.model.Currency;
import com.atakanoguz.bankapp.model.Customer;
import com.atakanoguz.bankapp.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.Assert.*;

public class AccountServiceTest {

    private  AccountService accountService;

    private AccountRepository accountRepository;
    private CustomerService customerService;
    private AccountDtoConverter accountDtoConverter;
    private DirectExchange exchange;
    private AmqpTemplate rabbitTemplate;
    private KafkaTemplate kafkaTemplate;

    @Before
    public void setUp() throws Exception {
        accountRepository = Mockito.mock(AccountRepository.class);
        customerService = Mockito.mock(CustomerService.class);
        accountDtoConverter = Mockito.mock(AccountDtoConverter.class);
        exchange = Mockito.mock(DirectExchange.class);
        rabbitTemplate = Mockito.mock(AmqpTemplate.class);
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);

        accountService = new AccountService(accountRepository, customerService , accountDtoConverter, exchange, rabbitTemplate, kafkaTemplate);

    }

    @Test
    public void whenCreateAccountCalledWithValidRequest_itShouldReturnValidAccountDto(){
        CreateAccountRequest createAccountRequest = new CreateAccountRequest("1234");
        createAccountRequest.setCustomerId("12345");
        createAccountRequest.setBalance(100.0);
        createAccountRequest.setCity(City.ISTANBUL);
        createAccountRequest.setCurrency(Currency.TRY);

        Customer customer = Customer.builder()
                .id("12345")
                .address(Address.builder().city(City.ISTANBUL).postcode("456312").addressDetails("bu bir adrestir").build())
                .city(City.ISTANBUL)
                .dateOfBirth(2000)
                .name("Atakan")
                .build();
        Mockito.when(customerService.getCustomerById(""));
    }

}