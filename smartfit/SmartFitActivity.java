package com.terapeutica.smartfit.smartfit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.terapeutica.smartfit.R;

public class SmartFitActivity extends AppCompatActivity {

    String TAG = "SmartFitActivity";

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartfit_layout);

        Log.v(TAG, "onCreate");

        context = getApplicationContext();

        Intent server = new Intent(context, SmartFitServer.class);
        context.startService(server);

        Intent manager = new Intent(context, SmartFitManager.class);
        context.startService(manager);

    }

}
