package com.terapeutica.smartfit.server;

import java.net.Socket;

/**
 * Created by daco on 12/01/2017.
 */
public interface ServerListener{

    public void onSuccess(int port);

    public void onFail(String msg);

    public void onSuccessSocket(int socketId);

    public void onTimeoutSocket(int socketId);

    public void onFailSocket(String msg, int socketId);

    public void onCloseSocket(int socketId);

    public void onAccept(Socket socket);

    public void onRead(String data, int socketId);
}
