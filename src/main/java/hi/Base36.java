package hi;

import java.util.Date;

public class Base36 {
    public static long decode(final String value) {
        return Long.parseLong(value, 36);
    }

    public static String encode(final long value) {
        return Long.toString(value, 36);
    }

    public static void main(String[] args) {
        System.out.println(new Date().getTime());
        System.out.println(encode(new Date().getTime()));
    }
}
