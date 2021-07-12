package com.atakanoguz.bankapp.repository;

import com.atakanoguz.bankapp.model.Account;
import com.atakanoguz.bankapp.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findAllByBalanceGreaterThan(Double balance);

    List<Account> findAllByCurrencyIsAndBalanceLessThan(Currency currency,Double balance);
}
