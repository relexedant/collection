package com.threadlocal;

public class MyDemo03 {
    // 变量
    private  String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static void main(String[] args) { 
        MyDemo03 myDemo01 = new MyDemo03();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                synchronized (MyDemo03.class) {
                    myDemo01.setContent(Thread.currentThread().getName() + "的数据");
                    System.out.println("-----------------------------------------");
                    System.out.println(Thread.currentThread().getName() + "\t  " + myDemo01.getContent());
                }
            }, String.valueOf(i)).start();
        }
    }
}