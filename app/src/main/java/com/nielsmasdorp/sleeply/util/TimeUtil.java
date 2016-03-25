package com.nielsmasdorp.sleeply.util;

import java.util.concurrent.TimeUnit;

/**
 * Created by niels on 25-3-16.
 */
public class TimeUtil {

    /**
     * How many milliseconds should the sleep timer last
     *
     * @param option for timer
     * @return milliseconds for timer
     */
    public static int calculateMs(int option) {
        switch (option) {
            case 0:
                return 0;
            case 1:
                return (int) TimeUnit.SECONDS.toMillis(15);
            case 2:
                return (int) TimeUnit.MINUTES.toMillis(20);
            case 3:
                return (int) TimeUnit.MINUTES.toMillis(30);
            case 4:
                return (int) TimeUnit.MINUTES.toMillis(40);
            case 5:
                return (int) TimeUnit.MINUTES.toMillis(50);
            case 6:
                return (int) TimeUnit.HOURS.toMillis(1);
            case 7:
                return (int) TimeUnit.HOURS.toMillis(2);
            case 8:
                return (int) TimeUnit.HOURS.toMillis(3);
            default:
                return 0;
        }
    }
}
