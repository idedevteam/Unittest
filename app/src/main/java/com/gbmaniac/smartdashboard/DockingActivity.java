package com.gbmaniac.smartdashboard;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Intent.ACTION_TIME_TICK;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
import static com.gbmaniac.smartdashboard.LoadingActivity.loadingInst;
import static com.gbmaniac.smartdashboard.Utilities.*;

public class DockingActivity extends AppCompatActivity {
    public static final boolean D = BuildConfig.DEBUG;
    private static final String TAG = "DockingActivity";
    //variabel Device Manager
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminSample;
    private boolean isPinned = false;

    //variabel USB
    private static final String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";
    private static final String ACTION_USB_PERMISSION = "com.gbmaniac.smartdashboard.USB_PERMISSION";
    private boolean mPermissionRequestPending;
    private PendingIntent mPermissionIntent;
    private Handler initiateIVCHandler;
    UsbAccessory mAccessory;
    UsbAccessory lastAccessory; //can be deleted
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    private UsbManager mUsbManager;
    ConnectedThread mConnectedThread;
    Handler dcHandler;
    Runnable disconnect = new Runnable() {
        @Override
        public void run() {
            mConnectedThread.cancel();
            finishAndRemoveTask();
        }
    };

    //variabel display
    public static boolean start_trip;
    int centerX;
    int start_odometer;
    int check_battery;
    int trip;
    int check_trip;
    float sisa;
    boolean batteryWarning = false;
    GesitsProgressBarCircular torque;
    TextView connectionStatus, val_speed, val_bat1, val_bat2, warningMessages;
    TextView val_odometer, val_trip, val_sisa;
    TextView temp_motor, temp_bat1, temp_bat2;
    TextView motorMode;
    TextView inverterText;//, bmsText, motorText;
    ImageView sign_left, sign_right, headlamp, startStopButton;
    ImageView left, rightMenuButton, battery1, battery2;
    ConstraintLayout right_menu;

    //variabel waktu
    boolean userDisplayModeOverride;
    TextView clock;
    private static final String TIME_FORMAT = "HH:mm";
    private static SimpleDateFormat sdfTime = new SimpleDateFormat(TIME_FORMAT, Locale.US);

    //variabel charging
    boolean isCharging;
    ImageView sdBatteryStatus;

    //variabel network
    private TextView network;
    private ImageView networkStrength;
    WifiManager wifiManager;
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetwork;
    MyPhoneStateListener phoneStatelistener;
    TelephonyManager telephonyManager;

    //variabel suara
    PerfectLoopMediaPlayer mediaPlayer;
    AudioManager am;
    SoundPool soundPool;
    int streamID = 0, soundID = -1;
    float pitch;
    int streamVolumeOld;

    //variabel kendaraan
    private boolean docking;
    public static boolean day_mode;
    private static boolean mMotorState;
    int motorModeIndex;
    boolean right_expanded = false;
    boolean regenerationBrake = false;
    Inverter mInverter;
    BatterySystem mBatterySystem[];
    Motor mMotor;

    //variabel lain
    Handler handler, flashHandler = new Handler();
    DialogBattery dialogBattery;
    Intent service;
    public final static int REQUEST_CODE = 5376;
    TripLog tripLog = TripLog.getInstance();
    /**
     * Receiver untuk menerima intent action USB_ATTACHED dan USB_DETACHED
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if (ACTION_USB_PERMISSION.equals(action)) {
                    toastMessage("ACTION_USB_PERMISSION");
                    synchronized (this) {
                        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            openAccessory(accessory);
                            if (accessory == null) openAccessory(lastAccessory);

                        } else {
                            openAccessory(lastAccessory);
                            toastMessage("Permission denied for accessory " + accessory);
                            if (D)
                                Log.d(TAG, "Permission denied for accessory " + accessory);
                        }
                        mPermissionRequestPending = false;
                    }
                }
            } catch (Exception e) {
                toastMessage(e.getMessage());
                e.printStackTrace();
            }
        }
    };

    /**
     * Receiver untuk pemutakhiran jam
     */
    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().compareTo(ACTION_TIME_TICK) == 0) {
                if (clock != null) {
                    clock.setText(sdfTime.format(new Date()));
                }
                if (!userDisplayModeOverride) {
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    if (day_mode && (hour < 6 || hour >= 18)) {
                        changeDisplay(false, mMotorState);
                    } else if (!day_mode && hour >= 6 && hour < 18) {
                        changeDisplay(true, mMotorState);
                    }
                }
            }
        }
    };

    /**
     * Receiver untuk pemutakhiran tipe jaringan
     */
    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (network != null) {
                if (intent.getAction().compareTo(CONNECTIVITY_ACTION) == 0) {
                    activeNetwork = connectivityManager.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            network.setText("WIFI");
                            wifiManager.startScan();
                        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                            network.setText(getNetworkClass());
                        }
                    } else {
                        network.setText("N/A");
                        networkStrength.getDrawable().setLevel(5);
                    }
                }
                if (intent.getAction().compareTo(SCAN_RESULTS_AVAILABLE_ACTION) == 0
                        && activeNetwork != null
                        && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    wifiManager.getScanResults();
                    int rssi = wifiManager.getConnectionInfo().getRssi();
                    int level = WifiManager.calculateSignalLevel(rssi, 5);
                    networkStrength.getDrawable().setLevel(level);
                }
            }
        }
    };

    /**
     * Receiver untuk pemutakhiran status pengisian baterai SmartDashboard
     */
    private BroadcastReceiver powerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) {
                if (sdBatteryStatus != null)
                    sdBatteryStatus.setImageResource(day_mode ?
                            R.drawable.ic_battery_charging_day : R.drawable.ic_battery_charging_night);
            } else {
                if (sdBatteryStatus != null) sdBatteryStatus.setImageResource(day_mode ?
                        R.drawable.ic_battery_notcharging_day : R.drawable.ic_battery_notcharging_night);
            }
        }
    };

    //TODO: Tambahkan deskripsi
    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                startOverlayService();
            }
        }
    }

    //TODO: Tambahkan deskripsi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        toastMessage("Result");
        if (requestCode == REQUEST_CODE) {
            /** if so check once again if we have permission */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // continue here - permission was granted
                    startOverlayService();
                }
            }
        }
    }

    /**
     * Mengembalikan aplikasi ke mode immersive saat fokus kembali ke DockingActivity
     *
     * @param hasFocus fokus berada pada layout utama (DockingActivity)
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_docking);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        centerX = Math.round(displayMetrics.widthPixels / displayMetrics.density) / 2;
        Guideline guideline = findViewById(R.id.guide_vertical);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guideBegin = convertDpToPixel(centerX);
        guideline.setLayoutParams(params);

        //inisiasi variabel display menu
        left = findViewById(R.id.left_button);

        right_menu = findViewById(R.id.right_menu);
        rightMenuButton = findViewById(R.id.right_button);
        rightMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRightMenu(!right_expanded, R.id.right_content);
            }
        });

        //inisiasi variabel display inverter
        inverterText = findViewById(R.id.debug_streamInverter);
        torque = findViewById(R.id.torque);
        val_speed = findViewById(R.id.speed);
        val_odometer = findViewById(R.id.odometer);
        val_trip = findViewById(R.id.trip);
        val_trip.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //DialogResetTripmeter dialog = new DialogResetTripmeter(DockingActivity.this,android.R.style.Theme_Light);
                //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                DialogResetTripmeter dialog = new DialogResetTripmeter(DockingActivity.this);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!start_trip) val_trip.setText(String.valueOf(trip));
                    }
                });
                dialog.show();
                return true;
            }
        });
        val_sisa = findViewById(R.id.sisa);

        //inisiasi variabel display motor
        motorMode = findViewById(R.id.motor_mode);
        temp_motor = findViewById(R.id.temp_motor);

        //inisiasi variabel display baterai
        battery1 = findViewById(R.id.gauge_battery1);
        findViewById(R.id.battery1_info).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogBattery = new DialogBattery(DockingActivity.this);
                dialogBattery.setmBSI(mBatterySystem[0]);
                dialogBattery.show();
                return true;
            }
        });
        /*findViewById(R.id.battery1_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBatterySystem[0].isFailure() && mBatterySystem[0].getPercentage() <= BATTERY_WARNING_LEVEL){
                    toggleRightMenu(!right_expanded,R.id.map_dummy);
                }
            }
        });*/
        battery2 = findViewById(R.id.gauge_battery2);
        /*findViewById(R.id.battery2_info).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogBattery = new DialogBattery(DockingActivity.this);
                dialogBattery.setmBSI(mBatterySystem[1]);
                dialogBattery.show();
                return true;
            }
        });*/
        val_bat1 = findViewById(R.id.bat1);
        val_bat2 = findViewById(R.id.bat2);
        temp_bat1 = findViewById(R.id.temp_bat1);
        temp_bat2 = findViewById(R.id.temp_bat2);

        warningMessages = findViewById(R.id.debug_warning);
        //inisiasi variabel display lampu
        sign_left = findViewById(R.id.sign_left);
        sign_right = findViewById(R.id.sign_right);
        headlamp = findViewById(R.id.headlamp);

        //inisiasi variabel suara
        SoundPool.Builder soundBuilder = new SoundPool.Builder();
        soundBuilder.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        soundPool = soundBuilder.build();
        soundID = soundPool.load(this, R.raw.engine_idle3, 1);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //inisiasi variabel USB
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        //filter.addAction(usbStateChangeAction);
        //registerReceiver(mUsbReceiver, filter);

        //inisialisasi variabel kendaraan
        startStopButton = findViewById(R.id.on_off_button);
        startStopButton.setOnClickListener(startStopClick);
        docking = false;
        day_mode = false;
        motorModeIndex = 0;
        mMotorState = false;
        regenerationBrake = false;
        mInverter = new Inverter();
        mBatterySystem = new BatterySystem[]{new BatterySystem(), new BatterySystem()};
        mMotor = new Motor();
        mMotorState = false;

        //inisiasi variabel status pengisian baterai SmartDashboard
        sdBatteryStatus = findViewById(R.id.charge_status);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(powerConnectionReceiver, filter);
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        }

        //inisiasi variabel status jaringan
        network = findViewById(R.id.network);
        networkStrength = findViewById(R.id.network_strength);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        phoneStatelistener = new MyPhoneStateListener();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null)
            telephonyManager.listen(phoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        registerReceiver(connectivityReceiver, new IntentFilter(CONNECTIVITY_ACTION));
        registerReceiver(connectivityReceiver, new IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION));

        //inisiasi variabel display waktu
        userDisplayModeOverride = false;
        clock = findViewById(R.id.clock);
        Calendar calendar = Calendar.getInstance();
        clock.setText(sdfTime.format(new Date()));
        if (calendar.get(Calendar.HOUR_OF_DAY) < 18 && calendar.get(Calendar.HOUR_OF_DAY) >= 6) {
            changeDisplay(true, mMotorState); //change display hanya dapat dilakukan setelah seluruh variabel display terinisiasi
        }
        registerReceiver(timeReceiver, new IntentFilter(ACTION_TIME_TICK));

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this, AdminReceiver.class);

        if (loadingInst != null)
            loadingInst.finish();
        if (initiateIVCHandler == null) {
            initiateIVCHandler = new Handler();
            initiateIVCHandler.post(initiateIVC);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(service != null){
//            stopService(service);
//            service = null;
//        }
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
//        filter.addAction(usbStateChangeAction);
//        registerReceiver(mUsbReceiver, filter);
//
//        if (mAccessory != null) {
//            setConnectionStatus(true);
//            return;
//        }
//        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
//        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
//        if (accessory != null) {
//                if (mUsbManager.hasPermission(accessory))
//                    openAccessory(accessory);
//                else {
//                    setConnectionStatus(false);
//                    synchronized (mUsbReceiver) {
//                        if (!mPermissionRequestPending) {
//                            mUsbManager.requestPermission(accessory, mPermissionIntent);
//                            mPermissionRequestPending = true;
//                        }
//                    }
//                }
//        } else {
//            setConnectionStatus(false);
//            if (D) {
//                toastMessage("IVC tidak terdeteksi");
//                Log.d(TAG, "mAccessory is null");
//            }
//        }
//        if(mAccessory == null){
//            finish();
//            startActivity(new Intent(this,DockingActivity.class));
//        }
//        torque.toggleNightMode(day_mode);
//    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopEngineSound();
//        unregisterReceiver(mUsbReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //checkDrawOverlayPermission();
        }else{
            //startOverlayService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TripLog.StopLogging(mInverter.getOdometer(),new Date().getTime());
        Log.d(TAG,tripLog.getLog());
        Log.d(TAG,tripLog.showLog());
        if(initiateIVCHandler != null) {
            initiateIVCHandler.removeCallbacks(initiateIVC);
            initiateIVCHandler = null;
        }
        closeAccessory();
        //unregisterReceiver(mUsbReceiver);
        unregisterReceiver(powerConnectionReceiver);
        unregisterReceiver(connectivityReceiver);
        try {
            unregisterReceiver(timeReceiver);
        }catch (Exception ignored){}
        if(service != null) {
            //Intent service = new Intent(this, OverlayService.class);
            stopService(service);
            service = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mAccessory == null) {
            finish();
        }
    }

    /**
     * Fungsi untuk mengalihkan menu kanan
     * Menu kanan berisi info-info mengenai kendaraan
     * @param isExpanded boolean apakah menu kanan akan dibuka atau ditutup
     */
    public void toggleRightMenu(boolean isExpanded, int revealID){
        final View toReveal = findViewById(revealID);
        if(isExpanded != right_expanded){
            if(!isExpanded){//close
                //right_expanded = false;
                animateCenter(centerX);
                right_menu.animate().translationX(convertDpToPixel(250)).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if(warningMessages.getVisibility() == View.INVISIBLE) {
                            findViewById(R.id.right_content).setVisibility(View.INVISIBLE);
                            findViewById(R.id.map_dummy).setVisibility(View.INVISIBLE);
                        }
                    }
                });
                left.animate().translationX(0);
                findViewById(R.id.bat1_layout).animate().scaleX(1).scaleY(1);
                findViewById(R.id.bat2_layout).animate().scaleX(1).scaleY(1);
                findViewById(R.id.right_button_arrow).animate().scaleX(1);
                findViewById(R.id.alternative_gauge).animate().translationX(convertDpToPixel(245));
            }else {//open
                //right_expanded = true;
                animateCenter(180);
                right_menu.animate().translationX(0).withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        if(warningMessages.getVisibility() == View.INVISIBLE) {
                            toReveal.setVisibility(View.VISIBLE);
                        }
                    }
                });
                left.animate().translationX(-left.getMeasuredWidth());
                findViewById(R.id.bat1_layout).animate().scaleX(0).scaleY(0);
                findViewById(R.id.bat2_layout).animate().scaleX(0).scaleY(0);
                findViewById(R.id.right_button_arrow).animate().scaleX(-1);
                findViewById(R.id.alternative_gauge).animate().translationX(0);
            }
            right_expanded = isExpanded;
        }else if(isExpanded) {
            findViewById(R.id.right_content).setVisibility(View.INVISIBLE);
            findViewById(R.id.map_dummy).setVisibility(View.INVISIBLE);
            toReveal.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Fungsi untuk menganimasikan perubahan posisi lingkaran tengah pada tampilan
     * @param end lokasi tujuan (0-1.0)
     */
    public void animateCenter(final int end){
        final Guideline guideline = findViewById(R.id.guide_vertical);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        final int start = params.guideBegin;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams)guideline.getLayoutParams();
                //lp.guideBegin = (end-start)*valueAnimator.getAnimatedFraction()+start;
                lp.guideBegin = (int) ((convertDpToPixel(end)-start)*valueAnimator.getAnimatedFraction()+start);
                guideline.setLayoutParams(lp);
            }
        });
        valueAnimator.start();
    }

    /**
     * Fungsi untuk mengirim perintah berubah mode ECO/NORMAL/SPORT
     * @param view ImageView motor_mode
     */
    public void toggleMode(View view) {
        motorModeIndex = (motorModeIndex+1)%3;
        sendMessage(new byte[]{0xe, (byte) motorModeIndex});
    }

    /**
     * Fungsi untuk mengirim perintah menyalakan dan mematikan motor
     * @param view ImageView on_off_button
     */
    private View.OnClickListener startStopClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            byte buffer[] = new byte[]{0x0c, (byte) (!mMotorState ? 1 : 0)};
            sendMessage(buffer);

//            if(warningMessages.getVisibility() == View.VISIBLE)
                readMessage(new byte[]{0x0d,buffer[1]},2);
        }
    };

    /*public void toggleMotor(View view) {
        byte buffer[] = new byte[]{0x0c, (byte) (!mMotorState ? 1 : 0)};
        sendMessage(buffer);
        if(warningMessages.getVisibility() == View.VISIBLE)
            readMessage(new byte[]{0xd,buffer[1]},2);
    }*/

    /**
     * Fungsi untuk mengubah tampilan siang dan malam
     * @param view ImageView display_mode_change
     */
    public void displayMode(View view) {
        userDisplayModeOverride = true;
        if(day_mode){//change to night mode
            changeDisplay(false,mMotorState);
            //day_mode = false;
            //((ImageView)view).setImageResource(R.drawable.ic_toggle_day_mode);
        }else{//change to day mode
            changeDisplay(true,mMotorState);
            //day_mode = true;
            //((ImageView)view).setImageResource(R.drawable.ic_toggle_night_mode);
        }
    }

    /**
     * Fungsi untuk menampilkan debug pada display
     * @param view Button
     */
    public void reveal(View view) {
        if(warningMessages.getVisibility() == View.INVISIBLE){//reveal debug
            warningMessages.setVisibility(View.VISIBLE);
            inverterText.setVisibility(View.VISIBLE);
            findViewById(R.id.debug_data).setVisibility(View.VISIBLE);
            findViewById(R.id.tripData).setVisibility(View.VISIBLE);
            if(right_expanded) {
                findViewById(R.id.right_content).setVisibility(View.INVISIBLE);
                findViewById(R.id.map_dummy).setVisibility(View.INVISIBLE);
            }
            if(isAppInLockTaskMode(this))
                stopLockTask();

        }else{//hide debug
            warningMessages.setVisibility(View.INVISIBLE);
            inverterText.setVisibility(View.INVISIBLE);
            findViewById(R.id.debug_data).setVisibility(View.INVISIBLE);
            findViewById(R.id.tripData).setVisibility(View.INVISIBLE);
            if(!isAppInLockTaskMode(this)) {
                if (mDPM.isDeviceOwnerApp(this.getPackageName())) {
                    Log.d(TAG, "isDeviceOwnerApp: YES");
                    String[] packages = {this.getPackageName()};
                    mDPM.setLockTaskPackages(mDeviceAdminSample, packages);
                } else {
                    Log.d(TAG, "isDeviceOwnerApp: NO");
                }
                if (mDPM.isLockTaskPermitted(this.getPackageName())) {
                    Log.d(TAG, "isLockTaskPermitted: ALLOWED");
                    startLockTask();
                } else {
                    Log.d(TAG, "isLockTaskPermitted: NOT ALLOWED");
                }
            }
        }
    }

    /**
     * Fungsi untuk memulai koneksi dengan USB accessory
     * @param accessory UsbAccessory yang akan dibuka
     */
    private void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;

            lastAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);

            mConnectedThread = new ConnectedThread(this);
            mConnectedThread.start();

            setConnectionStatus(true);
            if(!isAppInLockTaskMode(this)) {
                if (mDPM.isDeviceOwnerApp(this.getPackageName())) {
                    Log.d(TAG, "isDeviceOwnerApp: YES");
                    String[] packages = {this.getPackageName()};
                    mDPM.setLockTaskPackages(mDeviceAdminSample, packages);
                } else {
                    Log.d(TAG, "isDeviceOwnerApp: NO");
                }
                if (mDPM.isLockTaskPermitted(this.getPackageName())) {
                    startLockTask();
                    Log.d(TAG, "isLockTaskPermitted: ALLOWED");
                } else {
                    Log.d(TAG, "isLockTaskPermitted: NOT ALLOWED");
                }
            }
            if (D) {
                //toastMessage("Accessory opened");
                Log.d(TAG, "Accessory opened");
            }
            //mSendImei = new SendIMEI();
            //mSendImei.start();
        } else {
            setConnectionStatus(false);
            if (D) {
                //toastMessage("Accessory open failed");
                Log.d(TAG, "Accessory open failed");
            }
        }
    }

    /**
     * Fungsi untuk menampilkan koneksi dari USB accessory
     * @param connected boolean terkoneksi
     */
    private void setConnectionStatus(boolean connected) {
        //connectionStatus.setText(connected ? "Connected" : "Disconnected");
    }

    /**
     * Fungsi untuk menutup koneksi dari USB accessory
     */
    private void closeAccessory() {
        setConnectionStatus(false);

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Close all streams
        try {
            if (mInputStream != null)
                mInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mInputStream = null;
        }
        try {
            if (mOutputStream != null)
                mOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mOutputStream = null;
        }
        try {
            if (mFileDescriptor != null)
                mFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
            if(isAppInLockTaskMode(this)) {
                stopLockTask();
            }
            //toastMessage("Close Accessory");
        }
    }

    /**
     * Thread yang menerima input dari USB accessory
     */
    private class ConnectedThread extends Thread {
        Activity activity;
        TextView mTextView;
        byte[] buffer = new byte[1024];
        boolean running;

        ConnectedThread(Activity activity) {
            this.activity = activity;
            mTextView = findViewById(R.id.debug_data);
            running = true;
        }

        public void run() {
            while (running) {
                try {
                    final int bytes = mInputStream.read(buffer);
                    if(dcHandler != null){
                        dcHandler.removeCallbacks(disconnect);
                        dcHandler = null;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            byte message[] = new byte[bytes];
                            if (bytes > 0) {
                                String text = String.format(Locale.US, "RX %d bytes:", bytes);
                                //text = text.concat(", val 0x");
                                for (int i = 0; i < bytes; i++) {
                                    text = text.concat(String.format(Locale.US, "%02X", buffer[i]));
                                    message[i] = buffer[i];
                                }
                                mTextView.setText(text);
                            }
                            //parsing data yang diterima
                            readMessage(buffer,bytes);
                        }
                    });
                } catch (Exception ignore) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText("Cannot read mInputStream");
                            /*if(dcHandler == null){
                                dcHandler = new Handler();
                                dcHandler.postDelayed(disconnect,1000);
                            }*/
                            if(mAccessory != null) {
                                //Intent intent = new Intent(DockingActivity.this,LoadingActivity.class);
                                //startActivity(intent);
                                //running = false;
                                //finish();
                                finishAndRemoveTask();
                            }
                        }
                    });
                }
            }
        }

        public void cancel() {
            running = false;
        }
    }

    /**
     * Runnable yang mengirim inisiasi awal
     */
    private Runnable initiateIVC = new Runnable() {
        @Override
        public void run() {
            try {
                sendMessage(new byte[]{0x15});
            }catch (Exception ignored){
            }finally {
                if(!docking)
                    initiateIVCHandler.postDelayed(this,100);
            }
        }
    };

    /**
     * Fungsi untuk mengirim pesan ke USB accessory
     * @param message pesan yang akan dikirim
     */
    private void sendMessage(byte[] message) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(message);
                inverterText.setText(String.format(Locale.US,"TX: %s",bytesToHex(message)));
            } catch (IOException e) {
                if (D) {
                    inverterText.setText("TX: FAIL");
                    //toastMessage("write failed");
                    Log.e(TAG, "write failed", e);
                }
            }
        } else {
            inverterText.setText("TX: NO OUTPUT STREAM");
            //toastMessage("mOutputStream is null");
        }
    }

    /**
     * Fungsi untuk melakukan parsing terhadap data yang diterima dari USB accessory
     * @param message data yang diterima
     * @param len panjang data
     */
    public void readMessage(byte[] message, int len) {
//        Log.i("READMSG", )
        int count = 0;
        while(count < len) {
            switch (message[count]) {
                case 0x02:
                    authResponse(Arrays.copyOfRange(message,count,count+AUTH_RESPONSE_LEN));
                    count+=AUTH_RESPONSE_LEN;break;
                case 0x04:
                    //timeSyncRespone(Arrays.copyOfRange(message,count,count+TIME_SYNC_RESPONSE_LEN));
                    count+=TIME_SYNC_RESPONSE_LEN;break;
                case 0x06:
                    //timeDataACK(Arrays.copyOfRange(message,count,count+TIME_SYNC_ACK_LEN));
                    count+=TIME_SYNC_ACK_LEN;break;
                case 0x08:
                    vehicleInfo(Arrays.copyOfRange(message,count,count+VEHICLE_INFO_LEN));
                    count+=VEHICLE_INFO_LEN;break;
                case 0x0A:
                    //noLogResponse();
                    count+=2;break;
                case 0x0B:
                    //logResponse(Arrays.copyOfRange(message,count,count+LOG_RESPONSE_LEN));
                    count+=LOG_RESPONSE_LEN;break;
                case 0x0D:
                    onOffResponse(Arrays.copyOfRange(message,count,count+ON_OFF_RESPONSE_LEN));
                    count+=ON_OFF_RESPONSE_LEN;break;
                case 0x0F:
                    //chgModeResponse(Arrays.copyOfRange(message,count,count+CHANGE_MODE_RESPONSE_LEN));
                    count+=CHANGE_MODE_RESPONSE_LEN;break;
                case 0x11:
                    //regBrakeResponse(Arrays.copyOfRange(message,count,count+BRAKE_RESPONSE_LEN));
                    count+=BRAKE_RESPONSE_LEN;break;
                case 0x12:
                    StreamInverter(Arrays.copyOfRange(message,count,count+INVERTER_STREAM_LEN));
                    count+=INVERTER_STREAM_LEN;break;
                case 0x13:
                    StreamBMS(Arrays.copyOfRange(message,count,count+BMS_STREAM_LEN));
                    count+=BMS_STREAM_LEN;break;
                case 0x14:
                    StreamMotor(Arrays.copyOfRange(message,count,count+MOTOR_STREAM_LEN));
                    count+=MOTOR_STREAM_LEN;break;
                case 0x15:
                    dockingACK();
                    count+=DOCKING_ACK_LEN;break;
                default:
                    count++;break;
            }
        }
    }

    /**
     * Fungsi untuk testing
     */
    public void getData(byte[] message) {
        int length = message.length;
        readMessage(message, length);
    }


    /**
     * Fungsi untuk menangani response autentikasi dari USB accessory
     * @param message response authentikasi
     */
    private void authResponse(byte[] message) {
        if(message[1] == 0x1 && !docking){
            docking = true;
            toastMessage("Autentikasi berhasil");
            sendMessage(new byte[]{0x7});
        }
        if(message[1] == 0x0){
            toastMessage("IMEI tidak cocok");
        }

    }

    /**
     * Fungsi untuk menangani response penyelarasan waktu
     * @param message response penyelarasan waktu
     */
    private void timeSyncRespone(byte[] message) {

    }

    /**
     * Fungsi untuk menangani ACK dari penyelarasan waktu
     * @param message ACK dari USB accessory
     */
    private void timeDataACK(byte[] message) {

    }

    /**
     * Fungsi untuk menangani info kendaraan dari USB accessory
     * @param message info kendaraan
     */
    private void vehicleInfo(byte[] message) {
        ByteBuffer wrapped;
        wrapped = ByteBuffer.wrap(message, 1, 4);
        mInverter.set_id(wrapped.getInt());
        tripLog.setIvc_id(mInverter.get_id());

        wrapped = ByteBuffer.wrap(message, 5, 4);
        mBatterySystem[0].set_id(wrapped.getInt());
        tripLog.setBat_id(mBatterySystem[0].get_id());

        wrapped = ByteBuffer.wrap(message, 9, 4);
        mMotor.set_id(wrapped.getInt());
        tripLog.setMotor_id(mMotor.get_id());

        wrapped = ByteBuffer.wrap(message, 13, 4);
        mMotor.setFrame_id(wrapped.getInt());
        tripLog.setFrame_id(mMotor.getFrame_id());

        toastMessage("Info Get");
        Log.d("IVCID", ""+mInverter.get_id());
    }

    /**
     * Fungsi untuk menangani log kosong
     */
    private void noLogResponse() {

    }

    /**
     * Fungsi untuk menangani log yang dikirim dari USB accessory
     * @param message log data
     */
    private void logResponse(byte[] message) {

    }

    /**
     * Fungsi untuk menangani respons start/stop motor
     * @param message respons start/stop
     */
    private void onOffResponse(byte[] message) {
        if (message[1] == 0) {
            //stopEngineSound();
            start_trip = false;
            //toastMessage("change to off");
            changeDisplay(day_mode,false);
            //mMotorState = false;
            Log.i("RESPON", "OFF");
        }
        else{
            //startEngineSound();
            start_trip = false;
            //toastMessage("change to on");
            changeDisplay(day_mode,true);
            TripLog.StartLogging(mInverter.getOdometer(),new Date().getTime());
            //mMotorState = true;
            Log.i("RESPON", "ON");

//            byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) 0x60, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x03, (byte) 0x03, (byte) 0x00};
//            getData(buffer);
        }
    }

    /**
     * Fungsi untuk menangani respons penggantian mode
     * @param message respons berganti mode
     */
    private void chgModeResponse(byte[] message) {
        motorModeIndex = message[1];
        //toastMessage("Mode Change Response");
        switch(message[1]){
            case 0x00:
                motorMode.setText("NORM");
                break;
            case 0x01:
                motorMode.setText("ECO");
                break;
            case 0x02:
                motorMode.setText("SPORT");
                break;
            default:
                break;
        }
    }

    /**
     * Fungsi untuk menangani respons peralihan rem
     * @param message respons peralihan rem
     */
    private void regBrakeResponse(byte[] message) {
        regenerationBrake = message[1] == 1;
    }

    /**
     * Fungsi untuk menangani stream data inverter dari USB accessory
     * @param message stream data inverter
     */
    private void StreamInverter(byte[] message) {
        try {
            boolean ret;
            ByteBuffer wrapped;
            wrapped = ByteBuffer.wrap(message, 1, 2);
            ret = mInverter.setSpeed(wrapped.getShort());
            tripLog.AddSpeedSample(mInverter.getSpeed());
            Log.d("SPEED", ""+mInverter.getSpeed());
            if(ret) val_speed.setText(String.format(Locale.US, "%d", mInverter.getSpeed()));
            ret = mInverter.setStartFailure((message[3] & 0x80) == 0x80);
            ret = mInverter.setStopFailure((message[3] & 0x40) == 0x40);
            ret = mInverter.setThrottleOnFailure((message[3] & 0x20) == 0x20);
            ret = mInverter.setIVCFailure((message[3] & 0x10) == 0x10);
            if(ret) {
                if (mInverter.isIVCFailure()) findViewById(R.id.fail_ivc).setVisibility(View.VISIBLE);
                else findViewById(R.id.fail_ivc).setVisibility(View.INVISIBLE);
            }

            wrapped = ByteBuffer.wrap(message, 2, 4);
            ret = mInverter.setOdometer(wrapped.getInt() & 0xfffff);
            if(ret) val_odometer.setText(String.format(Locale.US, "%.1f", (float) mInverter.getOdometer() / 10));

            wrapped = ByteBuffer.wrap(message, 6, 2);
            ret = mInverter.setTachometer(wrapped.getShort() & 0x7fff);

            ret = mInverter.setTorque(message[8]&0xff);
            tripLog.AddTorqueSample(mInverter.getTorque());
            if(ret)torque.setProgress(mInverter.getTorque());

            if(mMotorState && !start_trip){
                start_odometer = mInverter.getOdometer();
                //Start logging the trip
                TripLog.StartLogging(start_odometer,new Date().getTime());
                check_trip = 0;
                check_battery = mBatterySystem[0].getPercentage();
                start_trip = true;
            }
            if(start_trip){
                trip = (mInverter.getOdometer()-start_odometer);
                val_trip.setText(String.format(Locale.US, "%.1f", (float) trip / 10));
            }
            //inverterText.setText(bytesToHex(Arrays.copyOfRange(message, 0, 7)));

            warning();

            if(warningMessages.getVisibility() != View.VISIBLE &&
                    am.getStreamVolume(STREAM_MUSIC)!= am.getStreamMaxVolume(STREAM_MUSIC)){
                am.setStreamVolume(STREAM_MUSIC, am.getStreamMaxVolume(STREAM_MUSIC),0);
            }

            /*if(mMotorState){
                if(mInverter.getSpeed()!=0){
                    stopEngineSound();
                }else{
                    startEngineSound();
                }
                //if(pitch != calculateEngineSoundPitch(mInverter.getTachometer())) {
                //    pitch = calculateEngineSoundPitch(mInverter.getTachometer());
                //    soundPool.setRate(streamID, pitch);
                //}
            }*/

            String tripData =
                    "Speed " + mInverter.getSpeed() +
                            "\nOdometer " + mInverter.getStringOdometer(" km") +
                            "\nRPM " + mInverter.getStringTachometer(" rpm") +
                            "\nTorque " + mInverter.getStringTorque(" Nm") +
                            "\nBat1 :" + mBatterySystem[0].getStringPercentage("%\n")
                            + mBatterySystem[0].getStringTemperature("°C\n")
                            + mBatterySystem[0].getStringVoltage("V\n")
                            + "Motor: " + mMotor.getTemperature() + "°C\n"
                            + mMotor.getCurrent() + "A\n";
            ((TextView) findViewById(R.id.tripData)).setText(tripData);
        } catch (Exception e) {
            //connectionStatus.setText(e.getMessage());
        }
    }

    /**
     * Fungsi untuk menangani stream data baterai dari USB accessory
     * @param message stream data baterai
     */
    private void StreamBMS(byte[] message) {
        boolean ret;
        ret = mBatterySystem[0].setFailure((message[1] & 0x80) == 0x80);
        if(ret){
            if (mBatterySystem[0].isFailure()) {
                val_bat1.setText(getString(R.string.not_available));
                temp_bat1.setText(getString(R.string.not_available));
                battery1.getDrawable().setLevel(0);
                val_bat2.setText(getString(R.string.not_available));
                temp_bat2.setText(getString(R.string.not_available));
                battery2.getDrawable().setLevel(0);
                ((ImageView)findViewById(R.id.battery1_info)).setImageResource(R.drawable.ic_fail_bat);
                ((ImageView)findViewById(R.id.battery2_info)).setImageResource(R.drawable.ic_fail_bat);
            }else {
                ((ImageView) findViewById(R.id.battery1_info)).setImageResource(R.drawable.ic_bat);
                ((ImageView) findViewById(R.id.battery2_info)).setImageResource(R.drawable.ic_bat);
            }
        }
        if(!mBatterySystem[0].isFailure()) {
            ret = mBatterySystem[0].setPercentage(message[1] & 0x7f);
            if (ret) {
                val_bat1.setText(mBatterySystem[0].getStringPercentage("%"));
                battery1.getDrawable().setLevel(mBatterySystem[0].getPercentage() / 10);
                val_bat2.setText(String.format(Locale.US, "%d%%", mBatterySystem[0].getPercentage()));
                battery2.getDrawable().setLevel(mBatterySystem[0].getPercentage() / 10);
                ((ProgressBar)findViewById(R.id.gauge_alternative1)).setProgress(mBatterySystem[0].getPercentage()/10);
                ((TextView)findViewById(R.id.info_level)).setText(String.valueOf(mBatterySystem[0].getPercentage()));

                //menghitung sisa perjalanan
                if(mMotorState) {
                    float usage = (float) (trip - check_trip) / (float) (check_battery - mBatterySystem[0].getPercentage());
                    sisa = usage * mBatterySystem[0].getPercentage();
                    if(sisa <= 0) sisa = 0;
                    val_sisa.setText(String.format(Locale.US, "%.1f", sisa / 10));
                    check_trip = trip;
                    check_battery = mBatterySystem[0].getPercentage();
                }

                //mengedipkan indikator baterai
                if (handler == null && mBatterySystem[0].getPercentage() <= BATTERY_WARNING_LEVEL) {
                    ((ImageView)findViewById(R.id.battery1_info)).setImageResource(R.drawable.ic_fail_bat);
                    ((ImageView)findViewById(R.id.battery2_info)).setImageResource(R.drawable.ic_fail_bat);
                    //toggleRightMenu(true,R.id.map_dummy);
                    handler = new Handler();
                    handler.postDelayed(blink, 500);
                } else if (handler != null && mBatterySystem[0].getPercentage() > BATTERY_WARNING_LEVEL) {
                    handler.removeCallbacks(blink);
                    handler = null;
                    ((ImageView) findViewById(R.id.battery1_info)).setImageResource(R.drawable.ic_bat);
                    ((ImageView) findViewById(R.id.battery2_info)).setImageResource(R.drawable.ic_bat);
                    battery1.setVisibility(View.VISIBLE);
                    battery2.setVisibility(View.VISIBLE);
                }
            }

            ret = mBatterySystem[0].setTemperature(message[2] & 0xff);
            if(ret) {
                temp_bat1.setText(mBatterySystem[0].getStringTemperature("°C"));
                temp_bat2.setText(String.format(Locale.US, "%d°C", mBatterySystem[0].getTemperature()));
                ((TextView) findViewById(R.id.info_temperature)).setText(String.valueOf(mBatterySystem[0].getTemperature()));
            }

            ret = mBatterySystem[0].setCurrent(message[3] & 0xff);
            if(ret)
                ((TextView)findViewById(R.id.info_current)).setText(String.valueOf(mBatterySystem[0].getCurrent()));

            ret = mBatterySystem[0].setVoltage(message[4] & 0xff);
            if(ret)
                ((TextView)findViewById(R.id.info_voltage)).setText(String.valueOf(mBatterySystem[0].getVoltage()));
            if(dialogBattery != null && dialogBattery.isShowing()){
                dialogBattery.invalidate_data(mBatterySystem[0]);
            }
        }

    }

    /**
     * Fungsi untuk menangani stream data motor dari USB accessory
     * @param message stream data motor
     */
    private void StreamMotor(byte[] message) {
        boolean ret;
        ret = mMotor.setFailure((message[4] & 0x1) == 0x1);
        if(ret){
            if (mMotor.isFailure()) findViewById(R.id.fail_engine).setVisibility(View.VISIBLE);
            else                    findViewById(R.id.fail_engine).setVisibility(View.INVISIBLE);
        }
        if(!mMotor.isFailure()) {
            ret = mMotor.setTemperature(message[1] & 0xff);
            if(ret)temp_motor.setText(String.format(Locale.US, "%d°", mMotor.getTemperature()));

            ret = mMotor.setCurrent((int) message[2]);
            //mMotor.setVoltage(message[3] & 0xff);

            ret = mMotor.setSignLampFailure((message[4] & 0x8) == 0x8);
            if(ret){
                if (mMotor.isSignLampFailure()) {
                    sign_left.setVisibility(View.INVISIBLE);
                    sign_right.setVisibility(View.INVISIBLE);
                    findViewById(R.id.fail_sign_left).setVisibility(View.VISIBLE);
                    findViewById(R.id.fail_sign_right).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.fail_sign_left).setVisibility(View.INVISIBLE);
                    findViewById(R.id.fail_sign_right).setVisibility(View.INVISIBLE);
                }
            }
            if (!mMotor.isSignLampFailure()) {
                ret = mMotor.setSignLeft((message[3] & 0x40) == 0x40);
                if(ret){
                    if(mMotor.isSignLeft()) flashHandler.post(flasher);
                    else{
                        findViewById(R.id.sign_left).setVisibility(View.INVISIBLE);
                        flashHandler.removeCallbacks(flasher);
                    }
                }
                ret = mMotor.setSignRight((message[3] & 0x20) == 0x20);
                if(ret){
                    if(mMotor.isSignRight()) flashHandler.post(flasher);
                    else{
                        findViewById(R.id.sign_right).setVisibility(View.INVISIBLE);
                        flashHandler.removeCallbacks(flasher);
                    }
                }
                //sign_left.setVisibility(mMotor.isSignLeft() ? View.VISIBLE : View.INVISIBLE);
                //sign_right.setVisibility(mMotor.isSignRight() ? View.VISIBLE : View.INVISIBLE);
            }

            ret = mMotor.setHeadLampFailure((message[4] & 0x4) == 0x4);
            if(ret){
                if (mMotor.isHeadLampFailure())
                    findViewById(R.id.fail_headlamp).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.fail_headlamp).setVisibility(View.INVISIBLE);
            }
            if (!mMotor.isHeadLampFailure()){
                mMotor.setHeadLamp((message[3] & 0x10) == 0x10);
                findViewById(R.id.lowlamp).setVisibility((message[3]&0x80)==0x80?View.VISIBLE : View.INVISIBLE);
                headlamp.setVisibility(mMotor.isHeadLamp() ? View.VISIBLE : View.INVISIBLE);
            }

            ret = mMotor.setBrakeLampFailure((message[4] & 0x2) == 0x2);
            if(ret){
                if (mMotor.isBrakeLampFailure())
                    findViewById(R.id.fail_brake).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.fail_brake).setVisibility(View.INVISIBLE);
            }
        }

        if (motorModeIndex != (message[3] & 0x3)) {
            chgModeResponse(new byte[]{0xf, (byte) (message[3] & 0x3)});
        }
        if (mMotorState != (((message[3] >> 2) & 0x1) == 0x1)) {
            onOffResponse(new byte[]{0xd, (byte) ((message[3] >> 2) & 0x1)});
        }
        if (mInverter.getSpeed() == 0 && streamID == 0 && (((message[3] >> 2) & 0x1) == 0x1)) {
            //startEngineSound();
        }
        if (regenerationBrake != (((message[3] >> 3) & 0x1) == 0x1)) {
            regBrakeResponse(new byte[]{0x11, (byte) ((message[3] >> 3) & 0x1)});
        }
    }

    /**
     * Fungsi untuk menampilkan peringatan pada TextView debug_warning
     */
    private void warning() {
        String warning_text = "";
        if (mInverter.isStartFailure())
            warning_text = warning_text.concat("Start Fail\n");

        if (mInverter.isStopFailure())
            warning_text = warning_text.concat("Stop Fail\n");

        if (mInverter.isThrottleOnFailure())
            warning_text = warning_text.concat("Throttle on\n");

        if (mInverter.isIVCFailure())
            warning_text = warning_text.concat("IVC Fail\n");

        if (mBatterySystem[0].isFailure())
            warning_text = warning_text.concat("Battery 1 Fail\n");

        if (mBatterySystem[1].isFailure())
            warning_text = warning_text.concat("Battery 2 Fail\n");

        if (mMotor.isFailure())
            warning_text = warning_text.concat("Motor Fail\n");

        if (mMotor.isSignLampFailure())
            warning_text = warning_text.concat("Sign Lamp Fail\n");

        if (mMotor.isBrakeLampFailure())
            warning_text = warning_text.concat("Brake Lamp Fail\n");

        if (mMotor.isHeadLampFailure())
            warning_text = warning_text.concat("Headlamp Fail");

        warningMessages.setText(warning_text);
    }

    /**
     * Fungsi untuk menangani ACK docking
     */
    private void dockingACK() {
        docking = true;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(telephonyManager!=null) {
            String imei = telephonyManager.getDeviceId();
            byte message[] = new byte[imei.length()+1];
            message[0] = 0x01;
            System.arraycopy(imei.getBytes(),0,message,1,imei.length());
            Log.d("MSG", message.toString());
            sendMessage(message);
        }
    }

    /**
     * Fungsi untuk mengubah tampilan dashboard berdasarkan mode waktu dan status motor yang baru
     * @param mode mode waktu yang baru
     * @param state status motor yang baru
     */
    private void changeDisplay(boolean mode, boolean state){
        if(!mode){//this is night mode
            //common
            if(day_mode) {
                findViewById(R.id.center).setBackground(getDrawable(R.drawable.center_night_mode_circle));
                findViewById(R.id.root).setBackgroundColor(0xff101010);
                ((ImageView)findViewById(R.id.display_mode_change)).setImageResource(R.drawable.ic_toggle_day_mode);
                sdBatteryStatus.setImageResource(isCharging?R.drawable.ic_battery_charging_night:R.drawable.ic_battery_notcharging_night);
                networkStrength.clearColorFilter();
                //((ImageView)findViewById(R.id.battery1_info)).setImageResource(R.drawable.bat_btn1);
                //((ImageView)findViewById(R.id.battery2_info)).setImageResource(R.drawable.bat_btn2);
                ((TextView)findViewById(R.id.torque_title)).setTextColor(0xffffffff);
                ((TextView)findViewById(R.id.clock)).setTextColor(0xffffffff);
                network.setTextColor(0xffffffff);
                val_speed.setTextColor(0xffffffff);
                val_bat1.setTextColor(0xffffffff);
                val_bat2.setTextColor(0xffffffff);
                val_odometer.setTextColor(0xffffffff);
                val_trip.setTextColor(0xffffffff);
                val_sisa.setTextColor(0xffffffff);
                temp_motor.setTextColor(0xffffffff);
                temp_bat1.setTextColor(0xffffffff);
                temp_bat2.setTextColor(0xffffffff);

            }
            if(state){
                findViewById(R.id.center).setForeground(getDrawable(R.drawable.center_night_mode_on));
                rightMenuButton.setImageResource(R.drawable.side_hexagon_on_night_mode);
                left.setImageResource(R.drawable.side_hexagon_on_night_mode);
                startStopButton.setImageResource(R.drawable.btn_stop_dark_mode);
                ((ImageView)findViewById(R.id.corner_bottom_left)).setImageResource(R.drawable.corner_double_on_night_mode);
                ((ImageView)findViewById(R.id.corner_bottom_right)).setImageResource(R.drawable.corner_double_on_night_mode);
                ((ImageView)findViewById(R.id.corner_top_left)).setImageResource(R.drawable.corner_single_on_night_mode);
                ((ImageView)findViewById(R.id.corner_top_right)).setImageResource(R.drawable.corner_single_on_night_mode);
                ((ImageView)findViewById(R.id.corner_bottom_left_var)).setImageResource(R.drawable.corner_var_on_night_mode);
                ((ImageView)findViewById(R.id.corner_bottom_right_var)).setImageResource(R.drawable.corner_var_on_night_mode);
                ((ImageView)findViewById(R.id.corner_top_left_var)).setImageResource(R.drawable.corner_var_on_night_mode);
                ((ImageView)findViewById(R.id.corner_top_right_var)).setImageResource(R.drawable.corner_var_on_night_mode);
                ((ImageView)findViewById(R.id.right_button_arrow)).setImageResource(R.drawable.side_hexagon_arrow_on);

                findViewById(R.id.center_logo).animate().rotation(-45).setDuration(100).
                        setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.center_logo).animate().scaleX(0).scaleY(0).setDuration(100);
                        findViewById(R.id.center_thingy).animate().setStartDelay(100).alpha(1);
                    }
                });
            }else{
                startStopButton.setImageResource(R.drawable.btn_start_dark_mode);
                ((ImageView)findViewById(R.id.corner_bottom_left)).setImageResource(R.drawable.corner_double_off_night_mode);
                ((ImageView)findViewById(R.id.corner_bottom_right)).setImageResource(R.drawable.corner_double_off_night_mode);
            }
        }else{//this is day mode
            //common
            if(!day_mode) {
                findViewById(R.id.center).setBackground(getDrawable(R.drawable.center_day_mode_circle));
                findViewById(R.id.root).setBackgroundColor(0xffededed);
                ((ImageView)findViewById(R.id.display_mode_change)).setImageResource(R.drawable.ic_toggle_night_mode);
                sdBatteryStatus.setImageResource(isCharging?R.drawable.ic_battery_charging_day:R.drawable.ic_battery_notcharging_day);
                networkStrength.setColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY);
                //((ImageView)findViewById(R.id.battery1_info)).setImageResource(R.drawable.bat_btn1_day);
                //((ImageView)findViewById(R.id.battery2_info)).setImageResource(R.drawable.bat_btn2_day);
                ((TextView)findViewById(R.id.torque_title)).setTextColor(0xff000000);
                ((TextView)findViewById(R.id.clock)).setTextColor(0xff000000);
                network.setTextColor(0xff000000);
                val_speed.setTextColor(0xff000000);
                val_bat1.setTextColor(0xff000000);
                val_bat2.setTextColor(0xff000000);
                val_odometer.setTextColor(0xff000000);
                val_trip.setTextColor(0xff000000);
                val_sisa.setTextColor(0xff000000);
                temp_motor.setTextColor(0xff000000);
                temp_bat1.setTextColor(0xff000000);
                temp_bat2.setTextColor(0xff000000);
            }
            if(state){
                findViewById(R.id.center).setForeground(getDrawable(R.drawable.center_day_mode_on));
                rightMenuButton.setImageResource(R.drawable.side_hexagon_on_day_mode);
                left.setImageResource(R.drawable.side_hexagon_on_day_mode);
                startStopButton.setImageResource(R.drawable.btn_stop_day_mode);
                ((ImageView)findViewById(R.id.corner_bottom_left)).setImageResource(R.drawable.corner_double_on_day_mode);
                ((ImageView)findViewById(R.id.corner_bottom_right)).setImageResource(R.drawable.corner_double_on_day_mode);
                ((ImageView)findViewById(R.id.corner_top_left)).setImageResource(R.drawable.corner_single_on_day_mode);
                ((ImageView)findViewById(R.id.corner_top_right)).setImageResource(R.drawable.corner_single_on_day_mode);
                ((ImageView)findViewById(R.id.corner_bottom_left_var)).setImageResource(R.drawable.corner_var_on_day_mode);
                ((ImageView)findViewById(R.id.corner_bottom_right_var)).setImageResource(R.drawable.corner_var_on_day_mode);
                ((ImageView)findViewById(R.id.corner_top_left_var)).setImageResource(R.drawable.corner_var_on_day_mode);
                ((ImageView)findViewById(R.id.corner_top_right_var)).setImageResource(R.drawable.corner_var_on_day_mode);
                ((ImageView)findViewById(R.id.right_button_arrow)).setImageResource(R.drawable.side_hexagon_arrow_on);

                findViewById(R.id.center_logo).animate().rotation(-45).setDuration(100).
                        setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.center_logo).animate().scaleX(0).scaleY(0).setDuration(100);
                        findViewById(R.id.center_thingy).animate().setStartDelay(100).alpha(1);
                    }
                });
            }else{
                startStopButton.setImageResource(R.drawable.btn_start_day_mode);
                ((ImageView)findViewById(R.id.corner_bottom_left)).setImageResource(R.drawable.corner_double_off_day_mode);
                ((ImageView)findViewById(R.id.corner_bottom_right)).setImageResource(R.drawable.corner_double_off_day_mode);
            }
        }
        if(!state&&mMotorState){
            findViewById(R.id.center).setForeground(getDrawable(R.drawable.center_off));
            rightMenuButton.setImageResource(R.drawable.side_hexagon_off);
            left.setImageResource(R.drawable.side_hexagon_off);
            ((ImageView) findViewById(R.id.corner_top_left)).setImageResource(R.drawable.corner_single_off);
            ((ImageView) findViewById(R.id.corner_top_right)).setImageResource(R.drawable.corner_single_off);
            ((ImageView) findViewById(R.id.corner_bottom_left_var)).setImageResource(R.drawable.corner_var_off);
            ((ImageView) findViewById(R.id.corner_bottom_right_var)).setImageResource(R.drawable.corner_var_off);
            ((ImageView) findViewById(R.id.corner_top_left_var)).setImageResource(R.drawable.corner_var_off);
            ((ImageView) findViewById(R.id.corner_top_right_var)).setImageResource(R.drawable.corner_var_off);
            ((ImageView)findViewById(R.id.right_button_arrow)).setImageResource(R.drawable.side_hexagon_arrow_off);

            findViewById(R.id.center_thingy).animate().alpha(0).setDuration(100);
            findViewById(R.id.center_logo).animate().scaleX(1).scaleY(1).
                    setDuration(100).setStartDelay(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.center_logo).animate().rotation(0).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            });

        }
        day_mode = mode;
        mMotorState = state;
        torque.toggleNightMode(mode);
    }

    /**
     * Runnable yang digunakan untuk mengedipkan indikator baterai1 saat baterai1 tersisa kurang
     * dari 30 persen
     */
    private Runnable blink = new Runnable() {
        @Override
        public void run() {
            if(battery1.getVisibility() == View.VISIBLE) {
                battery1.setVisibility(View.INVISIBLE);
                battery2.setVisibility(View.INVISIBLE);
            }
            else {
                battery1.setVisibility(View.VISIBLE);
                battery2.setVisibility(View.VISIBLE);
            }
            handler.postDelayed(this, 500);
        }
    };

    /**
     * Runnable yang digunakan untuk mengedipkan indikator baterai1 saat baterai1 tersisa kurang
     * dari 30 persen
     */
    private Runnable flasher = new Runnable() {
        @Override
        public void run() {
            View v = null;
            if(mMotor.isSignLeft()) v = findViewById(R.id.sign_left);
            else if(mMotor.isSignRight()) v = findViewById(R.id.sign_right);
            if(v != null) {
                if (v.getVisibility() == View.VISIBLE) {
                    v.setVisibility(View.INVISIBLE);
                    v.setVisibility(View.INVISIBLE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setVisibility(View.VISIBLE);
                }
                flashHandler.postDelayed(this, 500);
            }
        }
    };

    /**
     * Fungsi untuk menampilkan toast message dengan konteks aktivitas ini
     * @param message String yang akan ditampilkan
     */
    public void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Fungsi untuk membentuk representasi byte array ke string hexadecimal
     * @param bytes array yang akan dikonversi
     * @return string representasi hexadecimal
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Fungsi untuk menghitung kecepatan playback (semakin cepat semakin naik pitch-nya)
     * sample suara mesin berdasarkan nilai RPM
     * @param rpm nilai RPM yang digunakan untuk menghitung pitch
     * @return
     */
    private float calculateEngineSoundPitch(int rpm){
        float pitch;
        pitch = 0.5f + (float)rpm/1755.42857f;
        return pitch;
    }

    /**
     * Fungsi untuk memulai suara mesin dan inisiasinya
     */
    private void startEngineSound(){
        if(streamID == 0) {
            streamVolumeOld = am.getStreamVolume(STREAM_MUSIC);
            pitch = 0.5f;
            streamID = soundPool.play(soundID, 1, 1, 1, -1, pitch);
        }
    }

    /**
     * Fungsi untuk menghentikan suara mesin
     */
    private void stopEngineSound(){
        if(streamID != 0) {
            soundPool.stop(streamID);
            streamID = 0;
            am.setStreamVolume(STREAM_MUSIC, streamVolumeOld, 0);
        }
    }

    /**
     * Fungsi untuk memperoleh tipe jaringan internet
     */
    public String getNetworkClass() {
        if(telephonyManager == null) return "N/A";
        int networkType = telephonyManager.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "N/A";
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if(activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                networkStrength.getDrawable().setLevel(signalStrength.getLevel());
            }
        }
    }

    private void startOverlayService(){
        if(service==null) {
            service = new Intent(this,OverlayService.class);
            serviceUpdate = new Handler();
            serviceUpdate.post(serviceUpdater);
            startService(service);
        }
    }
    Handler serviceUpdate;
    Runnable serviceUpdater = new Runnable() {
        @Override
        public void run() {
            if(service != null) {
                OverlayService.speed = mInverter.getStringSpeed("");
                OverlayService.torque = mInverter.getTorque();
                OverlayService.tripmeter = trip;
                OverlayService.sisa = (int) sisa * 10;
                OverlayService.odometer = mInverter.getStringOdometer("");
                OverlayService.bat1 = mBatterySystem[0].getPercentage();
                OverlayService.bat1temp = mBatterySystem[0].getStringTemperature("°C");

                OverlayService.fail_ivc = mInverter.isIVCFailure();
                OverlayService.fail_brake = mMotor.isBrakeLampFailure();
                OverlayService.sign_all = mMotor.isSignLeft()||mMotor.isSignRight();
                serviceUpdate.postDelayed(this, 200);
            }
        }
    };
}
