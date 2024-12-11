package chr.wgx.drill;

public class Drill {
    public static void main(String[] args) {
        useString("abcd");
        useString(null);
    }

    private static void useString(String s) {
        System.out.println(s);
    }
}
