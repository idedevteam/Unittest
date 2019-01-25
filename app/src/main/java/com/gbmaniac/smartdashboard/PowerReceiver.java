package com.gbmaniac.smartdashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Power on");
        Intent i = new Intent(context, LoadingActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}