package com.terapeutica.smartfit.server;

import android.util.Log;

import com.terapeutica.smartfit.utils.TimerDelay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by daco on 12/01/2017.
 */
public class ServerSocketThread {

    Socket socket;
    int id;
    int maxTimeSocketConnection = 0;

    InputStream in;
    OutputStream out;

    String TAG = "Socket Server";

    Timer timer = new Timer();

    ServerListener serverListener;

    boolean closed = false;

    public ServerSocketThread(Socket socket, int id, ServerListener serverListener) {

        this.socket = socket;
        this.id = id;
        this.serverListener = serverListener;

        TAG = "Socket Server #" + String.valueOf(id);

        try {
            in = this.socket.getInputStream();
            out = this.socket.getOutputStream();
            if(this.serverListener != null)
                this.serverListener.onSuccessSocket(id);
        } catch (IOException e) {
            e.printStackTrace();
            if(this.serverListener != null)
                this.serverListener.onFailSocket( e.toString(), id );
        }
    }

    public int getId() {
        return id;
    }

    public int getMaxTimeSocketConnection() {
        return maxTimeSocketConnection;
    }

    public void setMaxTimeSocketConnection(int msec) {
        this.maxTimeSocketConnection = msec;
    }

    public void write(String str){
        try {
            out.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            if(serverListener != null)
                serverListener.onFailSocket( e.toString(), id );
            close();
        }
    }

    public int read(){

        int data = 0;

        try {
            data = in.read();
        } catch (IOException e) {
            e.printStackTrace();
            if(serverListener != null)
                serverListener.onFailSocket( e.toString(), id );
            close();
        }
        return data;
    }

    public void start(){
        Log.v(TAG, "Start");
        thread.start();
    }

    public void close(){

        if(closed)
            return;

        try {
            in.close();
            out.close();
            socket.close();
            closed = true;
            Log.v(TAG, "Close");
            if(serverListener != null)
                serverListener.onCloseSocket(id);

        } catch (IOException e) {
            e.printStackTrace();
            if(serverListener != null)
                serverListener.onFailSocket( e.toString(), id );
        }

        timer.cancel();
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

            String buffer = "";
            int data = 0;

            if(maxTimeSocketConnection > 0) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (socket.isConnected()) {
                            if(serverListener != null)
                                serverListener.onTimeoutSocket(id);
                            close();
                        }
                    }
                }, maxTimeSocketConnection);
            }

            while(socket.isConnected() && !closed){

                data = read();

                if(data == 13 || data <= 0)
                    continue;

                if(data == 10) {
                    if(serverListener != null )
                        serverListener.onRead(buffer, id);
                    TimerDelay.delay(500);
                    close();
                }

                if(data > 0 && data < 127) {
                    buffer += (char) data;
                }
            }
        }
    });


}
