package string_equals;

import java.util.HashSet;
import java.util.Set;

/**
 * equals和等等的区别
 *
 * @author: 陌溪
 * @create: 2020-04-03-8:50
 */
public class EqualsDemo {

    static class Person {
        private String name;

        public Person(String name) {
            this.name = name;
        }
    }
    public static void main(String[] args) {
        String s1 = new String("abc");
        String s2 = new String("abc");

        /**
         * false（==：如果是new出来的对象，比较的时候永远是false）
         * true：（字符串中的equals被重写过，比较的是值）
         * 1：（HashSet底层是HashMap，HashMap内部是调用equals 和 HashCode，但是String内部的HashCode和equals也被复写）
         */
        System.out.println(s1 == s2);
        System.out.println(s1.equals(s2));
        Set<String> set1 = new HashSet<>();
        set1.add(s1);
        set1.add(s2);
        System.out.println(set1.size());


        System.out.println("==============");

        String s3 = "cbd";
        String s4 = "cbd";

        /**
         * true（我们通过这种方式创建的会放在一个字符串常量池中，相同的字符串，会指向常量池中同一个对象，因此他们的地址是一样的）
         * true（字符串中的equals被重写过，比较的是值）
         * 1
         */
        System.out.println(s3 == s4);
        System.out.println(s3.equals(s4));
        Set<String> set3 = new HashSet<>();
        set3.add(s3);
        set3.add(s3);
        System.out.println(set3.size());

        System.out.println("==============");

        Person person1 = new Person("abc");
        Person person2 = new Person("abc");
        /**
         * false（==：如果是new出来的对象，比较的时候永远是false）
         * false（Person中的equals没有被重写，相当于等等）
         * 2
         */
        System.out.println(person1 == person2);
        System.out.println(person1.equals(person2));
        Set<Person> set2 = new HashSet<>();
        set2.add(person1);
        set2.add(person2);
        System.out.println(set2.size());
    }
}