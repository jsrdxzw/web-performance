package com.recruit.springdemo.dao;

import com.recruit.springdemo.domain.Coffee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Mapper
public interface CoffeeMapper {
    Coffee findCoffeeById(@Param("id") int id);
}
