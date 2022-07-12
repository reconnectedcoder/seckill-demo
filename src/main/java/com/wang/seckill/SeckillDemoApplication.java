package com.wang.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class SeckillDemoApplication { //引导类 执行 run() 扫描当前类所在包及其子包，加载bean
    public static void main(String[] args) {
        SpringApplication.run(SeckillDemoApplication.class, args);
    }
}