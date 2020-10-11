package com.atguigu.juc.lock;

import java.util.concurrent.TimeUnit;

class Phone{
    public static synchronized void sendEmail(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("send email=====");
    }


    public  synchronized void sendMsg(){
        System.out.println("send sendMsg=====");
    }

    public  void hello(){
        System.out.println("send hello=====");
    }
}

/**
 * 多线程 8 锁
 * 正常访问，邮件和短信谁先打印？                   ==>邮件       synchronized
 * 邮件暂停4S,邮件和短信谁先打印？                    ==>邮件
 * 新增一个普通方法hello ，邮件和hello谁先打印？     ==>hello
 * 两部手机 先打印邮件还是短信？                  ==>短信
 * 两个静态方法，一部手机，先邮件和短信谁先打印？  ==>邮件
 * 两个静态方法，二部手机，先邮件和短信谁先打印？ ==>邮件
 * 1个静态同步方法，1个普通同步方法，一部手机，先邮件和短信谁先打印？       ==>msg
 * 1个静态同步方法，1个普通同步方法，2部手机，先邮件和短信谁先打印？       ==>msg
 */

public class Lock8 {
    public static void main(String[] args) {
        Phone phone = new Phone();
        Phone phone2 = new Phone();
        new Thread(()->{
            phone.sendEmail();
        },"A").start();
        new Thread(()->{
            phone2.sendMsg();
        },"B").start();


    }
}
