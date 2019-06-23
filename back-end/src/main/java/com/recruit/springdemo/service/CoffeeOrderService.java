package com.recruit.springdemo.service;

import com.recruit.springdemo.dao.CoffeeOrderMapper;
import com.recruit.springdemo.domain.Coffee;
import com.recruit.springdemo.domain.CoffeeOrder;
import com.recruit.springdemo.domain.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Slf4j
@Service
@Transactional
public class CoffeeOrderService {

    @Autowired
    private CoffeeOrderMapper coffeeOrderMapper;

    public CoffeeOrder createOrder(String customer, Coffee... coffee) {
        CoffeeOrder order = CoffeeOrder.builder()
                .customer(customer)
                .items(new ArrayList<>(Arrays.asList(coffee)))
                .state(OrderState.INIT)
                .build();
        CoffeeOrder saved = coffeeOrderMapper.insertCoffeeOrder(order);
        log.info("New Order: {}", saved);
        return saved;
    }

    public boolean updateState(CoffeeOrder order, OrderState state) {
        if (state.compareTo(order.getState()) <= 0) {
            log.warn("Wrong State order: {}, {}", state, order.getState());
            return false;
        }
        order.setState(state);
        coffeeOrderMapper.updateCoffeeOrder(order);
        log.info("Updated Order: {}", order);
        return true;
    }
}
