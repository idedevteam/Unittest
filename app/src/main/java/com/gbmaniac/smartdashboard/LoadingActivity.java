package com.gbmaniac.smartdashboard;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;

public class LoadingActivity extends AppCompatActivity {
    public static LoadingActivity loadingInst = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
        setRotationAnimation();
        setContentView(R.layout.activity_dummy);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        changeImage((hour>=6&&hour<18), getResources().getConfiguration().orientation);
        loadingInst = this;

        new Handler().postDelayed(waitLoad,2000);
    }

    private Runnable waitLoad = new Runnable() {
        @Override
        public void run() {
            try {
                findViewById(R.id.load).setVisibility(View.INVISIBLE);
                findViewById(R.id.load_fail).setVisibility(View.VISIBLE);
                new Handler().postDelayed(waitDone, 1000);
            }catch (Exception ignored){}
        }
    };

    private Runnable waitDone = new Runnable() {
        @Override
        public void run() {
            try{
                finishAndRemoveTask();
            }catch (Exception ignored){}
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        changeImage((hour>=6&&hour<18),newConfig.orientation);
    }

    private void changeImage(boolean day, int orientation){
        if(day){
            if(orientation == Configuration.ORIENTATION_LANDSCAPE)
                ((ImageView)findViewById(R.id.background)).setImageResource(R.drawable.background_day_land);
            else if(orientation == Configuration.ORIENTATION_PORTRAIT)
                ((ImageView)findViewById(R.id.background)).setImageResource(R.drawable.background_day);
        }else{
            if(orientation == Configuration.ORIENTATION_LANDSCAPE)
                ((ImageView)findViewById(R.id.background)).setImageResource(R.drawable.background_night_land);
            else if(orientation == Configuration.ORIENTATION_PORTRAIT)
                ((ImageView)findViewById(R.id.background)).setImageResource(R.drawable.background_night);
        }
    }

    private void setRotationAnimation() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }
}
