package com.atakanoguz.bankapp.dto;

import com.atakanoguz.bankapp.model.Currency;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class AccountDto implements Serializable {

    private String id;
    private String customerId;
    private double balance;
    private Currency currency;
}
