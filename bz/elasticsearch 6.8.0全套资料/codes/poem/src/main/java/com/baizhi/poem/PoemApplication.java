package com.baizhi.poem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baizhi.poem.dao")
public class PoemApplication {

    public static void main(String[] args) {
       SpringApplication.run(PoemApplication.class, args);
    }

}
