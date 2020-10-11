package com.atguigu.juc.threadpool;


import java.util.concurrent.*;

/**
 * 线程池不允许使用 Executor 的方式创建，需要使用 ThreadPoolExecutor
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        ExecutorService threadPool = new ThreadPoolExecutor(
                2,
                5,
                2L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());// 出场默认
    }
}
