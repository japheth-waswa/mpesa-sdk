package base;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
class HelpersTest {

    @Test
    void formatDateTimeToInstant() {
        System.out.println(Helpers.formatDateTimeToInstant("20191219102115"));
        System.out.println(Helpers.formatDateTimeToInstant("20180223054112"));
        System.out.println(Helpers.formatDateTimeToInstant("20191122063845"));
        String[] x = "254708374149 - John Doe - ".split("-");
        System.out.println(Arrays.toString(x));
//String[] xx = (String[]) Arrays.stream(x).map(String::trim).filter(s->!s.isBlank()).toArray();
String[] xx = Arrays.stream("254708374149 - John Doe - ".split("-")).map(String::trim).filter(s->!s.isBlank()).toArray(String[]::new);
        System.out.println(Arrays.toString(xx));
    }
}