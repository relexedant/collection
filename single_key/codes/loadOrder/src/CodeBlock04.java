/**
 * 代码块
 *
 * @author: 陌溪
 * @create: 2020-04-03-9:51
 */
class Father {
    {
        System.out.println("我是父亲代码块");//4
    }
    public Father() {
        System.out.println("我是父亲构造");//5
    }
    static {
        System.out.println("我是父亲静态代码块");//2
    }
}

class Son extends Father{
    public Son() {
        System.out.println("我是儿子构造");//7
    }
    {
        System.out.println("我是儿子代码块");//6
    }

    static {
        System.out.println("我是儿子静态代码块");//3
    }
}

public class CodeBlock04 {

    public static void main(String[] args) {

        System.out.println("我是主类======");//1
        new Son();
        System.out.println("======");//8
        new Son();
        System.out.println("======");
        new Father();
    }
}