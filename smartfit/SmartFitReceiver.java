package com.terapeutica.smartfit.smartfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmartFitReceiver extends BroadcastReceiver {
    String TAG = "SmartFitReceiver";

    public SmartFitReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Intent server = new Intent(context, SmartFitServer.class);
            context.startService(server);

            Intent manager = new Intent(context, SmartFitManager.class);
            context.startService(manager);
        }
    }
}
