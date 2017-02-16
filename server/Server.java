package com.terapeutica.smartfit.server;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by daco on 12/01/2017.
 */
public class Server {

    String TAG = "Server";
    ServerSocket serverSocket;
    ServerListener serverListener;
    HashMap<String, ServerSocketThread> sockets = new HashMap<String, ServerSocketThread>();

    int port = 0;
    boolean listening = false;
    int maxTimeSocketConnection = 0;

    public Server(int port, ServerListener listener ) {
        this.port = port;
        this.serverListener = listener;

        try {
            serverSocket = new ServerSocket(port);
            if(serverListener != null)
                serverListener.onSuccess(port);
        } catch (IOException e) {
            e.printStackTrace();
            if(serverListener != null)
                serverListener.onFail( e.toString() );
        }
    }

    public void listen(){
        Log.v(TAG, "Listenning ... ");

        if(isListening())
            return;

        listening = true;
        listenThread.start();
    }

    public boolean isListening() {
        return listening;
    }

    public void close(){
        Log.v(TAG, "Close");

        listening = false;

        for( ServerSocketThread socket : sockets.values()){
            socket.close();
        }
    }

    Thread listenThread = new Thread(new Runnable() {
        @Override
        public void run() {

            while(true){
                try {
                    if(serverSocket != null) {
                        Socket socket = serverSocket.accept();
                        if(serverListener != null)
                            serverListener.onAccept(socket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public void addSocket(Socket socket){
        int id = socket.hashCode();
        String str = String.valueOf( id );

        if(sockets.containsKey(str)) {
            if (serverListener != null) {
                serverListener.onFailSocket("Socket already exist", id);
                return;
            }
        }

        sockets.put( str , new ServerSocketThread(socket, id, serverListener ) );
        sockets.get(str).setMaxTimeSocketConnection( maxTimeSocketConnection );
        sockets.get(str).start();
    }

    public void removeSocket(int id){
        String str = String.valueOf( id );
        if(sockets.containsKey(str)) {
            sockets.remove(str);
        }
    }

    public void closeSocket(int id){
        String str = String.valueOf( id );
        if(sockets.containsKey(str)) {
            sockets.get(str).close();
        }
    }

    public void write(String data, int socketId){
        if(sockets.containsKey( String.valueOf(socketId)) )
            sockets.get( String.valueOf(socketId) ).write( data + "\r\n");
    }

    public int getMaxTimeSocketConnection() {
        return maxTimeSocketConnection;
    }

    public void setMaxTimeSocketConnection(int msec) {
        this.maxTimeSocketConnection = msec;
    }
}
