package string_equals;

public class EqualsDemo2 {
    public static void main(String[] args) {
        String str1 = "abc";
        String str2 = new String("abc");
        String str3 = "abc";
        String str4 =  "xxx";
        String str5 = "abc" + "xxx";
        String str6 = str3 + str4;
        String str7 = "abcxxx";

        System.out.println("str1 == str2：" + (str1 == str2));
        System.out.println("str1.equals(str2)：" + (str1.equals(str2)));
        System.out.println("str1 == str5：" + (str1 == str5));
        System.out.println("str1 == str6：" + (str1 == str6));
        System.out.println("str5 == str6：" + (str5 == str6));
        System.out.println("str5 == str7：" + (str5 == str7));
        System.out.println("str6 == str7：" + (str6 == str7));

        System.out.println("str5.equals(str6)：" + (str5.equals(str6)));
        System.out.println("str1 == str6.intern()：" + (str1 == str6.intern()));
        System.out.println("str1 == str2.intern()：" + (str1 == str2.intern()));
        System.out.println("xxx".intern());
    }

}
