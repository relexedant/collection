package com.atguigu.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.util.Date;
import java.util.PropertyPermission;

@Data
public class User {
    //@TableId(type = IdType.ID_WORKER) //mp自带策略，生成19位值，数字类型使用这种策略，比如long
    //@TableId(type = IdType.ID_WORKER_STR) //mp自带策略，生成19位值，字符串类型使用这种策略

    private Long id;
    private String name;
    private Integer age;
    private String email;


    // 驼峰式命名
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    // 乐观锁 版本号
    @Version
    @TableField(fill=FieldFill.INSERT)
    private Integer version;

}
