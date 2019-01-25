package com.gbmaniac.smartdashboard;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static final String NOT_AVAILABLE = "Ø";
    public static final int AUTH_RESPONSE_LEN = 2;
    public static final int TIME_SYNC_RESPONSE_LEN = 2;
    public static final int TIME_SYNC_ACK_LEN = 2;
    public static final int VEHICLE_INFO_LEN = 17;
    public static final int NO_LOG_RESPONSE_LEN = 2;
    public static final int LOG_RESPONSE_LEN = 2;
    public static final int ON_OFF_RESPONSE_LEN = 2;
    public static final int CHANGE_MODE_RESPONSE_LEN = 2;
    public static final int BRAKE_RESPONSE_LEN = 2;
    public static final int INVERTER_STREAM_LEN = 9;
    public static final int BMS_STREAM_LEN = 9;
    public static final int MOTOR_STREAM_LEN = 5;
    public static final int DOCKING_ACK_LEN = 1;

    public static final int BATTERY_WARNING_LEVEL = 30;

    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    /**
     * Fungsi untuk mengecek status pinned task
     * @param context konteks yang memanggil fungsi
     * @return boolean
     */
    public static boolean isAppInLockTaskMode(Context context) {
        ActivityManager activityManager;

        activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);

        // For SDK version 23 and above.
        return activityManager != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activityManager.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
    }
}
