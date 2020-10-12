package com.atguigu.juc.collection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * java.util.ConcurrentModificationException  并发修改异常
 * add() 方法加synchronized
 * 解决方法：
 *      1. vector 项目于中不允许使用
 *      2. Collections.synchronizedList(new ArrayList<>()) 项目中不用
 *      3.
 *
 */
public class NotSafeDemo {

    public static void main(String[] args) {
        Map<String,String>  map = new HashMap<>(); // 底层 数组 + 链表 + 红黑树   工作中很少用
        Map<String,String > map2 = new HashMap<>(1000);

        Map<String,String>  map3 = new ConcurrentHashMap<>();

        for (int i = 0; i < 10; i++) {

            new Thread(()->{
                map3.put(Thread.currentThread().getName(),UUID.randomUUID().toString().substring(0,8));
                System.out.println(map3);
            },"线程"+i).start();


        }

    }

    public static void set(String[] args) {
        Set<String> set = new CopyOnWriteArraySet<>();

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread(()->{

                set.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(set);
            },"线程"+i).start();


        }

    }



    public static void list(String[] args) {
//        List<String> list = new ArrayList<>();
// 改善
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
        List<String> list =new CopyOnWriteArrayList();

        for (int i = 0; i < 10; i++) {

            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(list);
            },"线程"+i).start();


        }
    }
}
