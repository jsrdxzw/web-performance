package com.recruit.springdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeOrder {
    private String customer;
    private List<Coffee> items;
    private OrderState state;
}
