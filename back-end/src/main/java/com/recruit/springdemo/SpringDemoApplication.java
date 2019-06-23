package com.recruit.springdemo;

import com.recruit.springdemo.domain.Coffee;
import com.recruit.springdemo.service.CoffeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * @author xuzhiwei
 */
@SpringBootApplication
@Slf4j
@EnableTransactionManagement
@EntityScan("com.recruit.springdemo.domain")
public class SpringDemoApplication implements ApplicationRunner {

    @Autowired
    private CoffeeService coffeeService;

    public static void main(String[] args) {
        SpringApplication.run(SpringDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        Optional<Coffee> latte = coffeeService.findCoffeeByName("latte");
        latte.ifPresent(coffee -> log.info("Coffee: {}", coffee));
        log.info("All Coffee: {}", coffeeService.findAllCoffee());
    }
}
