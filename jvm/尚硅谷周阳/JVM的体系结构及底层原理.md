# JVM的体系结构及底层原理

## 1. JVM体系结构

首先，我们来了解 JVM 的位置，如下图，JVM 是运行在操作系统之上的，它与硬件没有直接交互。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200721212102560.png#pic_center)
接下来是 JVM 的整个体系结构图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200721212525209.png#pic_center)
然后，我们根据 JVM 体系结构图来阐述相关的知识点。

## 2. 类装载器 ClassLoader

**类装载器 ClassLoader** 是负责加载`class`文件的，将`class`文件字节码内容加载到内存中，并将这些内容转换成方法区中的运行时数据结构。`ClassLoader`只负责文件的加载，至于它是否可运行，则由`Execution Engine`决定。

在这里需要区分一下`class`与`Class`。小写的`class`，是指编译 Java 代码后所生成的以`.class`为后缀名的字节码文件。而大写的`Class`，是 JDK 提供的`java.lang.Class`，可以理解为封装类的模板。多用于反射场景，例如 JDBC 中的加载驱动，`Class.forName("com.mysql.jdbc.Driver");`

接下来我们来观察下图，`Car.class`字节码文件被`ClassLoader`类装载器加载并初始化，在方法区中生成了一个`Car Class`的类模板，而我们平时所用到的实例化，就是在这个类模板的基础上，形成了一个个实例，即`car1`，`car2`。反过来讲，我们可以对某个具体的实例进行`getClass()`操作，就可以得到该实例的类模板，即`Car Class`。再接着，我们对这个类模板进行`getClassLoader()`操作，就可以得到这个类模板是由哪个类装载器进行加载的。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200721213332630.png#pic_center)

> Tip：扩展一下，JVM并不仅仅只是通过检查文件后缀名是否是`.class`来判断是否加载，最主要的是通过`class`文件中特定的文件标示，即下图`test.class`文件中的`cafe babe`。![在这里插入图片描述](https://img-blog.csdnimg.cn/20200721214933626.png#pic_center)

### 2.1 有哪些类装载器？

**（1）虚拟机自带的加载器**

1. 启动类加载器（Bootstrap），也叫根加载器，加载`%JAVAHOME%/jre/lib/rt.jar`。
2. 扩展类加载器（Extension），加载`%JAVAHOME%/jre/lib/ext/*.jar`，例如`javax.swing`包。
3. 应用程序类加载器（AppClassLoader），也叫系统类加载器，加载`%CLASSPATH%`的所有类。

**（2）用户自定义的加载器** ：用户可以自定义类的加载方式，但必须是`Java.lang.ClassLoader`的子类。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200722134405251.png#pic_center)

### 2.2 双亲委派和沙箱安全

接下来，我们通过下面代码来观察这几个类加载器。首先，我们先看自定义的`MyObject`，首先通过`getClassLoader()`获取到的是`AppClassLoader`，然后`getParent()`得到`ExtClassLoader`，再`getParent()`竟然是`null`？可能大家会有疑惑，不应该是Bootstrap加载器么？**这是因为，`BootstrapClassLoader`是使用C++语言编写的，Java在加载的时候就成了null。**

我们再来看Java自带的`Object`，通过`getClassLoader()`获取到的加载器直接就是`BootstrapClassLoader`，如果要想`getParent()`的话，因为是null值，所以就会报`java.lang.NullPointerException`空指针异常。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200722131041957.png#pic_center)

> 输出中，`sun.misc.Launcher`是JVM相关调用的入口程序。

那为什么会出现这个情况呢？这就需要我们来了解类加载器的加载顺序和机制了，即**双亲委派**和**沙箱安全** 。

**（1）双亲委派**，当一个类收到了类加载请求，它首先不会尝试自己去加载这个类，而是把这个请求委派给父类去完成，因此所有的加载请求都应该传送到启动类加载器中，只有当父类加载器反馈自己无法完成这个请求的时候（在它的加载路径下没有找到所需加载的`Class`），子类加载器才会尝试自己去加载。

采用双亲委派的一个好处是，比如加载位于`rt.jar`包中的类`java.lang.Object`，不管是哪个加载器加载这个类，最终都是委派给顶层的启动类加载器进行加载，确保哪怕使用了不同的类加载器，最终得到的都是同样一个`Object`对象。

**（2）沙箱安全机制**，是基于双亲委派机制上采取的一种JVM的自我保护机制，假设你要写一个`java.lang.String`的类，由于双亲委派机制的原理，此请求会先交给`BootStrapClassLoader`试图进行加载，但是`BootStrapClassLoader`在加载类时首先通过包和类名查找`rt.jar`中有没有该类，有则优先加载`rt.jar`包中的类，**因此就保证了java的运行机制不会被破坏，确保你的代码不会污染到Java的源码**。

**所以，类加载器的加载顺序如下：**

1. 当`AppClassLoader`加载一个`class`时，它首先不会自己去尝试加载这个类，而是把类加载请求委派给父类加载器`ExtClassLoader`去完成。
2. 当`ExtClassLoader`加载一个`class`时，它首先也不会自己去尝试加载这个类，而是把类加载请求委派给`BootStrapClassLoader`去完成。
3. 如果`BootStrapClassLoader`加载失败（例如在`$JAVA_HOME/jre/lib`里未查找到该`class`），会使用`ExtClassLoader`来尝试加载。
4. 若`ExtClassLoader`也加载失败，则会使用`AppClassLoader`来加载，如果`AppClassLoader`也加载失败，则会报出异常`ClassNotFoundException`。

> Tip：`rt.jar`是什么？做了哪些事？这些暂且不提，那你有没有想过，为什么可以在idea这些开发工具中可以直接去使用String、ArrayList、甚至一些JDK提供的类和方法？观察下面动图就可以知道，原来这些都在`rt.jar`中定义好了，且直接被启动类加载器进行加载了。
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200722130535525.gif)

## 3. 本地方法栈 Native Method Stack

**本地方法接口（Native Interface）**，其作用是融合不同的编程语言为 Java 所用，它的初衷是用来融合 C/C++ 程序的，Java 诞生的时候是 C/C++ 流行时期，要想立足，就得调用 C/C++ 程序，于是 Java
就在内存中专门开辟了一块区域处理标记为 native 的代码。

而**本地方法栈（Native Method Stack）**，就是在一个 Stack 中登记这些 native 方法，然后在执行引擎`Execution Engine`执行时加载本地方法库`native libraies`。

接下来，我们通过下图的多线程部分源码来理解什么是`native`方法。首先我们观察`start()`的源码，发现它其实并没有做什么复杂的操作，只是单纯的调用了`start0()`这个方法，然后我们去观察`start0()`的源码，发现它只是一个使用了`native`关键字修饰的一个方法（`private native void start0();`），但**只有声明却没有具体的实现！**。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200722151311154.png#pic_center)
为什么？我们都知道`Thread`是`Class`关键字修饰的类（`class Thread implements Runnable`），而不是接口。一般来说，类中的方法都要有定义和实现，接口里面才有方法的定义声明。这就是`native`方法的独特之处，说白了，被`native`关键字修饰的方法，基本上和我们，甚至和 Java 都没啥关系了，因为它要去调用底层操作系统或者第三方语言的库函数，所以我们不需要去考虑它具体是如何实现的。

## 4. 程序计数器 Program Counter Register

**程序计数器（Program Counter Register）**，也叫PC寄存器。每个线程启动的时候，都会创建一个PC寄存器。PC寄存器里保存当前正在执行的JVM指令的地址。 每一个线程都有它自己的PC寄存器，也是该线程启动时创建的。

简单来说，PC寄存器就是保存下一条将要执行的指令地址的寄存器，其内容总是指向下一条将被执行指令的地址，这里的地址可以是一个本地指针，也可以是在方法区中相对应于该方法起始指令的偏移量。

每个线程都有一个程序计数器，是线程私有的,就是一个指针，指向方法区中的方法字节码（用来存储指向下一条指令的地址,也即将要执行的指令代码），由执行引擎`Execution Engine`读取下一条指令，是一个非常小的内存空间，几乎可以忽略不记。

这块内存区域很小，它是当前线程所执行的字节码的行号指示器，字节码解释器通过改变这个计数器的值来选取下一条需要执行的字节码指令。

PC寄存器一般用以完成分支、循环、跳转、异常处理、线程恢复等基础功能。不会发生内存溢出（OutOfMemory，OOM）错误。

> 如果执行的是一个`native`方法，那这个计数器是空的。

## 5. 方法区 Method Area

**方法区（Method Area）**，是供各线程共享的运行时内存区域，它**存储了每一个类的结构信息**。例如运行时常量池（Runtime Constant Pool）、字段和方法数据、构造函数和普通方法的字节码内容。

上面说的是规范（定义的一种抽象概念），实际在不同虚拟机里实现是不一样的，最典型的就是永久代（PermGen space）和元空间（Meta space）。

> 实例变量存在堆内存中，和方法区无关。

## 6. 栈 Stack

**栈管运行，堆管存储！** 请熟读并默写全文。

**栈（Stack）**，也叫栈内存，主管Java程序的运行，在线程创建时创建。其生命期是跟随线程的生命期，是线程私有的，线程结束栈内存也就是释放。

对于栈来说，不存在垃圾回收的问题，只要线程一结束该栈就Over。

### 6.1 栈存储什么数据？

栈主要存储**8种基本类型的变量、对象的引用变量、以及实例方法。**

这里引出一个名词，**栈帧**，什么是栈帧？（java方法=**栈帧**）
每个方法执行的同时都会创建一个栈帧，用于存储局部变量表、操作数栈、动态链接、方法出口等信息，每个方法从调用直至执行完毕的过程，就对应着一个栈帧在虚拟机中入栈到出栈的过程。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725155759288.png#pic_center)
简单来说，**栈帧**对应一个方法的执行和结束，是方法执行过程的内存模型。

其中，栈帧主要保持了3类数据：

1. **本地变量（Local Variables）**：输入参数和输出参数，以及方法内的变量。
2. **栈操作（Operand Stack）**：记录出栈、入栈的操作。
3. **栈帧数据（Frame Data）**：包括类文件、方法等。

> 栈的大小是根据JVM有关，一般在256K~756K之间，约等于1Mb左右。

### 6.2 栈的运行原理

观察下图，在java中，`test()`和`main()`都是方法，而在栈中，称为栈帧。在栈中，`main()`都是第一个入栈的。
栈的顺序为：`main()`入栈 --> `test()`入栈 --> `test()`出栈 --> `main()`出栈。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725150917476.png#pic_center)
根据代码和运行结果可以知道，`main()`想要出栈，则必须`test()`先出栈。那么怎么证明呢？观察下面代码，我们在`test()`方法中添加了一条语句`Thread.sleep(Integer.MAX_VALUE);`，来让`test()`无法进行出栈操作，进而导致`main()`也无法出栈。运行代码发现，运行结果如我们所料，程序一直停留在`test()`入栈，无法进行其他操作。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725150109203.png#pic_center)
我们接着观察下图，在图中一个栈中有两个栈帧，分别是`Stack Frame1`和`Stack Frame2`，对应方法1和方法2。其中`Stack Frame2`是最先被调用的方法2，所以它先入栈。然后方法2又调用了方法1，所以`Stack Frame1`处于栈顶位置。执行完毕后，依次弹出`Stack Frame1`和`Stack Frame2`，然后线程结束，栈释放。
所以，每执行一个方法都会产生一个栈帧，并保存到栈的顶部，顶部的栈帧就是当前所执行的方法，该方法执行完毕后会自动出栈。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725153138242.png#pic_center)
总结如下，栈中的数据都是以栈帧（Stack Frame）的格式存在，栈帧是一个内存区块，是一个数据集，是一个有关方法（Method）和运行期数据的数据集，当一个方法A被调用时就产生了一个栈帧F1，并被压入到栈中，方法A中又调用了方法B，于是产生栈帧F2也被压入栈中，方法B又调用方法C，于是产生栈帧F3也被压入栈中······执行完毕后，**遵循“先进后出，后进先出”的原则**，先弹出F3栈帧，再弹出F2栈帧，再弹出F1栈帧。

### 6.3 栈溢出 StackOverflowError

大家肯定对栈溢出耳熟，那栈溢出是怎么产生的呢？

请看下面代码，`test()`方法里面又调用了`test()`方法，即自己调用自己，也叫递归。
同时，栈是一个内存块，它是有大小长度的，而我们观察代码发现，只要代码一运行，`test()`方法就会一直进行入栈操作，而没有出栈操作，结果肯定会超出栈的大小，进而造成栈溢出错误，即`java.lang.StackOverflowError`。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725155127479.png#pic_center)
所以说，老哥们，禁止套娃，禁止套娃，禁止套娃！!!!∑(ﾟДﾟノ)ノ

> `java.lang.StackOverflowError`是错误，不是异常！证明如下 ：
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725155549596.png)

## 7. 栈、堆、方法区的交互关系

栈、堆、方法区三者的关系如下图，其中`reference`是引用类型。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725160245331.png#pic_center)
举个栗子，比如`MyObject myObject = new MyObject();`，等号左边`MyObject myObject`的`myObject`就是引用，在Java栈里面。等号右边的`new MyObject()`，`new`出来的`MyObject`实例对象在堆里面。简单来说，就是Java栈中的引用`myObject`指向了堆中的`MyObject`实例对象。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725161551868.png#pic_center)

## 8. 堆 Heap

### 8.1 堆体系结构

一个JVM实例只存在一个堆内存，堆内存的大小是可以调节的。类加载器读取了类文件之后，需要把类、方法、常量变量放到堆内存中，保持所以引用类型的真实信息，方便执行器执行。

其中，堆内存分为3个部分：

1. Young Generation Space，新生区、新生代
2. Tenure Generation Space，老年区、老年代
3. Permanent Space，永久区、元空间

**Java7之前，堆结构图如下，而Java8则只将永久区变成了元空间。**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200725163244622.png#pic_center)
**总结一下，堆内存在逻辑上分为新生+养老+元空间，而堆内存在物理上分为新生+养老。**

### 8.2 对象在堆中的生命周期

那么如何直观的了解对象在堆中的生命周期呢？

（1）首先，新生区是类的诞生、成长、消亡的区域。一个类在这里被创建并使用，最后被垃圾回收器收集，结束生命。

（2）其次，所有的类都是在`Eden Space`被`new`出来的。而当`Eden Space`的空间用完时，程序又需要创建对象，JVM的垃圾回收器则会将`Eden Space`中不再被其他对象所引用的对象进行销毁，也就是垃圾回收（Minor GC）。此时的GC可以认为是**轻量级GC**。

（3）然后将`Eden Space`中剩余的未被回收的对象，移动到`Survivor 0 Space`，以此往复，直到`Survivor 0 Space`也满了的时候，再对`Survivor 0 Space`进行垃圾回收，剩余的未被回收的对象，则再移动到`Survivor 1 Space`。`Survivor 1 Space`也满了的话，再移动至`Tenure Generation Space`。

（4）最后，如果`Tenure Generation Space`也满了的话，那么这个时候就会被垃圾回收（Major GC or Full GC）并将该区的内存清理。此时的GC可以认为是**重量级GC**。如果`Tenure Generation Space`被GC垃圾回收之后，依旧处于占满状态的话，就会产生我们场景的`OOM`异常，即`OutOfMemoryError`。

### 8.3 Minor GC的过程

`Survivor 0 Space`，幸存者0区，也叫`from`区；
`Survivor 1 Space`，幸存者1区，也叫`to`区。



其中，`from`区和`to`区的区分不是固定的，是互相交换的，意思是说，在每次GC之后，两者会进行交换，谁空谁就是`to`区。

不明白？没关系，接着往下看。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807105031435.png#pic_center)
**（1）`Eden Space`、`from`复制到`to`，年龄+1。**
首先，当`Eden Space`满时，会触发第一次GC，把还活着的对象拷贝到`from`区。而当`Eden Space`再次触发GC时，会扫描`Eden Space`和`from`，对这两个区进行垃圾回收，经过此次回收后依旧存活的对象，则直接复制到`to`区（如果对象的年龄已经达到老年的标准，则移动至老年代区），同时把这些对象的年龄+1。

**（2）清空`Eden Space`、`from`**
然后，清空`Eden Space`和`from`中的对象，此时的`from`是空的。

**（3）`from`和`to`互换**
最后，`from`和`to`进行互换，原`from`成为下一次GC时的`to`，原`to`成为下一次GC时的`from`。部分对象会在`from`和`to`中来回进行交换复制，如果交换15次（由JVM参数`MaxTenuringThreshold`决定，默认15），最终依旧存活的对象就会移动至老年代。

总结一句话，**GC之后有交换，谁空谁是`to`**。

> 这样也是为了保证内存中没有碎片，所以`Survivor 0 Space`和`Survivor 1 Space`有一个要是空的。

### 8.4 HotSpot虚拟机的内存管理

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807112107588.png#pic_center)

> 不同对象的生命周期不同，其中98%的对象都是临时对象，即这些对象的生命周期大多只存在于Eden区。

实际而言，方法区（`Method Area`）和堆一样，是各个线程共享的内存区域，它用于存储虚拟机加载的：类信息+普通常量+静态常量+编译器编译后的代码等等。**虽然JVM规范将方法区描述为堆的一个逻辑部分，但它却还有一个别名叫做`Non-Heap`（非堆内存），目的就是要和堆区分开。**

对于HotSpot虚拟机而言，很多开发者习惯将方法区称为 “永久代（`Permanent Gen`）” 。但严格来说两者是不同的，或者说只是使用永久代来实现方法区而已，永久代是方法区（可以理解为一个接口`interface`）的一个实现，JDK1.7的版本中，已经将原本放在永久代的字符串常量池移走。（字符串常量池，JDK1.6在方法区，JDK1.7在堆，JDK1.8在元空间。）
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807112937832.png#pic_center)

> 如果没有明确指明，Java虚拟机的名字就叫做`HotSpot`。
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807114729427.png)

### 8.5 永久区

永久区是一个常驻内存区域，用于存放JDK自身所携带的`Class`，`Interface`的元数据（也就是上面文章提到的`rt.jar`等），也就是说它存储的是运行环境必须的类信息，被装载进此区域的数据是不会被垃圾回收器回收掉的，关闭JVM才会释放此区域所占用的内存。
**（1）JDK1.7**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807121320925.png#pic_center)
**（2）JDK1.8**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807121358976.png#pic_center)
在JDK1.8中，永久代已经被移除，被一个称为**元空间**的区域所取代。元空间的本质和永久代类似。

元空间与永久代之间最大的区别在于： **永久带使用的JVM的堆内存，但是java8以后的元空间并不在虚拟机中而是使用本机物理内存。**

因此，默认情况下，元空间的大小仅受本地内存限制。
类的元数据放入`native memory`，字符串池和类的静态变量放入Java堆中，这样可以加载多少类的元数据就不再由`MaxPermSize`控制, 而由系统的实际可用空间来控制。

### 8.6 堆参数调优

在进行堆参数调优前，我们可以通过下面的代码来获取虚拟机的相关内存信息。

```java
public class JVMMemory {
    public static void main(String[] args) {
        // 返回 Java 虚拟机试图使用的最大内存量
        long maxMemory = Runtime.getRuntime().maxMemory();
        System.out.println("MAX_MEMORY = " + maxMemory + "（字节）、" + (maxMemory / (double) 1024 / 1024) + "MB");
        // 返回 Java 虚拟机中的内存总量
        long totalMemory = Runtime.getRuntime().totalMemory();
        System.out.println("TOTAL_MEMORY = " + totalMemory + "（字节）、" + (totalMemory / (double) 1024 / 1024) + "MB");
    }
}
12345678910
```

运行结果如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807135332693.png#pic_center)
有人就有疑问了，这个`3607.5MB`和`243.5MB`是怎么算出来的？看下图就明白了，虚拟机最大内存为物理内存的1/4，而初始分配的内存为物理内存的1/64。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807124758835.png)
IDEA中如何配置JVM内存参数？在【Run】->【Edit Configuration…】->【VM options】中，输入参数`-Xms1024m -Xmx1024m -XX:+PrintGCDetails`，然后保存退出。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020080714041120.png#pic_center)
运行结果如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807141114216.png#pic_center)

> **JVM的初始内存和最大内存一般怎么配？**
> 答：初始内存和最大内存一定是一样大，理由是避免GC和应用程序争抢内存，进而导致内存忽高忽低产生停顿。

### 8.7 堆溢出 OutOfMemoryError

现在我们来演示一下`OOM`，首先把堆内存调成10M后，再一直new对象，导致Full GC也无法处理，直至撑爆堆内存，进而导致`OOM`堆溢出错误，程序及结果如下：

```java
import java.util.Random;
public class OOMTest {
    public static void main(String[] args) {
        String str = "Atlantis";
        while (true) {
            // 每执行下面语句，会在堆里创建新的对象
            str += str + new Random().nextInt(88888888) + new Random().nextInt(999999999);
        }
    }
}
12345678910
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807143709937.png#pic_center)

> 如果出现`java.lang.OutOfMemoryError: Java heap space`异常，说明Java虚拟机的堆内存不够，造成堆内存溢出。原因有两点：
> ①Java虚拟机的堆内存设置太小，可以通过参数`-Xms`和`-Xmx`来调整。
> ②代码中创建了大量对象，并且长时间不能被GC回收（存在被引用）。

## 9. GC（Java Garbage Collection）

### 9.1 GC垃圾收集机制

对于GC垃圾收集机制，我们需要记住以下几点：

1. 次数上频繁收集Young区。
2. 次数上较少收集Old区。
3. 基本不动元空间。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020080716523497.png#pic_center)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807154231426.png#pic_center)
JVM在进行GC时，并非每次都对上面三个内存区域一起回收的，大部分时候回收的都是指新生代。
因此GC按照回收的区域又分了两种类型，一种是普通GC（minor GC），一种是全局GC（major GC or Full GC）

**Minor GC和Full GC的区别：**
**（1）普通GC（minor GC）**：只针对新生代区域的GC，指发生在新生代的垃圾收集动作，因为大多数Java对象存活率都不高，所以Minor GC非常频繁，一般回收速度也比较快。
**（2）全局GC（major GC or Full GC）**：指发生在老年代的垃圾收集动作，出现了Major GC，经常会伴随至少一次的Minor GC（但并不是绝对的）。Major GC的速度一般要比Minor GC慢上10倍以上

### 9.2 GC日志信息详解

通过上面`OOM`案例，是不是觉得那一大片的日志信息看不懂？懵逼？没事，通过下图你就知道如何阅读GC日志信息。

**（1）YGC相关参数：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807151938212.png#pic_center)
**（2）FGC相关参数：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200807153518837.png#pic_center)

### 9.3 GC四大算法

1. 复制算法（Copying）
2. 标记清除（Mark-Sweep）
3. 标记压缩（Mark-Compact）
4. 分代收集算法

具体内容请跳转至：[GC四大算法详解](https://blog.csdn.net/q961250375/article/details/107859902)