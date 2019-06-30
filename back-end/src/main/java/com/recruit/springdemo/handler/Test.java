package com.recruit.springdemo.handler;

/**
 * @Author: xuzhiwei
 * @Date: 2019-06-30
 * @Description:
 */
public class Test {
    public static void main(String[] args) {
        new Test().method1();
        new Test().method2();
    }

    public void method1(){
        int i = 1;
        int a = i++;
        System.out.println(a);
    }

    public void method2(){
        int i = 1;
        int a = ++i;
        System.out.println(a);
    }
}
