package com.baizhi.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true) // 链式调用 各个setter方法可以链式赋值
public class User implements Serializable {

    private    String id;
    private String name;
    private Integer age;
    private Date bir;

}
