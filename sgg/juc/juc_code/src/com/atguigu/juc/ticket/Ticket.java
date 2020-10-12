package com.atguigu.juc.ticket;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 资源类
class Ti {
     private int number = 30;


     // synchronized 可以达成效果 会将整个方法中的代码快锁住  不能指定代码加锁

    // 使用Lock 进行优化
    private Lock lock = new ReentrantLock();
    public    void saleTicket(){

        lock.lock();
        try{
            if (number > 0){
                // 可以将常用的代码抽取出快捷键
                System.out.println(Thread.currentThread().getName() +"\t 还剩余"+ number-- );
            }

        }finally {
            lock.unlock();
        }



     }

}


/**
 * 多线程企业级编程 + 模板
 *
 * 1. 在高内聚低耦合的前提下  线程   操作（对外暴露的方法）   资源类
 *      1.创建共享资源对象
 *      2.创建操作资源对象的方法
 * 2.lamda表达式
 */
public class Ticket {
    public static void main(String[] args) {
        Ti t = new Ti();


        new Thread(()->{for (int i = 1;i<=40;i++){t.saleTicket();}},"C").start();
        new Thread(()->{for (int i = 1;i<=40;i++){t.saleTicket();}},"D").start();
        new Thread(()->{for (int i = 1;i<=40;i++){t.saleTicket();}},"E").start();

//        // 工作中常用方案  Thread(Runnable ,String)
//      new  Thread(new Runnable(){
//
//            @Override
//            public void run() {
//                for (int i = 1;i<=40;i++){
//                    t.saleTicket();
//                }
//            }
//        },"AA").start();
//
//      new Thread(new Runnable(){
//
//          @Override
//          public void run() {
//              for (int i = 1;i<=40;i++){
//                  t.saleTicket();
//              }
//          }
//      },"B").start();





    }
}
