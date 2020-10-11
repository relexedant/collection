package com.atguigu.juc.ticket;


@FunctionalInterface
interface Foo{
    public void sayHello();



    default int div(int x, int y){
        return x/y;
    }

    public static int mv(int x,int y){
        return x*y;
    }
}


/**
 * 接口中 只有一个方法的接口  称为 函数式接口
 * 函数式接口 可以添加 @FunctionInterface
 *
 *
 *  1. lamda表达式十秒钟入门
 *      拷贝小括号
 *      写死右箭头
 *      落地大括号
 *  2.  @FunctionInterface
 *
 *  3. java8以后 接口中可以有由default修饰的实现  函数式接口中的 default 修饰的方法 不会导致 一个函数式接口 成为非函数式接口
 *
 *  4. 静态方法实现 接口总的静态实现方法不影响函数式接口
 */
public class LamdaExpression {
    public static void main(String[] args) {
        Foo foo = ()->{System.out.println("hello");};
        foo.sayHello();
        System.out.println(Foo.mv(3, 6));
    }
}
