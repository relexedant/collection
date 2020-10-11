package com.atguigu.mybatisplus;

import com.atguigu.mybatisplus.entity.User;
import com.atguigu.mybatisplus.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.xalan.internal.xsltc.dom.SortingIterator;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class MybatisPlusApplicationTests {

    @Autowired
    UserMapper userMapper;
    // 此处报错可以不理会，程序可以运行
    // 在接口上增加 @Reposity 可以消除此项报错
    // 使容器扫描到该包所在位置的两个注解：@Mapper @MapperScan

    // 查询user表中数据
    @Test
    void contextLoads() {
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
    }


    /**
     * mp实现添加操作
     * 不需要设置id 主键的值 自动生成
     * mp自动生成的主键为19位
     * <p>
     * 主键生成策略
     */
    @Test
    public void testAdd() {
        User u = new User();
        u.setName("东方不败");
        u.setAge(55);
        u.setEmail("sss@ee.com");
        int insert = userMapper.insert(u);
        System.out.println("add" + insert);
        System.out.println("user" + u);
    }


    /**
     * 修改操作
     *
     */

    @Test
    public void testUpdate(){
        User u = new User();
        u.setId(1314152074350460929L);
        u.setAge(20);
        u.setCreateTime(new Date());
        // 使用此方法需要给元素设置id值
        // 需要修改什么属性，就给元素设置什么属性
        int i = userMapper.updateById(u);
        System.out.println(i);
    }

    /**
     * 乐观锁测试
     */
    @Test
    public void testLock() {
        User u = userMapper.selectById(1314194949469745154L);

        u.setAge(200);
        int i = userMapper.updateById(u);
        System.out.println("testLock：" + i);
        System.out.println("user：" + u);
    }


    /**
     * 分页查询
     */
    @Test
    public void testPage() {

        // 1. 创建分页对象，传入两个参数
        Page<User> page = new Page<>(1,3);

        userMapper.selectPage(page,null);


        System.out.println(page.getCurrent());//当前页
        System.out.println(page.getRecords());//每页数据list集合
        System.out.println(page.getSize());//每页显示记录数
        System.out.println(page.getTotal()); //总记录数
        System.out.println(page.getPages()); //总页数

        System.out.println(page.hasNext()); //下一页
        System.out.println(page.hasPrevious()); //上一页

    }

}
