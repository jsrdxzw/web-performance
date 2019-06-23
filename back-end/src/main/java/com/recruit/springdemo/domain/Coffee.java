package com.recruit.springdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.joda.money.Money;

import java.io.Serializable;
import java.sql.Date;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-23
 * @Description:
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Coffee implements Serializable {
    private static final long serialVersionUID = 6655146413950383765L;

    private int id;
    private String name;
    private Money price;
    private Date createTime;
    private Date updateTime;
}
