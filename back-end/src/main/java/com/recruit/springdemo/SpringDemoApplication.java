package com.recruit.springdemo;

import com.recruit.springdemo.dao.CoffeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @author xuzhiwei
 */
@SpringBootApplication
@EntityScan("com.recruit.springdemo.domain")
public class SpringDemoApplication implements CommandLineRunner{

    @Autowired
    private CoffeeMapper coffeeMapper;

    public static void main(String[] args) {
        SpringApplication.run(SpringDemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println(coffeeMapper.findCoffeeById(1));
    }
}
