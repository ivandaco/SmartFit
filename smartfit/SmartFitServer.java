package com.terapeutica.smartfit.smartfit;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.terapeutica.smartfit.server.Server;
import com.terapeutica.smartfit.server.ServerListener;
import com.terapeutica.smartfit.utils.TimerDelay;

import java.net.Socket;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class SmartFitServer extends Service {
    String TAG = "SmartFitServer";

    Server server;
    Context context;
    int port = 7002;

    public SmartFitServer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStart");
        server.setMaxTimeSocketConnection( 5000 );
        server.listen();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");

        context = getApplicationContext();

        WifiManager wifi = (WifiManager)context.getSystemService(context.WIFI_SERVICE);

        if(!wifi.isWifiEnabled())
            wifi.setWifiEnabled(true);

        server = new Server(port, serverListener);

        IntentFilter managerFilter = new IntentFilter("smartfitmanager2server");
        managerFilter.addAction("smartfit.SERVER_WRITE");
        registerReceiver(managerReceiver, managerFilter);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            return;
        if(!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Runtime.getRuntime().gc();
            }
        },30000, 10000);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");

        if(server.isListening())
            server.close();
    }

    BroadcastReceiver managerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("smartfit.SERVER_WRITE")){
                String data = intent.getStringExtra("data");
                int socketId = intent.getIntExtra("socketid", 0);
                server.write(data, socketId);
            }
        }
    };

    ServerListener serverListener = new ServerListener() {

        @Override
        public void onSuccess(int port) {
            Log.v(TAG, "Server connected on port " + port);
        }

        @Override
        public void onFail(String msg) {
            Log.v(TAG, "Error " + msg);
            stopSelf();
        }

        @Override
        public void onSuccessSocket(int socketId) {
            Log.v(TAG, "New Socket #" + socketId);
        }

        @Override
        public void onTimeoutSocket(int socketId) {
            Log.v(TAG, "Timeout Socket #" + socketId);
        }

        @Override
        public void onFailSocket(String msg, int socketId) {
            Log.v(TAG, "Error Socket#" + socketId + " : " + msg);
        }

        @Override
        public void onCloseSocket(int socketId) {
            Log.v(TAG, "Close Socket #" + socketId);
            server.removeSocket(socketId);
        }

        @Override
        public void onAccept(Socket socket) {
            Log.v(TAG, "Accept Socket");
            server.addSocket(socket);
        }

        @Override
        public void onRead(String command, int socketId) {
            exec( command, socketId);
        }
    };

    public void exec(String command, int socketId){

        String[] args = command.split(" ");

        if(!verifyCommand( args )) {
            server.write("Invalid command", socketId );
            return;
        }

        String action = args[0];

        if(action.equals("band")){
            Log.v(TAG, "band command " + command);
            Intent intent = new Intent("smartfitserver2manager");
            intent.setAction("smartfit.BAND_COMMAND");
            intent.putExtra("command", command);
            intent.putExtra("socketid", socketId);
            sendBroadcast(intent);
            return;
        }

        if(action.equals("resetble")){
            Log.v(TAG, "Ble On/Off");

            server.write("Reset Bluetooth LE device", socketId);

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null)
                return;

            mBluetoothAdapter.disable();
            TimerDelay.delay(500);

            mBluetoothAdapter.enable();
            TimerDelay.delay(500);
            return;
        }

        if(action.equals("scan")){
            Log.v(TAG, "Scan");

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> devs =  mBluetoothAdapter.getBondedDevices();

            server.write("Scan BLE devices", socketId);

            int count = 1;
            for ( BluetoothDevice dev : devs ) {
                if(dev.getAddress().startsWith("C8:0F:10")) {
                    server.write("Band #" + String.valueOf(count++) + " Name: " + dev.getName() + " Address " + dev.getAddress(), socketId);
                }
            }
            return;
        }

        server.write("Unknown command", socketId );
    }

    public boolean verifyCommand(String[] args){

        if(args == null)
            return false;

        if(args.length == 0)
            return false;

        return true;
    }

}
