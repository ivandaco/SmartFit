package com.terapeutica.smartfit.miband;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import com.terapeutica.smartfit.miband.listeners.HeartRateNotifyListener;
import com.terapeutica.smartfit.miband.listeners.NotifyListener;
import com.terapeutica.smartfit.miband.model.BatteryInfo;
import com.terapeutica.smartfit.miband.model.StatusBand;
import com.terapeutica.smartfit.miband.model.UserData;
import com.terapeutica.smartfit.miband.model.UserInfo;
import com.terapeutica.smartfit.miband.model.VibrationMode;

import java.sql.Time;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by daco on 23/01/2017.
 */
public class Band {

    String TAG;
    UserData userData = new UserData("User");

    int counterFail = 0;
    int counterRssi = 0;
    int counterTask = 0;

    MiBand band;
    UserInfo userInfo;
    Context context;

    BandListener listener;

    Timer timer = new Timer();

    boolean alarmed = false;

    public boolean isConnected() {
        return userData.isConnected();
    }

    public boolean isStarted() {
        return userData.isStarted();
    }

    public String getAddress(){
        return userData.getAddress();
    }

    public Band(Context context, String address, BandListener listener) {
        this.context = context;
        this.listener = listener;
        userData.setStatus( StatusBand.BAND_DISCONNECTED );
        band = new MiBand(context);
        userData.setAddress( address );
        TAG = "Band " + userData.getAddress();
        band.setStateChangedListener( stateListener );
    }

    public void on(){
        if(!userData.isConnected())
            connect();
    }

    public void off(){

        if(userData.isStarted())
            stop();

        if(userData.isConnected())
            disconnect();
    }

    public void connect() {
        Log.v(TAG, "Try connect");

        band.connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(userData.getAddress()), new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.v(TAG, "connect");
                counterFail=0;
                counterRssi=0;
                userData.setStatus(StatusBand.BAND_CONNECTING);
                updateUserInfo();

                if(listener != null)
                    listener.onConnected();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.v(TAG, "Error connect " + msg);
                disconnect();
            }
        });
    }

    public void disconnect() {
        Log.v(TAG, "Disconnect");

        if(userData.isStarted())
            stop();

        if(userData.isConnected())
            band.disableHeartRateScanListener();

        userData.setConnected(false);

        if(userData.getStatus() != StatusBand.BAND_USER_FAIL && userData.getStatus() != StatusBand.BAND_BLE_FAIL && userData.getStatus() != StatusBand.BAND_USER_NULL)
            userData.setStatus(StatusBand.BAND_DISCONNECTED);

        band.disconnect();

        if(listener != null)
            listener.onDisconnected();
    }

    private void vibrate(){
        Log.v("Vibrar", "Update !!!");
        band.startVibration(VibrationMode.VIBRATION_WITH_LED, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.v(TAG, "Vibrar");
                alarmed=false;
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.v(TAG, "Error Vibrar " + msg);
                userData.setStatus( StatusBand.BAND_VIBRATE_ERROR );
            }
        });
    }

    private void updateUserInfo(){
        Log.v(TAG, "Load user");

        if(userInfo == null) {
            userData.setStatus( StatusBand.BAND_USER_NULL );
            return;
        }

        band.setUserInfo(userInfo, userData.getAddress(), new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.v(TAG, "User Info:" + userInfo.toString() + ",data:" + Arrays.toString(userInfo.getBytes(userData.getAddress())));
                userData.setConnected(true);
                userData.setStatus( StatusBand.BAND_CONNECTED );
                band.enableHeartRateScanListener( heartRateNotifyListener );
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.v(TAG, "Error User info " + msg);
                userData.setStatus( StatusBand.BAND_USER_FAIL );
                disconnect();
            }
        });
    }

    private void updateBatteryInfo(){
        band.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BatteryInfo batteryInfo = (BatteryInfo)data;
                userData.setBattery(batteryInfo.getLevel());
//                Log.v(TAG, "Battery " + String.valueOf(userData.getBattery()) + "%");

            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.v(TAG, "Error Battery " + msg);
            }
        });
    }

    private void updateStepsInfo() {
        band.getSteps(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                userData.setSteps((int)data);
//                Log.v(TAG, "Steps: " + String.valueOf(userData.getSteps()));
                counterFail=0;
            }

            @Override
            public void onFail(int errorCode, final String msg) {
                Log.v(TAG, "Error Steps " + msg);
                counterFail++;
                if(counterFail>3) {
                    userData.setStatus( StatusBand.BAND_UNSIGNAL_COUNTER );
                    disconnect();
                }
            }
        });
    }

    private void updateRssiInfo() {
        band.readRssi(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                userData.setRssi((int)data);
//                Log.v(TAG, "Rssi: " + String.valueOf(userData.getRssi()));
                if(userData.getRssi() <= -93){
                    counterRssi++;
                    if(counterRssi>3)
                        userData.setStatus( StatusBand.BAND_UNSIGNAL_RSSI );
                }else{
                    counterRssi=0;
                }
            }

            @Override
            public void onFail(int errorCode, final String msg) {
                Log.v(TAG, "Error Rssi " + msg);
            }
        });
    }

    private HeartRateNotifyListener heartRateNotifyListener = new HeartRateNotifyListener() {
        @Override
        public void onNotify(int heartRate) {
//            Log.v(TAG, "HR: " + String.valueOf(hr));
            if(userData.isStarted())
                userData.setHeartRate(heartRate);
        }
    };

    private NotifyListener stateListener = new NotifyListener() {
        @Override
        public void onNotify(byte[] data) {
            int status = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
            Log.v(TAG, "Status BLE: " + String.valueOf(status));

            if(status == 133) {
                userData.setStatus( StatusBand.BAND_DISCONNECTED );
                disconnect();
            }

            if(status != 0 && status != 133){
                userData.setStatus( StatusBand.BAND_BLE_FAIL );
                disconnect();
            }
        }
    };

    public void startTask(){

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(counterTask%3==0)
                    userData.setStatus( StatusBand.BAND_STARTED );
            }
        }, 0);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateRssiInfo();
            }
        }, 100);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateStepsInfo();
            }
        }, 200);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(counterTask%30==0)
                    updateBatteryInfo();
            }
        }, 300);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(counterTask%5==0)
                    band.startHeartRateScan();
            }
        }, 400);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(alarmed)
                    vibrate();
            }
        }, 500);

        counterTask++;
    }

    public void setUser(String alias, String gender, int age, int height, int weight){
        Log.v(TAG, "User");

        if(gender.equals("F"))          userInfo = new UserInfo(alias.hashCode(), UserInfo.GENDER_FEMALE, age, height, weight, alias, 0);
        else if(gender.equals("M"))     userInfo = new UserInfo(alias.hashCode(), UserInfo.GENDER_MALE, age, height, weight, alias, 0);
        else                            userInfo = new UserInfo(alias.hashCode(), UserInfo.GENDER_OTHER, age, height, weight, alias, 0);

        Log.v(TAG, "Set User " + userInfo.toString() );
        userData.setAlias( userInfo.getAlias() );
    }

    public UserData getUserData(){
//        Log.v(TAG, "User Data");
        userData.setTime( new Time(System.currentTimeMillis()));
        return userData;
    }

    public void start(){
        Log.v(TAG, "Start");

        if(!userData.isConnected())
            return;

        if(userData.isStarted())
            return;

        userData.setStarted(true);

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(userData.isStarted())
                    startTask();
            }
        }, 0, 1000);
    }

    public void stop(){
        Log.v(TAG, "Stop");
        userData.setStarted(false);

        if(userData.getStatus() != StatusBand.BAND_USER_FAIL && userData.getStatus() != StatusBand.BAND_BLE_FAIL && userData.getStatus() != StatusBand.BAND_USER_NULL)
            userData.setStatus( StatusBand.BAND_STOPPED );

        timer.cancel();
        timer.purge();
    }

    public void alarm(){
        Log.v(TAG, "Alarm");
        userData.setStatus( StatusBand.BAND_ALARM );

        if(userData.isStarted())
            alarmed = true;
        else
            vibrate();
    }

}
