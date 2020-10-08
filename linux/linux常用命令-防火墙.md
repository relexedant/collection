## linux常用命令-防火墙

#### **1、查看firewall服务状态**

```
systemctl status firewalld
```

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9pbWFnZXMyMDE1LmNuYmxvZ3MuY29tL2Jsb2cvOTY0MTc1LzIwMTcwNy85NjQxNzUtMjAxNzA3MDQxMDQyNTkxNTktOTEzMjE4Nzc1LnBuZw?x-oss-process=image/format,png)

#### **2、查看firewall的状态**

```
firewall-cmd --state
```

 ![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9pbWFnZXMyMDE1LmNuYmxvZ3MuY29tL2Jsb2cvOTY0MTc1LzIwMTcwNy85NjQxNzUtMjAxNzA3MDQxMDQ0MjU3NjktNjk4ODQ0MDQxLnBuZw?x-oss-process=image/format,png)

#### **3、开启、\**重启、关闭、\**firewalld.service服务**

```
# 开启
service firewalld start
# 重启
service firewalld restart
# 关闭
service firewalld stop
```

#### **4、查看防火墙规则**

```
firewall-cmd --list-all 
```

![img](https://img-blog.csdnimg.cn/2019082711382141.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3p6X2xrdw==,size_16,color_FFFFFF,t_70)

#### **5、查询、开放、关闭端口**

 

```
# 查询端口是否开放
firewall-cmd --query-port=8080/tcp
# 开放80端口
firewall-cmd --permanent --add-port=80/tcp
# 移除端口
firewall-cmd --permanent --remove-port=8080/tcp
#重启防火墙(修改配置后要重启防火墙)
firewall-cmd --reload

# 参数解释
1、firwall-cmd：是Linux提供的操作firewall的一个工具；
2、--permanent：表示设置为持久；
3、--add-port：标识添加的端口；
```