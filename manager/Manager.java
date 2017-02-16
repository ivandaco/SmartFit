package com.terapeutica.smartfit.manager;

import android.content.Context;
import android.util.Log;
import com.terapeutica.smartfit.miband.Band;
import java.util.HashMap;

/**
 * Created by daco on 20/01/2017.
 */
public class Manager {

    String TAG = "Manager";

    HashMap<String, Band> bands = new HashMap<String, Band>();

    Context context;

    ManagerListener listener;

    public Manager(Context context, ManagerListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void exec(String command, int socketId){
        Log.v(TAG, "Exec " + command);

        String[] args = command.split(" ");

        if(!verifyCommand(args)){
            if(listener != null)
                listener.onWriteServer("Invalid command", socketId);
            return;
        }

        String action = args[1];
        String address = args[2];

        if(action.equals("on")){
            if(!bands.containsKey(address)) {
                bands.put(address, new Band(context, address, null));
            }
            bands.get(address).on();
            return;
        }

        if (!bands.containsKey(address)){
            if(listener != null)
                listener.onWriteServer("Band is not available", socketId);
            return;
        }

        if(action.equals("off")){
            bands.get(address).off();
            return;
        }

        if(action.equals("start")){
            bands.get(address).start();
            return;
        }

        if(action.equals("stop")){
            bands.get(address).stop();
            return;
        }

        if(action.equals("user")){
            String alias = args[3];
            String gender = args[4];
            int age = Integer.parseInt(args[5]);
            int height = Integer.parseInt(args[6]);
            int weight = Integer.parseInt(args[7]);

            bands.get(address).off();
            bands.get(address).setUser(alias, gender, age, height, weight);
            bands.get(address).on();

            return;
        }

        if(action.equals("data")){
            if(listener != null)
                listener.onWriteServer( bands.get(address).getUserData().toString(), socketId);
            return;
        }

        if(action.equals("alarm")){
            bands.get(address).alarm();
            return;
        }

        if(listener != null)
            listener.onWriteServer("Unknown command", socketId);
    }

    public boolean verifyCommand(String[] args){

        if(args == null)
            return false;

        if(args.length != 3 && args.length != 8)
            return false;

        if(!args[0].equals("band"))
            return false;

        return true;
    }

}
