package com.recruit.springdemo.dao;

import com.recruit.springdemo.domain.Coffee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Mapper
public interface CoffeeMapper {

    Optional<Coffee> findCoffeeById(@Param("id") long id);

    Optional<Coffee> findCoffeeByName(@Param("name") String name);

    List<Coffee> findCoffeeList();

}
