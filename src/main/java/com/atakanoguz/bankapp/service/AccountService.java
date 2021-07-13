package com.atakanoguz.bankapp.service;

import com.atakanoguz.bankapp.dto.*;
import com.atakanoguz.bankapp.exception.CustomerNotFoundException;
import com.atakanoguz.bankapp.model.Account;
import com.atakanoguz.bankapp.model.Customer;
import com.atakanoguz.bankapp.repository.AccountRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerService customerService;
    private AccountDtoConverter accountDtoConverter;

    private final AmqpTemplate rabbitTemplate;

    private final KafkaTemplate<String,String> kafkaTemplate;

    private final DirectExchange exchange;

    public AccountService(AccountRepository accountRepository,CustomerService customerService, AccountDtoConverter accountDtoConverter, DirectExchange exchange,AmqpTemplate rabbitTemplate, KafkaTemplate<String,String> kafkaTemplate){
        this.accountRepository = accountRepository;
        this.customerService = customerService;
        this.accountDtoConverter = accountDtoConverter;
        this.exchange = exchange;
    }

    @CachePut(value = "accounts", key = "#id")
    public AccountDto createAccount(CreateAccountRequest createAccountRequest) {
        Customer customer = customerService.getCustomerById(createAccountRequest.getCustomerId());

        if (customer.getId() == null || customer.getId().trim().equals("")) {
            throw new CustomerNotFoundException("Customer Not Found !");
        }

        Account account = Account.builder()
                .id(createAccountRequest.getId())
                .balance(createAccountRequest.getBalance())
                .currency(createAccountRequest.getCurrency())
                .customerId(createAccountRequest.getCustomerId())
                .city(createAccountRequest.getCity())
                .build();

        return accountDtoConverter.convert(accountRepository.save(account));
    }

    @CacheEvict(value = "accounts", allEntries = true)
    public AccountDto updateAccount(String id, UpdateAccountRequest request) {
        Customer customer = customerService.getCustomerById(request.getCustomerId());
        if (customer.getId().equals("") || customer.getId() == null) {
            return AccountDto.builder().build();
        }

        Optional<Account> accountOptional = accountRepository.findById(id);
        accountOptional.ifPresent(account -> {
            account.setBalance(request.getBalance());
            account.setCity(request.getCity());
            account.setCurrency(request.getCurrency());
            account.setCustomerId(request.getCustomerId());
            accountRepository.save(account);
        });

        return accountOptional.map(accountDtoConverter::convert).orElse(new AccountDto());
    }

    @Cacheable(value = "accounts")
    public List<AccountDto> getAllAccountsDto() {
        List<Account> accountList = accountRepository.findAll();

        return accountList.stream().map(accountDtoConverter::convert).collect(Collectors.toList());
    }

    public AccountDto getAccountById(String id) {
        return accountRepository.findById(id)
                .map(accountDtoConverter::convert)
                .orElse(new AccountDto());
    }

    @CacheEvict(value = "accounts", allEntries = true)
    public void deleteAccount(String id) {
        accountRepository.deleteById(id);
    }

    public AccountDto withdrawMoney(String id, Double amount) {
        Optional<Account> accountOptional = accountRepository.findById(id);
        accountOptional.ifPresent(account -> {
            if (account.getBalance() > amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepository.save(account);
            } else {
                System.out.println("Insufficient balance -> accountId:" +id + " balance: " + account.getBalance() + " amount " +amount);
            }
        });

        return accountOptional.map(accountDtoConverter::convert).orElse(new AccountDto());
    }

    public AccountDto addMoney(String id, Double amount) {
        Optional<Account> accountOptional = accountRepository.findById(id);

        accountOptional.ifPresent(account -> {
            account.setBalance(account.getBalance() + amount);

            accountRepository.save(account);
        });

        return accountOptional.map(accountDtoConverter::convert).orElse(new AccountDto());
    }

    public void transferMoney(MoneyTransferRequest transferRequest){
        rabbitTemplate.convertAndSend(exchange.getName(), routingKey, transferRequest);
    }

    @RabbitListener(queues = "${sample.rabbitmq.queue}")
    public void transferMoneyMessage(MoneyTransferRequest transferRequest) {
        Optional<Account> accountOptional = accountRepository.findById(transferRequest.getFromId());
        accountOptional.ifPresentOrElse(account -> {
                    if (account.getBalance() > transferRequest.getAmount()) {
                        account.setBalance(account.getBalance() - transferRequest.getAmount());
                        accountRepository.save(account);
                        rabbitTemplate.convertAndSend(exchange.getName(), "secondRoute", transferRequest);
                    } else {
                        System.out.println("Insufficient funds -> accountId: " + transferRequest.getFromId() + " balance: " + account.getBalance() + " amount: " + transferRequest.getAmount());
                    }},
                () -> System.out.println("Account not found")
        );
    }

    @RabbitListener(queues = "secondStepQueue")
    public void updateReceiverAccount(MoneyTransferRequest transferRequest) {
        Optional<Account> accountOptional = accountRepository.findById(transferRequest.getToId());
        accountOptional.ifPresentOrElse(account -> {
                    account.setBalance(account.getBalance() + transferRequest.getAmount());
                    accountRepository.save(account);
                    rabbitTemplate.convertAndSend(exchange.getName(), "thirdRoute", transferRequest);
                },
                () -> {
                    System.out.println("Receiver Account not found");
                    Optional<Account> senderAccount = accountRepository.findById(transferRequest.getFromId());
                    senderAccount.ifPresent(sender -> {
                        System.out.println("Money charge back to sender");
                        sender.setBalance(sender.getBalance() + transferRequest.getAmount());
                        accountRepository.save(sender);
                    });

                }
        );
    }

    @RabbitListener(queues = "thirdStepQueue")
    public void finalizeTransfer(MoneyTransferRequest transferRequest) {
        Optional<Account> accountOptional = accountRepository.findById(transferRequest.getFromId());
        accountOptional.ifPresentOrElse(account ->
                {
                    String notificationMessage = "Dear customer %s \n Your money transfer request has been succeed. Your new balance is %s";
                    System.out.println("Sender(" + account.getId() +") new account balance: " + account.getBalance());
                    String senderMessage = String.format(notificationMessage, account.getId(), account.getBalance());
                    kafkaTemplate.send("transfer-notification",  senderMessage);
                }, () -> System.out.println("Account not found")
        );

        Optional<Account> accountToOptional = accountRepository.findById(transferRequest.getToId());
        accountToOptional.ifPresentOrElse(account ->
                {
                    String notificationMessage = "Dear customer %s \n You received a money transfer from %s. Your new balance is %s";
                    System.out.println("Receiver(" + account.getId() +") new account balance: " + account.getBalance());
                    String receiverMessage = String.format(notificationMessage, account.getId(), transferRequest.getFromId(), account.getBalance());
                    kafkaTemplate.send("transfer-notification",  receiverMessage);
                },
                () -> System.out.println("Account not found")
        );


    }

}
