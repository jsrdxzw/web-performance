package com.recruit.springdemo.service;

import com.recruit.springdemo.dao.CoffeeMapper;
import com.recruit.springdemo.domain.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Slf4j
@Service
public class CoffeeService {
    @Autowired
    private CoffeeMapper coffeeMapper;

    public Optional<Coffee> findCoffeeByName(String name) {
        Optional<Coffee> coffee = coffeeMapper.findCoffeeByName(name);
        log.info("Coffee Found: {}", coffee);
        return coffee;
    }

    public List<Coffee> findAllCoffee() {
        return coffeeMapper.findCoffeeList();
    }
}
