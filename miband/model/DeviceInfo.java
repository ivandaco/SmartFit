package com.terapeutica.smartfit.miband.model;

import com.terapeutica.smartfit.utils.CheckSums;


public class DeviceInfo {


    public String deviceId;
    public int profileVersion;
    /**
     * Mi Band firmware version identifier
     */
    public int fwVersion;
    public int hwVersion;
    public int feature;
    public int appearance;
    /**
     * Heart rate firmware version identifier
     */
    public int fw2Version;
    private boolean test1AHRMode;

    private DeviceInfo() {

    }

    private static boolean isChecksumCorrect(byte[] data) {
        int crc8 = CheckSums.getCRC8(new byte[]{data[0], data[1], data[2], data[3], data[4], data[5], data[6]});
        return (data[7] & 255) == (crc8 ^ data[3] & 255);
    }

    public static DeviceInfo fromByteData(byte[] data) {

        DeviceInfo info = new DeviceInfo();

        if ((data.length == 16 || data.length == 20) && isChecksumCorrect(data)) {

            info.deviceId = String.format("%02X%02X%02X%02X%02X%02X%02X%02X", data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            info.profileVersion = getInt(data, 8);
            info.fwVersion = getInt(data, 12);
            info.hwVersion = data[6] & 255;
            info.appearance = data[5] & 255;
            info.feature = data[4] & 255;
            if (data.length == 20) {
                int s = 0;
                for (int i = 0; i < 4; ++i) {
                    s |= (data[16 + i] & 255) << i * 8;
                }
                info.fw2Version = s;
            } else {
                info.fw2Version = -1;
            }
        } else {
            info.deviceId = "crc error";
            info.profileVersion = -1;
            info.fwVersion = -1;
            info.hwVersion = -1;
            info.feature = -1;
            info.appearance = -1;
            info.fw2Version = -1;
        }

        return info;
    }

    public static int getInt(byte[] data, int from, int len) {
        int ret = 0;
        for (int i = 0; i < len; ++i) {
            ret |= (data[from + i] & 255) << i * 8;
        }
        return ret;
    }

    private static int getInt(byte[] data, int from) {
        return getInt(data, from, 4);
    }

    public int getFirmwareVersion() {
        return fwVersion;
    }

    public int getHeartrateFirmwareVersion() {
        if (test1AHRMode) {
            return fwVersion;
        }
        return fw2Version;
    }

    public void setTest1AHRMode(boolean enableTestMode) {
        test1AHRMode = enableTestMode;
    }

    public String toString() {
        return "DeviceInfo{" +
                "deviceId='" + deviceId + '\'' +
                ", profileVersion=" + profileVersion +
                ", fwVersion=" + fwVersion +
                ", hwVersion=" + hwVersion +
                ", feature=" + feature +
                ", appearance=" + appearance +
                ", fw2Version (hr)=" + fw2Version +
                '}';
    }
}
