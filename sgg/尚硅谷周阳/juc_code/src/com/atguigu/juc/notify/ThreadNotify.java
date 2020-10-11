package com.atguigu.juc.notify;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Notify {

    private int num = 0;

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void in() {
        lock.lock();

        try {
            while (num != 0) {
//                this.wait();
                condition.await();
            }
            num++;
            System.out.println(Thread.currentThread().getName() + ":" + num);
//            this.notifyAll();
            condition.signalAll();

        } catch (Exception e) {

        } finally {
            lock.unlock();
        }

    }

    public void de() {
        lock.lock();
        try {
            while (num == 0) {
                condition.await();
            }
            num--;
            System.out.println(Thread.currentThread().getName() + ":" + num);
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


//    synchronized void in() throws InterruptedException {
//        if (num != 0) {
//            this.wait();
//        }
//        num++;
//        System.out.println(Thread.currentThread().getName() + ":" + num);
//        this.notifyAll();
//    }
//
//    synchronized void de() throws InterruptedException {
//        if (num == 0) {
//            this.wait();
//        }
//        num++;
//        System.out.println(Thread.currentThread().getName() + ":" + num);
//        this.notifyAll();
//    }


}

/**
 * 1. 高内聚低耦合  线程操作资源类
 * 2. 判断、干活、通知
 * 3. 多线程交互中，必须防止多线程的虚假唤醒，也即（判断只能用while，不能使用if）  wait() notify（）是object的方法
 * wait()方法不能和if搭配使用，需要和while循环使用
 *
 * 使用 Lock 和 Condition替代 synchronized 和原来的线程方法
 */
public class ThreadNotify {
    public static void main(String[] args) {
        Notify notify = new Notify();

        for (int i = 0; i <20; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    notify.in();
                }
            }, "A").start();


            new Thread(()->notify.de(),"B").start();
            new Thread(()->notify.in(),"C").start();
            new Thread(()->notify.de(),"D").start();
        }


    }
}
