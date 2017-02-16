package com.terapeutica.smartfit.smartfit;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.terapeutica.smartfit.manager.Manager;
import com.terapeutica.smartfit.manager.ManagerListener;

import java.util.Timer;
import java.util.TimerTask;

public class SmartFitManager extends Service {
    String TAG = "SmartFitManager";

    Context context;

    Manager manager;

    public SmartFitManager() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        context = getApplicationContext();
        manager = new Manager(context, managerListener);

        IntentFilter serverFilter = new IntentFilter("smartfitserver2manager");
        serverFilter.addAction("smartfit.BAND_COMMAND");
        registerReceiver(serverReceiver, serverFilter);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
            }
        },30000, 10000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStart");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        unregisterReceiver(serverReceiver);
    }

    public void writeServer(String data, int socketId){
        Intent intent = new Intent("smartfitmanager2server");
        intent.setAction("smartfit.SERVER_WRITE");
        intent.putExtra("data", data);
        intent.putExtra("socketid", socketId);
        sendBroadcast(intent);
    }

    ManagerListener managerListener = new ManagerListener() {

        @Override
        public void onWriteServer(String data, int socketId) {
            writeServer(data, socketId);
        }
    };

    BroadcastReceiver serverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v(TAG, "onServerReceiver");

            if(intent.getAction().equals("smartfit.BAND_COMMAND")) {
                String command = intent.getStringExtra("command");
                int socketid = intent.getIntExtra("socketid", 0);
                manager.exec(command, socketid);
            }
        }
    };
}
