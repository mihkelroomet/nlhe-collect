package javafx.ryhmat88_2;

import java.util.concurrent.TimeUnit;

public class Üld {
    // magab etteantud arvu millisekundeid
    public static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException("Sleep interrupted", e);
        }
    }
}
