package test;

public class A {
    public static void main(String[] args) {
        Integer a = 100;
        Integer b = 100;
        Integer c = 200;
        Integer d = 200;
        System.out.println(a == b);
        System.out.println(c == d);
        String a1 = "a";
        String aa = new String("a");
        System.out.println(a1 == aa);
    }
}
