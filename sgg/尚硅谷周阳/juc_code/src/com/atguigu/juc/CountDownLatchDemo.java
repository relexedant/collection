package com.atguigu.juc;


import java.util.concurrent.CountDownLatch;

/**
 * 代码的顺序执行
 * 减少计数
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 0; i < 6; i++) {
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+":\t离开了教室");
                countDownLatch.countDown();
            },String.valueOf(i)).start();
        }

        // 截至countDownLatch 成为0
        countDownLatch.await();
        System.out.println(Thread.currentThread().getName()+"\t 最后一个离开");
    }
}
