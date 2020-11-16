# Optional类

## 概念

Optional类是一个容器类，代表一个值存在或者不存在，原来null表示一个值不存在，现在Optional可以更好的表达这个概念，并且可以规避空指针异常

## 常用方法

- map(Function f)：如果有值对其处理，返回处理后的Optional，否则返回Optional.empty()

- flatMap(Function mapper)：与map类似，要求返回值必须是Optional

  

##### 1. 创建Optional类对象的方法：

 **Optional.of(T t) : 创建一个 Optional 实例， t必须非空；**
 Optional.empty() : 创建一个空的 Optional 实例
 **Optional.ofNullable(T t)： t可以为null**

##### 2. 判断Optional容器中是否包含对象：

 **boolean isPresent() : 判断是否包含对象**
 void ifPresent(Consumer<? super T> consumer) ： 如果有值，就执行Consumer
接口的实现代码，并且该值会作为参数传给它。

##### 3. 获取Optional容器的对象：

 T get(): 如果调用对象包含值，返回该值，否则抛异常
 **T orElse(T other) ： 如果有值则将其返回，否则返回指定的other对象。**
 T orElseGet(Supplier<? extends T> other) ： 如果有值则将其返回，否则返回由
Supplier接口实现提供的对象。
 T orElseThrow(Supplier<? extends X> exceptionSupplier) ： 如果有值则将其返
回，否则抛出由Supplier接口实现提供的异常。  