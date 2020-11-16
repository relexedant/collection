package staticproxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestDynamic {
    public static void main(String[] args) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = {UserService.class};

        UserService o = (UserService) Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                try {
                    System.out.println("前置方法");

                    Object invoke = method.invoke(new UserServiceImpl(), args);
                    System.out.println(method.getName());
                    int i = 1/0;

                    System.out.println("后置方法");
                    return invoke;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常方法");
                }
                return null;
            }
        });
        String gagagagag = o.findAll("gagagagag");
        System.out.println(gagagagag);
    }
}
