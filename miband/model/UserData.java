package com.terapeutica.smartfit.miband.model;


import com.terapeutica.smartfit.miband.model.StatusBand;

import java.sql.Time;

/**
 * Created by daco on 18/01/2017.
 */
public class UserData {

    int heartRate = 0;
    int steps = 0;
    int battery = 0;
    int rssi = 0;
    boolean connected = false;
    boolean started = false;
    String address = "";
    String alias = "user";
    Time time = new Time(System.currentTimeMillis());
    int status = StatusBand.BAND_DISCONNECTED;

    public UserData(String alias) {
        this.alias = alias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String statusToString(){
        switch (status){
            case (StatusBand.BAND_DISCONNECTED):        return "Disconnected";
            case (StatusBand.BAND_CONNECTING):          return "Connecting";
            case (StatusBand.BAND_CONNECTED):           return "Connected";
            case (StatusBand.BAND_STARTED):             return "Started";
            case (StatusBand.BAND_STOPPED):             return "Stopped";

            case (StatusBand.BAND_USER_FAIL):           return "User Fail";
            case (StatusBand.BAND_USER_NULL):           return "User Null";
            case (StatusBand.BAND_BLE_FAIL):            return "Bluetooth Fail";

            case (StatusBand.BAND_ALARM):               return "Alarmed";
            case (StatusBand.BAND_VIBRATE_ERROR):       return "Vibrate Error";

            case (StatusBand.BAND_UNSIGNAL_COUNTER):    return "Unsignal Data";
            case (StatusBand.BAND_UNSIGNAL_RSSI):       return "Unsignal Rssi";
            default:                                    return "Unknown";
        }
    }

    public String toString(){
        return  "time:" + time.toString() +
                ",alias:" + alias +
                ",address:" + address +
                ",heartRate:" + String.valueOf(heartRate) +
                ",steps:" + String.valueOf(steps) +
                ",rssi:" + String.valueOf(rssi) +
                ",battery:" + String.valueOf(battery) +
                ",status:" + statusToString();
    }
}
