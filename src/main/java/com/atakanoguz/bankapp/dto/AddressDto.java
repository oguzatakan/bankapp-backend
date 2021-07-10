package com.atakanoguz.bankapp.dto;

import com.atakanoguz.bankapp.model.City;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class AddressDto {

    private String id;
    private City city;
    private String postCode;
}
