package com.terapeutica.smartfit.utils;

/**
 * Created by daco on 19/01/2017.
 */
public class TimerDelay {

    public static void delay(int msec){
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
