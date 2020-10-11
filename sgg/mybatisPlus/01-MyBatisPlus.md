# 一、简介

官网：http://mp.baomidou.com/

参考教程：http://mp.baomidou.com/guide/

[MyBatis-Plus](https://github.com/baomidou/mybatis-plus)（简称 MP）是一个 [MyBatis](http://www.mybatis.org/mybatis-3/) 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

### 1. 配置文件差异

在 `application.properties` 配置文件中添加 MySQL 数据库的相关配置：

#### mysql5 

```
#mysql数据库连接
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/mybatis_plus
```

#### mysql8以上（spring boot 2.1）

```
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/mybatis_plus?serverTimezone=GMT%2B8
```

**注意：**

1、这里的 url 使用了 ?serverTimezone=GMT%2B8 后缀，因为Spring Boot 2.1 集成了 8.0版本的jdbc驱动，这个版本的 jdbc 驱动需要添加这个后缀，否则运行测试用例报告如下错误：

java.sql.SQLException: The server time zone value 'ÖÐ¹ú±ê×¼Ê±¼ä' is unrecognized or represents more 

2、这里的 driver-class-name 使用了 com.mysql.cj.jdbc.Driver ，在 jdbc 8 中 建议使用这个驱动，之前的 com.mysql.jdbc.Driver 已经被废弃，否则运行测试用例的时候会有 WARN 信息

### 2.显示sql语句日志

```properties
// 以下两种都可以
#logging.level.com.atguigu.mybatisplus.mapper=debug

#mybatis日志
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

### 3.插入操作 insert

##### **1. 有自动回填功能**

~~~java
   @Test
    public void testInsert(){
		User user = new User();
        user.setName("Helen");
        user.setAge(18);
        user.setEmail("55317332@qq.com");

        int result = userMapper.insert(user);
        System.out.println(result); //影响的行数
        System.out.println(user); //id自动回填
    }
}
~~~



### 4.主键生成策略

##### **（1）ID_WORKER**

MyBatis-Plus默认的主键策略是：ID_WORKER  *全局唯一ID*

**参考资料：分布式系统唯一ID生成方案汇总：**https://www.cnblogs.com/haoxinyue/p/5208136.html

~~~java
 	//@TableId(type = IdType.ID_WORKER) //mp自带策略，生成19位值，数字类型使用这种策略，比如long
    //@TableId(type = IdType.ID_WORKER_STR) //mp自带策略，生成19位值，字符串类型使用这种策略
    private Long id;
~~~

![image-20201008181043299](https://gitee.com/guo_xingchao/PicGo/raw/master/image/20201008181043.png)

##### **（2）自增策略**

要想主键自增需要配置如下主键策略

​		1. 需要在创建数据表的时候设置主键自增

​		2. 实体字段中配置 @TableId(type = IdType.AUTO)

 要想影响所有实体的配置，可以设置全局主键配置 

```
#全局设置主键生成策略
mybatis-plus.global-config.db-config.id-type=auto
```

其它主键策略：分析 IdType 源码可知 

```
@Getter
public enum IdType {
    /**
     * 数据库ID自增
     */
    AUTO(0),
    /**
     * 该类型为未设置主键类型
     */
    NONE(1),
    /**
     * 用户输入ID
     * 该类型可以通过自己注册自动填充插件进行填充
     */
    INPUT(2),
    /* 以下3种类型、只有当插入对象ID 为空，才自动填充。 */
    /**
     * 全局唯一ID (idWorker)
     */
    ID_WORKER(3),
    /**
     * 全局唯一ID (UUID)
     */
    UUID(4),
    /**
     * 字符串全局唯一ID (idWorker 的字符串表示)
     */
    ID_WORKER_STR(5);
    private int key;
    IdType(int key) {
        this.key = key;
    }
}
```

##### （3）其他主键生成策略

~~~
1. 数据库自增长序列或字段
2. UUID
4. Redis生成ID
5. Twitter的snowflake算法（mp自带默认算法）
~~~



### 5.update

```java
User u = new User();
u.setId(2L);
u.setAge(20);
// 使用此方法需要给元素设置id值
// 需要修改什么属性，就给元素设置什么属性
int i = userMapper.updateById(u);
```

### 6.自动填充

项目中经常会遇到一些数据，每次都使用相同的方式填充，例如记录的创建时间，更新时间等。

我们可以使用MyBatis Plus的自动填充功能，完成这些字段的赋值工作：不需要手动 `set`到对象中

**（1）数据库表中添加自动填充字段**

~~~
在User表中添加datetime类型的新的字段 create_time、update_time
~~~

**（2）实体上添加注解**

~~~
@Data
public class User {
    // 字段名使用驼峰命名法
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
~~~



**（3）实现元对象处理器接口**

注意：不要忘记添加 **@Component** 注解

```java
import java.util.Date;
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyMetaObjectHandler.class);
    @Override
    public void insertFill(MetaObject metaObject) {
        LOGGER.info("start insert fill ....");
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
    @Override
    public void updateFill(MetaObject metaObject) {
        LOGGER.info("start update fill ....");
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
```

### 7.乐观锁

乐观锁**主要适用场景：**当要更新一条记录的时候，希望这条记录没有被别人更新，也就是说实现线程安全的数据更新

**乐观锁实现方式**：

1. 取出记录时，获取当前version
2. 更新时，带上这个version
3. 执行更新时， set version = newVersion where version = oldVersion
4. 如果version不对，就更新失败

**（1）数据库中添加version字段** 

```
	ALTER TABLE `user` ADD COLUMN `version` INT
```


**（2）实体类添加version字段**
并添加 @Version 注解 

```
@Version
@TableField(fill = FieldFill.INSERT)
private Integer version;
```

**（3）元对象处理器接口添加version的insert默认值** 

```
@Override
public void insertFill(MetaObject metaObject) {
    ......
    this.setFieldValByName("version", 1, metaObject);
}
```

**特别说明:**

1. 支持的数据类型只有 int,Integer,long,Long,Date,Timestamp,LocalDateTime
2. 整数类型下 `newVersion = oldVersion + 1`
3. `newVersion` 会回写到 `entity` 中
4. 仅支持 `updateById(id)` 与 `update(entity, wrapper)` 方法
5. 在 `update(entity, wrapper)` 方法下, `wrapper` 不能复用!!!

**（4）在 MybatisPlusConfig 中注册 Bean**创建配置类 

```
@Configuration
@MapperScan("com.atguigu.mybatis_plus.mapper")
public class MybatisPlusConfig {
    /**
     * 乐观锁插件
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }
}
```

**（5）测试乐观锁可以修改成功**测试后分析打印的sql语句，将version的数值进行了加1操作

 

```
/**
 * 测试 乐观锁插件
 */
@Test
public void testOptimisticLocker() {
    //查询
    User user = userMapper.selectById(1L);
    //修改数据
    user.setName("Helen Yao");
    user.setEmail("helen@qq.com");
    //执行更新
    userMapper.updateById(user);
}
```

**（5）测试乐观锁修改失败**

 

```java
/**
 * 测试乐观锁插件 失败
 */
@Test
public void testOptimisticLockerFail() {
    //查询
    User user = userMapper.selectById(1L);
    //修改数据
    user.setName("Helen Yao1");
    user.setEmail("helen@qq.com1");
    //模拟取出数据后，数据库中version实际数据比取出的值大，即已被其它线程修改并更新了version
    user.setVersion(user.getVersion() - 1);
    //执行更新
    userMapper.updateById(user);
}
```

### 8.select

~~~java
selectById
selectBatchIds
selectByMap  
~~~

### 9.分页查询

MyBatis Plus自带分页插件，只要简单的配置即可实现分页功能

#### **（1）创建配置类**

~~~java
/**
 * 分页插件
 */
@Bean
public PaginationInterceptor paginationInterceptor() {
    return new PaginationInterceptor();
}
~~~

#### **（2）测试selectPage分页**

**测试：**最终通过page对象获取相关数据

~~~java
@Test
public void testSelectPage() {

    Page<User> page = new Page<>(1,5);
    userMapper.selectPage(page, null);

    page.getRecords().forEach(System.out::println);
    System.out.println(page.getCurrent());
    System.out.println(page.getPages());
    System.out.println(page.getSize());
    System.out.println(page.getTotal());
    System.out.println(page.hasNext());
    System.out.println(page.hasPrevious());
}
~~~

### 10.删除

**物理删除**：真实删除，将对应数据从数据库中删除，之后查询不到此条被删除数据

~~~
deleteById
deleteBatchIds
deleteByMap  // map要写列名条件 不能是实体属性名  map中的条件之间是  交集关系
~~~

**逻辑删除**：假删除，将对应数据中代表是否被删除字段状态修改为“被删除状态”，之后在数据库中仍旧能看到此条数据记录

**（1）数据库中添加 deleted字段** 

```
ALTER TABLE `user` ADD COLUMN `deleted` boolean
```

**（2）实体类添加deleted 字段**

并加上 @TableLogic 注解 和 @TableField(fill = FieldFill.INSERT) 注解 

```
@TableLogic
@TableField(fill = FieldFill.INSERT)
private Integer deleted;
```

**（3）元对象处理器接口添加deleted的insert默认值** 

```
@Override
public void insertFill(MetaObject metaObject) {
    ......
    this.setFieldValByName("deleted", 0, metaObject);
}
```

**（4）application.properties 加入配置**
此为默认值，如果你的默认值和mp默认的一样,该配置可无 

```
mybatis-plus.global-config.db-config.logic-delete-value=1
mybatis-plus.global-config.db-config.logic-not-delete-value=0
```

**（5）在 MybatisPlusConfig 中注册 Bean** 

```
// 逻辑删除插件
@Bean
public ISqlInjector sqlInjector() {
    return new LogicSqlInjector();
}
```

### 11.性能分析插件

性能分析拦截器，用于输出每条 SQL 语句及其执行时间

SQL 性能执行分析,开发环境使用，超过指定时间，停止运行。有助于发现问题

#### 1、配置插件

**（1）参数说明**

参数：maxTime： SQL 执行最大时长，超过自动停止运行，有助于发现问题。

参数：format： SQL是否格式化，默认false。

**（2）在 MybatisPlusConfig 中配置** 

```
/**
 * SQL 执行性能分析插件
 * 开发环境使用，线上不推荐。 maxTime 指的是 sql 最大执行时长
 */
@Bean
@Profile({"dev","test"})// 设置 dev test 环境开启
public PerformanceInterceptor performanceInterceptor() {
    PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
    performanceInterceptor.setMaxTime(100);//ms，超过此处设置的ms则sql不执行
    performanceInterceptor.setFormat(true);
    return performanceInterceptor;
}
```

**（3）Spring Boot 中设置dev环境** 

```
#环境设置：dev、test、prod
spring.profiles.active=dev
```

可以针对各环境新建不同的配置文件`application-dev.properties`、`application-test.properties`、`application-prod.properties`

### 12.MybatisPlus 复杂条件查询

![img](https://gitee.com/guo_xingchao/PicGo/raw/master/image/20201008221059.png)

二、AbstractWrapper

**注意：**以下条件构造器的方法入参中的 `column `均表示`数据库字段`

**1、ge、gt、le、lt、isNull、isNotNull** 

```
@Test
public void testDelete() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper
        .isNull("name")
        .ge("age", 12)
        .isNotNull("email");
    int result = userMapper.delete(queryWrapper);
    System.out.println("delete return count = " + result);
}
```

SQL：UPDATE user SET deleted=1 WHERE deleted=0 AND name IS NULL AND age >= ? AND email IS NOT NULL



**2、eq、ne**

**注意：**seletOne返回的是`一条`实体记录，当出现多条时会报错 

```
@Test
public void testSelectOne() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("name", "Tom");
    User user = userMapper.selectOne(queryWrapper);
    System.out.println(user);
}
```

SELECT id,name,age,email,create_time,update_time,deleted,version FROM user WHERE deleted=0 AND name = ?



 **3、between、notBetween**包含大小边界 

```
@Test
public void testSelectCount() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.between("age", 20, 30);
    Integer count = userMapper.selectCount(queryWrapper);
    System.out.println(count);
}
```

SELECT COUNT(1) FROM user WHERE deleted=0 AND age BETWEEN ? AND ? 

**4、allEq** 

```
@Test
public void testSelectList() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    Map<String, Object> map = new HashMap<>();
    map.put("id", 2);
    map.put("name", "Jack");
    map.put("age", 20);
    queryWrapper.allEq(map);
    List<User> users = userMapper.selectList(queryWrapper);
    users.forEach(System.out::println);
}
```

SELECT id,name,age,email,create_time,update_time,deleted,version FROM user WHERE deleted=0 AND name = ? AND id = ? AND age = ? **
****5、like、notLike、likeLeft、likeRight**selectMaps返回Map集合列表

 

```
@Test
public void testSelectMaps() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper
        .notLike("name", "e")
        .likeRight("email", "t");
    List<Map<String, Object>> maps = userMapper.selectMaps(queryWrapper);//返回值是Map列表
    maps.forEach(System.out::println);
}
```

SELECT id,name,age,email,create_time,update_time,deleted,version FROM user WHERE deleted=0 AND name NOT LIKE ? AND email LIKE ? 
**6、in、notIn、inSql、notinSql、exists、notExists**

in、notIn：

`notIn("age",{1,2,3})--->age not in (1,2,3)`

`notIn("age", 1, 2, 3)--->age not in (1,2,3)`

inSql、notinSql：可以实现子查询

例: `inSql("age", "1,2,3,4,5,6")`--->`age in (1,2,3,4,5,6)`

例: `inSql("id", "select id from table where id < 3")`--->`id in (select id from table where id < 3)`

 

```
@Test
public void testSelectObjs() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    //queryWrapper.in("id", 1, 2, 3);
    queryWrapper.inSql("id", "select id from user where id < 3");
    List<Object> objects = userMapper.selectObjs(queryWrapper);//返回值是Object列表
    objects.forEach(System.out::println);
}
```

SELECT id,name,age,email,create_time,update_time,deleted,version FROM user WHERE deleted=0 AND id IN (select id from user where id < 3) 

**7、or、and****注意：**这里使用的是 UpdateWrapper 不调用`or`则默认为使用 `and `连 

```
@Test
public void testUpdate1() {
    //修改值
    User user = new User();
    user.setAge(99);
    user.setName("Andy");
    //修改条件
    UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
    userUpdateWrapper
        .like("name", "h")
        .or()
        .between("age", 20, 30);
    int result = userMapper.update(user, userUpdateWrapper);
    System.out.println(result);
}
```

UPDATE user SET name=?, age=?, update_time=? WHERE deleted=0 AND name LIKE ? OR age BETWEEN ? AND ?

**8、嵌套or、嵌套and**这里使用了lambda表达式，or中的表达式最后翻译成sql时会被加上圆括号 

```
@Test
public void testUpdate2() {
    //修改值
    User user = new User();
    user.setAge(99);
    user.setName("Andy");
    //修改条件
    UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
    userUpdateWrapper
        .like("name", "h")
        .or(i -> i.eq("name", "李白").ne("age", 20));
    int result = userMapper.update(user, userUpdateWrapper);
    System.out.println(result);
}
```

UPDATE user SET name=?, age=?, update_time=? WHERE deleted=0 AND name LIKE ? OR ( name = ? AND age <> ? ) 

**9、orderBy、orderByDesc、orderByAsc** 

```
@Test
public void testSelectListOrderBy() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.orderByDesc("id");
    List<User> users = userMapper.selectList(queryWrapper);
    users.forEach(System.out::println);
}
```

SELECT id,name,age,email,create_time,update_time,deleted,version FROM user WHERE deleted=0 ORDER BY id DESC 

**10、last**直接拼接到 sql 的最后
**注意：**只能调用一次,多次调用以最后一次为准 有sql注入的风险,请谨慎使用 

```
@Test
public void testSelectListLast() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.last("limit 1");
    List<User> users = userMapper.selectList(queryWrapper);
    users.forEach(System.out::println);
}
```

SELECT id,name,age,email,create_time,update_time,deleted,version FROM user WHERE deleted=0 limit 1 

**11、**指定要查询的列 

```
@Test
public void testSelectListColumn() {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("id", "name", "age");
    List<User> users = userMapper.selectList(queryWrapper);
    users.forEach(System.out::println);
}
```

SELECT id,name,age FROM user WHERE deleted=0 

**12、set、setSql**最终的sql会合并 user.setAge()，以及 userUpdateWrapper.set()  和 setSql() 中 的字段 

```
@Test
public void testUpdateSet() {
    //修改值
    User user = new User();
    user.setAge(99);
    //修改条件
    UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
    userUpdateWrapper
        .like("name", "h")
        .set("name", "老李头")//除了可以查询还可以使用set设置修改的字段
        .setSql(" email = '123@qq.com'");//可以有子查询
    int result = userMapper.update(user, userUpdateWrapper);
}
```

UPDATE user SET age=?, update_time=?, name=?, email = '123@qq.com' WHERE deleted=0 AND name LIKE ? 