package com.atguigu.juc.orderCall;

import jdk.internal.org.objectweb.asm.commons.TryCatchBlockSorter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ShareResource {
    private int num = 1;//1a  2b  3c
    private Lock lock = new ReentrantLock();
    private Condition condition1 = lock.newCondition();
    private Condition condition2 = lock.newCondition();
    private Condition condition3 = lock.newCondition();

    public void print5() {
        lock.lock();

        try {

            // 1. 判断
            while (num != 1) {
                condition1.await();
            }
            // 2.干活
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + num);
            }
            // 3. 通知
            num = 2;
            condition2.signal();


        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    public void print10() {
        lock.lock();

        try {

            // 1. 判断
            while (num != 2) {
                condition2.await();
            }
            // 2.干活
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + num);
            }

            // 3. 通知
            num = 3;
            condition3.signal();

        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    public void print15() {
        lock.lock();

        try {

            // 1. 判断
            while (num != 3) {
                condition3.await();
            }
            // 2.干活
            for (int i = 0; i < 15; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + num);
            }
            // 3. 通知
            num = 1;
            condition1.signal();

        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }
}


/**
 * 多线程按指定顺序调用 a b c
 * <p>
 * a * 五次  b 十次  c 十五次   总共来10轮
 * <p>
 * 1. 高内聚低耦合情况下 此案成操作资源类
 * 2. 判断通知干活
 * 3. 多线程交互中，必须防止多线程的虚假唤醒
 * 4. 标志位
 */
public class ThreadOrder {


    public static void main(String[] args) {
        ShareResource shareResource = new ShareResource();


        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                shareResource.print5();
            }
        }, "A").start();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                shareResource.print10();
            }
        }, "b").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                shareResource.print15();
            }
        }, "c").start();
    }
}
