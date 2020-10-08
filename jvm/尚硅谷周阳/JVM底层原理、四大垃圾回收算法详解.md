# JVM底层原理、四大垃圾回收算法详解

> - **转载请注明出处：**https://www.jianshu.com/p/9e6841a895b4

> **注意：**垃圾回收算法周阳老师讲的有错误，具体在p19，四大垃圾回收算法为复制算法、标记-整理算法、标记-清除算法、**分代收集算法**（不是引用计数算法）。这里感谢@[9c0bd0ceebfa](https://www.jianshu.com/u/9c0bd0ceebfa)指出。下文已经更正正确，请放心食用。

![img](https://upload-images.jianshu.io/upload_images/4070621-bba54463485506eb.png?imageMogr2/auto-orient/strip|imageView2/2/w/890/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-2dfbe0fa4266a276.png?imageMogr2/auto-orient/strip|imageView2/2/w/964/format/webp)

- 注意：我们平时说的栈是指的Java栈，native method stack 里面装的都是native方法。见下文

  ![img](https://upload-images.jianshu.io/upload_images/4070621-a3e9ac354336aa47.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - 方法区并不是存放方法的区域，其是存放类的描述信息(模板)的地方
>
> - Class loader只是负责class文件的加载，相当于快递员，这个“快递员”并不是只有一家，Class loader有多种
>
> - 加载之前是“小class”，加载之后就变成了“大Class”，这是安装java.lang.Class模板生成了一个实例。“大Class”就装载在方法区，模板实例化之后就得到n个相同的对象
>
> - JVM并不是通过检查文件后缀是不是
>
>   ```
>   .class
>   ```
>
>   来判断是否需要加载的，而是通过
>
>   文件开头的特定文件标志
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-91c98e0dcc20f0ed.png?imageMogr2/auto-orient/strip|imageView2/2/w/677/format/webp)
>
>   文件开头的特殊标识

![img](https://upload-images.jianshu.io/upload_images/4070621-026cc3734789ca8f.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - Class loader有多种，可以说三个，也可以说是四个（第四个为自己定义的加载器，继承 ClassLoader），系统自带的三个分别为：
>
> 1. 启动类加载器(Bootstrap) ，C++所写
> 2. 扩展类加载器(Extension) ，Java所写
> 3. 应用程序类加载器(AppClassLoader)。
>
> 我们自己new的时候创建的是应用程序类加载器(AppClassLoader)。



```csharp
import com.gmail.fxding2019.T;

public class  Test{
    //Test:查看类加载器
    public static void main(String[] args) {

        Object object = new Object();
        //查看是那个“ClassLoader”（快递员把Object加载进来的）
        System.out.println(object.getClass().getClassLoader());
        //查看Object的加载器的上一层
        // error Exception in thread "main" java.lang.NullPointerException（已经是祖先了）
        //System.out.println(object.getClass().getClassLoader().getParent());

        System.out.println();

        Test t = new Test();
        System.out.println(t.getClass().getClassLoader().getParent().getParent());
        System.out.println(t.getClass().getClassLoader().getParent());
        System.out.println(t.getClass().getClassLoader());
    }
}

/*
*output:
* null
* 
* null
* sun.misc.Launcher$ExtClassLoader@4554617c
* sun.misc.Launcher$AppClassLoader@18b4aac2
* */
```

> **注意：**
>
> - 如果是JDK自带的类(Object、String、ArrayList等)，其使用的加载器是Bootstrap加载器；如果自己写的类，使用的是AppClassLoader加载器；Extension加载器是负责将把java更新的程序包的类加载进行
> - 输出中，sun.misc.Launcher是JVM相关调用的入口程序
> - Java加载器个数为3+1。前三个是系统自带的，用户可以定制类的加载方式，通过继承Java. lang. ClassLoader

![img](https://upload-images.jianshu.io/upload_images/4070621-2cf08ab9b6da3c81.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - 双亲委派机制：“我爸是李刚，有事找我爹”。
>   例如：需要用一个A.java这个类，首先去顶部Bootstrap根加载器去找，找得到你就用，找不到再下降一层，去Extension加载器去找，找得到就用，找不到再将一层，去AppClassLoader加载器去找，找得到就用，找不到就会报"CLASS NOT FOUND EXCEPTION"。



```csharp
//测试加载器的加载顺序
package java.lang;

public class String {

    public static void main(String[] args) {

        System.out.println("hello world!");

    }
}

/*
* output:
* 错误: 在类 java.lang.String 中找不到 main 方法
* */
```

上面代码是为了测试加载器的顺序：首先加载的是Bootstrap加载器，由于JVM中有java.lang.String这个类，所以会首先加载这个类，而不是自己写的类，而这个类中并无main方法，所以会报“在类 java.lang.String 中找不到 main 方法”。

这个问题就涉及到，如果有两个相同的类，那么java到底会用哪一个？如果使用用户自己定义的java.lang.String，那么别使用这个类的程序会去全部出错，所以，为了保证用户写的源代码不污染java出厂自带的源代码，而提供了一种“双亲委派”机制，保证“沙箱安全”。即先找到先使用。

![img](https://upload-images.jianshu.io/upload_images/4070621-d493880d2c91e6b5.png?imageMogr2/auto-orient/strip|imageView2/2/w/891/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-e815940f813a7e28.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)



Thread类的start方法如下：



```csharp
public synchronized void start() {
        /**
         * This method is not invoked for the main method thread or "system"
         * group threads created/set up by the VM. Any new functionality added
         * to this method in the future may have to also be added to the VM.
         *
         * A zero status value corresponds to state "NEW".
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* Notify the group that this thread is about to be started
         * so that it can be added to the group's list of threads
         * and the group's unstarted count can be decremented. */
        group.add(this);

        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* do nothing. If start0 threw a Throwable then
                  it will be passed up the call stack */
            }
        }
    }

    private native void start0();
```

Thread类中竟然有一个只有声明没有实现的方法，并使用`native`关键字。用native表示，也此方法是系统级（底层操作系统或第三方C语言）的，而不是语言级的，java并不能对其进行操作。native方法装载在native method stack中。

![img](https://upload-images.jianshu.io/upload_images/4070621-eb47cd3689d8fd02.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

- 注意：native方法不归java管，所以计数器是空的

![img](https://upload-images.jianshu.io/upload_images/4070621-2dfbe0fa4266a276.png?imageMogr2/auto-orient/strip|imageView2/2/w/964/format/webp)

> 上面图中是亮色的地方有两个特点：
>
> - 1. 所有线程共享（灰色是线程私有）
> - 1. 亮色地方存在垃圾回收

![img](https://upload-images.jianshu.io/upload_images/4070621-656d85ebbb170bdd.png?imageMogr2/auto-orient/strip|imageView2/2/w/964/format/webp)

> **注意：**
>
> - 方法区：绝对不是放方法的地方，他是存储的每一个类的结构信息(比如static)
>
> - 永久代和元空间的解释：
>
>   方法区是一种规范，类似于接口定义的规范：
>
>   ```
>   List list = new ArrayList();
>   ```
>
>   把这种比喻用到方法区则有：
>
>   1. java 7中：`方法区 f = new 永久代();`
>   2. java 8中：`方法去 f = new 元空间();`

![img](https://upload-images.jianshu.io/upload_images/4070621-d0edbf860466dcb4.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - 栈管运行，堆管存储
> - 栈是线程私有，不存在垃圾回收
> - 栈帧的概念：java中的方法被扔进虚拟机的栈空间之后就成为“栈帧”，比如main方法，是程序的入口，被压栈之后就成为栈帧。

![img](https://upload-images.jianshu.io/upload_images/4070621-19b5b0443400e313.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-ad996fd40b4f0d2b.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-1b59709fae10a4a1.png?imageMogr2/auto-orient/strip|imageView2/2/w/919/format/webp)



```csharp
public class  Test{

    public static  void  m(){
        m();
    }

    public static void main(String[] args) {

        System.out.println("111");
        //Exception in thread "main" java.lang.StackOverflowError
        m();
        System.out.println("222");

    }
}

/*
*output:
* 111
* Exception in thread "main" java.lang.StackOverflowError
* */
```

> **注意：**
>
> - StackOverflowError是一个
>
>   “”错误
>
>   ，而不是
>
>   “异常”
>
>   。
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-5df3029ee7dff3d3.png?imageMogr2/auto-orient/strip|imageView2/2/w/438/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-5bcc19f55683b59d.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - HotSpot：如果没有明确指明，JDK的名字就叫HotSpot
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-3be86c30f479e979.png?imageMogr2/auto-orient/strip|imageView2/2/w/738/format/webp)
>
> - 元数据：描述数据的数据（即模板，也就是“大Class”）
>   上面的关系图的一个实例为下图：
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-1e9d4eb5deff6083.png?imageMogr2/auto-orient/strip|imageView2/2/w/753/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-a3d31ee101600821.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-bf558c3e60ac0e40.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - **Java 7**之前和图上一模一样，**Java 8**把**永久区**换成了**元空间**
> - **堆逻辑上由”新生+养老+元空间“三个部分组成，物理上由”新生+养老“两个部分组成**
> - 当执行`new Person()；`时，其实是new在新生区的伊甸园区，然后往下走，走到养老区，但是并未到元空间。

![img](https://upload-images.jianshu.io/upload_images/4070621-1926dd216ce9e26c.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - GC发生在伊甸园区，当对象快占满新生代时，就会发生YGC（Young GC，轻量级GC）操作，伊甸园区基本全部清空
> - 幸存者0区(S0)，别名“from区”。伊甸园区没有被YGC清空的对象将移至幸存者0区，幸存者1区别名“to 区”
> - 每次进行YGC操作，幸存的对象就会从伊甸园区移到幸存者0区，如果幸存者0区满了，就会继续往下移，如果经历数次YGC操作对象还没有消亡，最终会来到养老区
> - 如果到最后，养老区也满了，那么就对养老区进行FGC(Full GC，重GC)，对养老区进行清洗
> - 如果进行了多次FGC之后，还是无法腾出养老区的空间，就会报**OOM（out of Memory）**异常
> - **from区和to区位置和名分不是固定的，每次GC过后都会交换，GC交换后，谁空谁是to区**

![img](https://upload-images.jianshu.io/upload_images/4070621-45ec61c8f5d1c887.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - 整个堆分为新生区和养老区，新生区占整个堆的1/3，养老区占2/3。新生区又分为3份：伊甸园区：幸存者0区(from区):幸存者1区(to区) = 8:1:1
> - **每次从伊甸园区经过GC幸存的对象，年龄(代数)会+1**

![img](https://upload-images.jianshu.io/upload_images/4070621-df06084e11c11870.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-ede6cec97ade9a23.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意：**
>
> - 临时对象就是说明，其在伊甸园区生，也在伊甸园区死。
> - **堆逻辑上由”新生+养老+元空间“三个部分组成，物理上由”新生+养老“两个部分组成，元空间也叫方法区**
> - 永久代(方法区)几乎没有垃圾回收，里面存放的都是加载的rt.jar等，让你随时可用

![img](https://upload-images.jianshu.io/upload_images/4070621-fa575fe14562f74a.png?imageMogr2/auto-orient/strip|imageView2/2/w/969/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-cb6e0dffd5a7106b.png?imageMogr2/auto-orient/strip|imageView2/2/w/958/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-539c3eed7284a5bb.png?imageMogr2/auto-orient/strip|imageView2/2/w/973/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-db894c5dc43cc231.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

> **注意**
>
> - 上面的图展示的是物理上的堆，分为两块，新生区和养老区。
>
> - 堆的参数主要有两个：
>
>   ```
>   -Xms
>   ```
>
>   ，
>
>   ```
>   Xmx
>   ```
>
>   ：
>
>   1. `-Xms`堆的初始化的大小
>   2. `Xmx`堆的最大化
>
> - Young Gen(新生代)有一个参数`-Xmn`，这个参数可以调新生区和养老区的比例。但是，这个参数一般不调。
>
> - 永久代也有两个参数：`-XX:PermSize`，`-XX:MaxPermSize`，可以分别调永久带的初始值和最大值。**Java 8 后没有这两个参数啦**，因为Java 8后元空间不在虚拟机内啦，而是在本机物理内存中

![img](https://upload-images.jianshu.io/upload_images/4070621-dff0b5b2865e1067.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-8901fb1813c06fa5.png?imageMogr2/auto-orient/strip|imageView2/2/w/600/format/webp)



```csharp
//查看自己机器上的默认堆内存和最大堆内存
public class  Test{

    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());
        //返回 Java虚拟机试图使用的最大内存量。物理内存的1/4（-Xmx）
        long maxMemory = Runtime.getRuntime().maxMemory() ;
        //返回 Java虚拟机中的内存总量(初始值)。物理内存的1/64（-Xms）
        long totalMemory = Runtime.getRuntime().totalMemory() ;
        System.out.println("MAX_MEMORY =" + maxMemory +"(字节)、" + (maxMemory / (double)1024 / 1024) + "MB");
        System.out.println("DEFALUT_MEMORY = " + totalMemory + " (字节)、" + (totalMemory / (double)1024 / 1024) + "MB");

    }
}

/*
*   8
    MAX_MEMORY =1868038144(字节)、1781.5MB
    TOTAL_MEMORY = 126877696 (字节)、121.0MB
* */
```

> - **注意：**JVM参数调优，平时可以随便挑初始大小和最大大小，但是**实际工作中，初始大小和最大大小应该是一致的，原因是避免内存忽高忽低产生停顿**
>
> - IDEA 的JVM内存配置
>
>   1. 点击Run列表下的Edit Configuration
>
>      ![img](https://upload-images.jianshu.io/upload_images/4070621-35de395dfee6c3e2.png?imageMogr2/auto-orient/strip|imageView2/2/w/557/format/webp)
>
>   2. 在VM Options中输入以下参数:
>
>      ```
>      -Xms1024m -Xmx1024m -XX:+PrintGCDetails
>      ```
>
>      。
>
>      ![img](https://upload-images.jianshu.io/upload_images/4070621-3792f8476ec10953.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)
>
>   3. 运行程序查看结果
>
>      ![img](https://upload-images.jianshu.io/upload_images/4070621-8eaf64157a3b1ce1.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)
>
> - 把堆内存调成10M后，再一直new对象，导致Full GC也无法处理，直至撑爆堆内存，查看堆溢出错误(OOM)，程序及结果如下：
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-b7c7631f058cb3eb.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-58e8e307ddbeef5c.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-098f475b3ef94c31.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

> **GC收集日志信息详解**
>
> - 第一次进行YGC相关参数：
>   [PSYoungGen: 2008K->482K(2560K)] 2008K->782K(9728K), 0.0011440 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-f9fdd483c3983a8f.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)
>
> - 最后一次进行FGC相关参数：
>   [Full GC (Allocation Failure) [PSYoungGen: 0K->0K(2048K)] [ParOldGen: 4025K->4005K(7168K)] 4025K->4005K(9216K), [Metaspace: 3289K->3289K(1056768K)], 0.0082055 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-900f4e7a0826e16b.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)
>
>   ![img](https://upload-images.jianshu.io/upload_images/4070621-cc943f01079a4552.png?imageMogr2/auto-orient/strip|imageView2/2/w/474/format/webp)

> **面试题：GC是什么（分代收集算法）**
>
> - 次数上频繁收集Young区
> - 次数上较少收集Old区
> - 基本不动元空间
>
> **面试题：GC的四大算法（后有详解）**
>
> - 1.复制算法(Copying)
> - 1. 标记清除(Mark-Sweep)
> - 1. 标记压缩(Mark-Compact)
> - 1. 分代收集算法
>
> **面试题：下面程序中，有几个线程在运行**
>
> ![img](https://upload-images.jianshu.io/upload_images/4070621-8539dbb83f6556a0.png?imageMogr2/auto-orient/strip|imageView2/2/w/866/format/webp)
>
> Answer:有两个线程，一个是main线程，一个是后台的gc线程。

![img](https://upload-images.jianshu.io/upload_images/4070621-98170629a0965926.png?imageMogr2/auto-orient/strip|imageView2/2/w/1109/format/webp)

GC算法概述

> **知识点：**
>
> - JVM在进行GC时，并非每次都对上面三个内存区域一起回收的，大部分时候回收的都是指新生代。因此GC按照回收的区域又分了两种类型，一种是普通GC（minor GC or Young GC），一种是全局GC（major GC or Full GC）
> - Minor GC和Full GC的区别
>   　　普通GC（minor GC）：只针对新生代区域的GC,指发生在新生代的垃圾收集动作，因为大多数Java对象存活率都不高，所以Minor GC非常频繁，一般回收速度也比较快。
>   　　全局GC（major GC or Full GC）：指发生在老年代的垃圾收集动作，出现了Major GC，经常会伴随至少一次的Minor GC（但并不是绝对的）。Major GC的速度一般要比Minor GC慢上10倍以上 (因为养老区比较大，占堆的2/3)

------

### GC四大算法详解：

首先看一下判断Java中对象存活的算法：

- **1.引用计数算法**：引用计数器算法是给每个对象设置一个计数器，当有地方引用这个对象的时候，计数器+1，当引用失效的时候，计数器-1，当计数器为0的时候，JVM就认为对象不再被使用，是“垃圾”了。
  引用计数器实现简单，效率高；但是不能解决循环引用问问题（A对象引用B对象，B对象又引用A对象，但是A,B对象已不被任何其他对象引用），同时每次计数器的增加和减少都带来了很多额外的开销，所以在JDK1.1之后，这个算法已经不再使用了。

- 2.根搜索方法：

  根搜索方法是通过一些“GCRoots”对象作为起点，从这些节点开始往下搜索，搜索通过的路径成为引用链（ReferenceChain），当一个对象没有被GCRoots的引用链连接的时候，说明这个对象是不可用的。

  GCRoots对象包括：

  \1. 虚拟机栈（栈帧中的本地变量表）中的引用的对象。

  \2. 方法区域中的类静态属性引用的对象。

  \3. 方法区域中常量引用的对象。

  \4. 方法栈中JNI（Native方法）的引用的对象。

  ![img](https://upload-images.jianshu.io/upload_images/4070621-8ec56f8a9d20f8e4.png?imageMogr2/auto-orient/strip|imageView2/2/w/465/format/webp)

#### 1. 复制算法(Copying)

年轻代中使用的是Minor GC（YGC），这种GC算法采用的是复制算法(Copying)。

Minor GC会把Eden中的所有活的对象都移到Survivor区域中，如果Survivor区中放不下，那么剩下的活的对象就被移到Old generation中，也即一旦收集后，Eden是就变成空的了。

当对象在 Eden ( 包括一个 Survivor 区域，这里假设是 from 区域 ) 出生后，在经过一次 Minor GC 后，如果对象还存活，并且能够被另外一块 Survivor 区域所容纳( 上面已经假设为 from 区域，这里应为 to 区域，即 to 区域有足够的内存空间来存储 Eden 和 from 区域中存活的对象 )，则使用复制算法将这些仍然还存活的对象复制到另外一块 Survivor 区域 ( 即 to 区域 ) 中，然后清理所使用过的 Eden 以及 Survivor 区域 ( 即 from 区域 )，并且将这些对象的年龄设置为1，以后对象在 Survivor 区每熬过一次 Minor GC，就将对象的年龄 + 1，当对象的年龄达到某个值时 ( 默认是 15 岁，通过-XX:MaxTenuringThreshold 来设定参数)，这些对象就会成为老年代。

-XX:MaxTenuringThreshold — 设置对象在新生代中存活的次数

![img](https://upload-images.jianshu.io/upload_images/4070621-65895cbe7327a999.png?imageMogr2/auto-orient/strip|imageView2/2/w/929/format/webp)

年轻代中的GC,主要是复制算法（Copying）。 HotSpot JVM把年轻代分为了三部分：1个Eden区和2个Survivor区（分别叫from和to）。默认比例为8:1:1,一般情况下，新创建的对象都会被分配到Eden区(一些大对象特殊处理),这些对象经过第一次Minor GC后，如果仍然存活，将会被移到Survivor区。对象在Survivor区中每熬过一次Minor GC，年龄就会增加1岁，当它的年龄增加到一定程度时，就会被移动到年老代中。因为年轻代中的对象基本都是朝生夕死的(90%以上)，**所以在年轻代的垃圾回收算法使用的是复制算法**，复制算法的基本思想就是将内存分为两块，每次只用其中一块(from)，当这一块内存用完，就将还活着的对象复制到另外一块上面。**复制算法的优点是不会产生内存碎片，缺点是耗费空间**。

在GC开始的时候，对象只会存在于Eden区和名为“From”的Survivor区，Survivor区“To”是空的。紧接着进行GC，Eden区中所有存活的对象都会被复制到“To”，而在“From”区中，仍存活的对象会根据他们的年龄值来决定去向。年龄达到一定值(年龄阈值，可以通过-XX:MaxTenuringThreshold来设置)的对象会被移动到年老代中，没有达到阈值的对象会被复制到“To”区域。**经过这次GC后，Eden区和From区已经被清空。这个时候，“From”和“To”会交换他们的角色，也就是新的“To”就是上次GC前的“From”，新的“From”就是上次GC前的“To”**。不管怎样，都会保证名为To的Survivor区域是空的。Minor GC会一直重复这样的过程，直到“To”区被填满，“To”区被填满之后，会将所有对象移动到年老代中。

![img](https://upload-images.jianshu.io/upload_images/4070621-6c6b1a262845b208.png?imageMogr2/auto-orient/strip|imageView2/2/w/1069/format/webp)

因为Eden区对象一般存活率较低，一般的，使用两块10%的内存作为空闲和活动区间，而另外80%的内存，则是用来给新建对象分配内存的。一旦发生GC，将10%的from活动区间与另外80%中存活的eden对象转移到10%的to空闲区间，接下来，将之前90%的内存全部释放，以此类推。

![img](https://upload-images.jianshu.io/upload_images/4070621-1db29c74a72b69e9.gif?imageMogr2/auto-orient/strip|imageView2/2/w/415/format/webp)

蜜汁动画：看不懂请忽略

上面动画中，Area空闲代表to，Area激活代表from，绿色代表不被回收的，红色代表被回收的。

**复制算法它的缺点也是相当明显的:**

- 1. 它浪费了一半的内存，这太要命了。
- 1. 如果对象的存活率很高，我们可以极端一点，假设是100%存活，那么我们需要将所有对象都复制一遍，并将所有引用地址重置一遍。复制这一工作所花费的时间，在对象存活率达到一定程度时，将会变的不可忽视。 所以从以上描述不难看出，复制算法要想使用，最起码对象的存活率要非常低才行，而且最重要的是，我们必须要克服50%内存的浪费。

#### 2 .标记清除(Mark-Sweep)

**复制算法的缺点就是费空间，其是用在年轻代的，老年代一般是由标记清除或者是标记清除与标记整理的混合实现。**

![img](https://upload-images.jianshu.io/upload_images/4070621-f33c19fb1b50e432.png?imageMogr2/auto-orient/strip|imageView2/2/w/983/format/webp)



![img](https://upload-images.jianshu.io/upload_images/4070621-048d4544ed0c75a9.png?imageMogr2/auto-orient/strip|imageView2/2/w/827/format/webp)

用通俗的话解释一下标记清除算法，就是当程序运行期间，若可以使用的内存被耗尽的时候，GC线程就会被触发并将程序暂停，随后将要回收的对象标记一遍，最终统一回收这些对象，完成标记清理工作接下来便让应用程序恢复运行。

主要进行两项工作，第一项则是标记，第二项则是清除。

- 标记：从引用根节点开始标记遍历所有的GC Roots， 先标记出要回收的对象。
- 清除：遍历整个堆，把标记的对象清除。

**缺点：此算法需要暂停整个应用，会产生内存碎片**

![img](https://upload-images.jianshu.io/upload_images/4070621-6cf07565eed0fb2f.gif?imageMogr2/auto-orient/strip|imageView2/2/w/413/format/webp)

标记清除算法动态版

**标记清除算法小结：**

- 1、首先，它的缺点就是效率比较低（递归与全堆对象遍历），而且在进行GC的时候，需要停止应用程序，这会导致用户体验非常差劲
- 2、其次，主要的缺点则是这种方式清理出来的空闲内存是不连续的，这点不难理解，我们的死亡对象都是随即的出现在内存的各个角落的，现在把它们清除之后，内存的布局自然会乱七八糟。而为了应付这一点，JVM就不得不维持一个内存的空闲列表，这又是一种开销。而且在分配数组对象的时候，寻找连续的内存空间会不太好找。

#### 3. 标记压缩(Mark-Compact)

标记压缩(Mark-Compact)又叫标记清除压缩(Mark-Sweep-Compact)，或者标记清除整理算法。老年代一般是由**标记清除**或者是**标记清除与标记整理的混合**实现

![img](https://upload-images.jianshu.io/upload_images/4070621-2833156bab33fba9.png?imageMogr2/auto-orient/strip|imageView2/2/w/884/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-c535423e8c9ad06b.png?imageMogr2/auto-orient/strip|imageView2/2/w/752/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-cf3df690e1ca5521.png?imageMogr2/auto-orient/strip|imageView2/2/w/860/format/webp)

![img](https://upload-images.jianshu.io/upload_images/4070621-fdc8c57e8e175c3a.gif?imageMogr2/auto-orient/strip|imageView2/2/w/407/format/webp)

标记清除整理动态版

#### 4. 分代收集算法

当前商业虚拟机都是采用分代收集算法，它根据对象存活周期的不同将内存划分为几块，一般是把Java堆分为新生代和老年代，然后根据各个年代的特点采用最适当的收集算法，在新生代中，每次垃圾收集都发现有大批对象死去，只有少量存活，就选用复制算法，而老年代因为对象存活率高，没有额外空间对它进行分配担保，就必须使用“标记清理”或者“标记整理”算法来进行回收。

![img](https://upload-images.jianshu.io/upload_images/4070621-053eba368ec45995.png?imageMogr2/auto-orient/strip|imageView2/2/w/714/format/webp)

分代收集

图的左半部分是未回收前的内存区域，右半部分是回收后的内存区域。

> **面试题：四种算法那个好**
> Answer：没有那个算法是能一次性解决所有问题的，因为JVM垃圾回收使用的是**分代收集算法**，没有最好的算法，只有根据每一代他的垃圾回收的特性用对应的算法。**新生代使用复制算法，老年代使用标记清除和标记整理算法。**没有最好的垃圾回收机制，只有最合适的。

> **面试题：请说出各个垃圾回收算法的优缺点**
>
> - **内存效率：**复制算法>标记清除算法>标记整理算法（此处的效率只是简单的对比时间复杂度，实际情况不一定如此）。
> - **内存整齐度：**复制算法=标记整理算法>标记清除算法。
> - **内存利用率：**标记整理算法=标记清除算法>复制算法。
>
> 可以看出，效率上来说，复制算法是当之无愧的老大，但是却浪费了太多内存，而**为了尽量兼顾上面所提到的三个指标，标记/整理算法相对来说更平滑一些**，但效率上依然不尽如人意，它比复制算法多了一个标记的阶段，又比标记/清除多了一个整理内存的过程
>
> 难道就没有一种最优算法吗？Java 9 之后出现了**G1垃圾回收器（使用分代收集）**，能够解决以上问题，有兴趣参考[这篇文章](https://www.jianshu.com/p/ba415aa2330b)。

------

#### 总结：

- ##### 年轻代(Young Gen)

年轻代特点是区域相对老年代较小，对像存活率低。

这种情况复制算法的回收整理，速度是最快的。复制算法的效率只和当前存活对像大小有关，因而很适用于年轻代的回收。而复制算法内存利用率不高的问题，通过hotspot中的两个survivor的设计得到缓解。

- ##### 老年代(Tenure Gen)

老年代的特点是区域较大，对像存活率高。

这种情况，存在大量存活率高的对像，复制算法明显变得不合适。一般是由标记清除或者是标记清除与标记整理的混合实现。

Mark阶段的开销与存活对像的数量成正比，这点上说来，对于老年代，标记清除或者标记整理有一些不符，但可以通过多核/线程利用，对并发、并行的形式提标记效率。

Sweep阶段的开销与所管理区域的大小形正相关，但Sweep“就地处决”的特点，回收的过程没有对像的移动。使其相对其它有对像移动步骤的回收算法，仍然是效率最好的。但是需要解决内存碎片问题。

Compact阶段的开销与存活对像的数据成开比，如上一条所描述，对于大量对像的移动是很大开销的，做为老年代的第一选择并不合适。

基于上面的考虑，老年代一般是由标记清除或者是标记清除与标记整理的混合实现。以hotspot中的CMS回收器为例，CMS是基于Mark-Sweep实现的，对于对像的回收效率很高，而对于碎片问题，CMS采用基于Mark-Compact算法的Serial Old回收器做为补偿措施：当内存回收不佳（碎片导致的Concurrent Mode Failure时），将采用Serial Old执行Full GC以达到对老年代内存的整理。



> - **参考：**[尚硅谷周阳视频及课件](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.bilibili.com%2Fvideo%2Fav75769290%3Fp%3D1)
> - **资料提取：**[https://pan.baidu.com/s/1w-M3S8777iR4oekw7S3crA](https://links.jianshu.com/go?to=https%3A%2F%2Fpan.baidu.com%2Fs%2F1w-M3S8777iR4oekw7S3crA) 提取码：6ea8

