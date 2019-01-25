package com.gbmaniac.smartdashboard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import static com.gbmaniac.smartdashboard.Utilities.convertDpToPixel;

/**
 * Created by gbmaniac on 2/20/2018.
 */

public class OverlayService extends Service{
    private static final String TAG = OverlayService.class.getSimpleName();
    private static final String BROADCAST_OVERLAY_WIDTH = "com.gesits.smartdashboard.overlay.WIDTH";
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private View floatingView,failIVC,failBrake,signAll;
    private TextView speedView,odoView,tripView,bat1View,bat1tempView;
    private ImageView dismiss;
    private Handler handler;
    public static int bat1,torque,tripmeter,sisa;
    public static boolean fail_ivc, fail_brake, sign_all;
    public static String speed, odometer, bat1temp;
    ProgressBar torquemeter, batGauge1, batGauge2;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        addOverlayView();
        handler = new Handler();
        handler.post(update);
    }

    private void addOverlayView() {
        params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        //        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;

        FrameLayout interceptorLayout = new FrameLayout(this);
        floatingView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.overlay_layout_light, interceptorLayout);
        speedView = floatingView.findViewById(R.id.speed);
        torquemeter = floatingView.findViewById(R.id.torsi);
        odoView = floatingView.findViewById(R.id.odometer_value);
        tripView = floatingView.findViewById(R.id.trip_value);

        batGauge1 = floatingView.findViewById(R.id.batterymeter1);
        bat1View = floatingView.findViewById(R.id.bat1val);
        bat1tempView = floatingView.findViewById(R.id.bat1temp);

        batGauge2 = floatingView.findViewById(R.id.batterymeter2);

        failIVC = floatingView.findViewById(R.id.fail_ivc);
        failBrake = floatingView.findViewById(R.id.fail_brake);
        signAll = floatingView.findViewById(R.id.sign_all);

        dismiss = floatingView.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context,DockingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        windowManager.addView(floatingView, params);
        Log.i(TAG,String.valueOf("onCreate "+floatingView.getMeasuredWidth()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            Intent broadcast = new Intent(BROADCAST_OVERLAY_WIDTH);
            broadcast.putExtra("width", convertDpToPixel(150));
            sendBroadcast(broadcast);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            //speedView.setText(speed);
            //torquemeter.setProgress(torque);
            //odoView.setText(odometer);
            //batGauge1.setProgress(bat1/10);
            //bat1tempView.setText(bat1temp);
            //batGauge2.setProgress(intent.getIntExtra("battery2",0)/10);
        }catch (Exception e){
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
            handler.removeCallbacks(update);
        }
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            speedView.setText(speed);
            torquemeter.setProgress(torque);
            odoView.setText(odometer);
            tripView.setText(String.format(Locale.US,"%.1f",(float)tripmeter/10));
            batGauge1.setProgress(bat1/10);
            bat1View.setText(String.format(Locale.US,"%d %%", bat1));
            bat1tempView.setText(bat1temp);

            failIVC.setVisibility(fail_ivc?View.VISIBLE:View.INVISIBLE);
            failBrake.setVisibility(fail_brake?View.VISIBLE:View.INVISIBLE);
            signAll.setVisibility(sign_all?View.VISIBLE:View.INVISIBLE);
            handler.postDelayed(this,200);
        }
    };
}
