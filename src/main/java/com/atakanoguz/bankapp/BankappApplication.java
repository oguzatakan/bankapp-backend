package com.atakanoguz.bankapp;

import com.atakanoguz.bankapp.model.*;
import com.atakanoguz.bankapp.repository.AccountRepository;
import com.atakanoguz.bankapp.repository.AddressRepository;
import com.atakanoguz.bankapp.repository.CustomerRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
@EnableCaching
public class BankappApplication implements CommandLineRunner {

	private final AccountRepository accountRepository;
	private final CustomerRepository customerRepository;
	private final AddressRepository addressRepository;

	public BankappApplication(AccountRepository accountRepository,CustomerRepository customerRepository, AddressRepository addressRepository){

		this.accountRepository = accountRepository;
		this.customerRepository= customerRepository;
		this.addressRepository = addressRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(BankappApplication.class, args);

	}

	@Bean
	public OpenAPI customOpenAPI(@Value("${application-description}") String description,@Value("${application-version}") String version){
		return new OpenAPI()
				.info(new Info()
				.title("BankApp API")
				.version(version)
				.description(description)
				.license(new License().name("BankApp API Licance")));
	}

	@Override
	public void run(String... args) throws Exception{
		Customer c1 = Customer.builder()
				.id("1234568")
				.name("Atakan")
				.address(Address.builder().city(City.CANAKKALE).postcode("17100").addressDetails("bu bir adrestir").build())
				.city(City.CANAKKALE)
				.dateOfBirth(2000)
				.build();

		Customer c2 = Customer.builder()
				.id("987653")
				.name("Mehmet")
				.city(City.MANISA)
				.address(Address.builder().city(City.MANISA).postcode("45000").addressDetails("bu bir adrestir 2").build())
				.dateOfBirth(1999)
				.build();

		Customer c3 = Customer.builder()
				.id("234987")
				.name("Ahmet")
				.city(City.IZMIR)
				.address(Address.builder().city(City.IZMIR).postcode("456312").addressDetails("bu bir adrestir 3").build())
				.dateOfBirth(2001)
				.build();

		customerRepository.saveAll(Arrays.asList(c1,c2,c3));

		Account a1 = Account.builder()
				.id("100")
				.customerId("1234569")
				.city(City.ISTANBUL)
				.balance(1350.0)
				.currency(Currency.TRY)
				.build();

		Account a2 = Account.builder()
				.id("101")
				.customerId("987653")
				.city(City.ISTANBUL)
				.balance(7898.0)
				.currency(Currency.TRY)
				.build();

		Account a3 = Account.builder()
				.id("102")
				.customerId("234987")
				.city(City.ISTANBUL)
				.balance(120000.0)
				.currency(Currency.TRY)
				.build();

		accountRepository.saveAll(Arrays.asList(a1,a2,a3));
	}

}
