package com.terapeutica.smartfit.miband.model;

/**
 * Created by daco on 23/01/2017.
 */
public class StatusBand {

    public static final int BAND_DISCONNECTED = 0;
    public static final int BAND_CONNECTING = 1;
    public static final int BAND_CONNECTED = 2;
    public static final int BAND_STARTED = 3;
    public static final int BAND_STOPPED = 4;

    public static final int BAND_USER_FAIL = 10;
    public static final int BAND_USER_NULL = 11;
    public static final int BAND_BLE_FAIL = 12;

    public static final int BAND_ALARM = 20;
    public static final int BAND_VIBRATE_ERROR = 21;

    public static final int BAND_UNSIGNAL_RSSI = 30;
    public static final int BAND_UNSIGNAL_COUNTER = 31;

}
