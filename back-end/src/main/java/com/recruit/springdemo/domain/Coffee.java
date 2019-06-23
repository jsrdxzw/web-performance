package com.recruit.springdemo.domain;

import lombok.*;
import org.joda.money.Money;

import java.io.Serializable;
import java.sql.Date;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coffee implements Serializable {
    private static final long serialVersionUID = 6655146413950383765L;

    private long id;
    private String name;
    private Money price;
    private Date createTime;
    private Date updateTime;
}
