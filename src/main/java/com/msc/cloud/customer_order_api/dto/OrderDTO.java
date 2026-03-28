package com.msc.cloud.customer_order_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class OrderDTO {
// Getter and Setter
    // SET ID
    // GET ID
    private Long id;
    // SET ORDER DATE
    // GET ORDER DATE
    private LocalDate orderDate;
    // SET AMOUNT
    // GET AMOUNT
    private Double amount;

}
