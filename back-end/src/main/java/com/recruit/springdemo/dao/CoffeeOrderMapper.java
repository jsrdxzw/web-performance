package com.recruit.springdemo.dao;

import com.recruit.springdemo.domain.CoffeeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Mapper
public interface CoffeeOrderMapper {

    CoffeeOrder insertCoffeeOrder(CoffeeOrder order);

    void updateCoffeeOrder(CoffeeOrder order);
}
