package com.gbmaniac.smartdashboard;

import android.os.SystemClock;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static java.lang.Integer.parseInt;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StreamBMS {

    /* Created by Annisa Alifiani
     * Date 19/12/18
     *
     * BMS Stream berisi informasi baterai pada motor
     * byte[0] = 0x13 >> header
     * byte[1] = battery 1 level
     * byte[2] = battery 1 temp
     * byte[3] = battery 1 current
     * byte[4] = battery 1 volt
     * byte[5] = battery 2 level
     * byte[6] = battery 2 temp
     * byte[7] = battery 2 current
     * byte[8] = battery 2 volt
     *
     * Parse yang dilakukan pada streamBMS hanya battery 1
     */

    DockingActivity activity;

    @Rule
    public ActivityTestRule<DockingActivity> mDockingActivity = new ActivityTestRule<DockingActivity>(DockingActivity.class);

    @UiThreadTest
    @Before
    public void setUp(){
        activity = mDockingActivity.getActivity();
    }

//    @Test
//    public void nilaiMinimum(){
//        onView(withId(R.id.on_off_button)).perform(click());
//
//        //thread untuk inisialisasi view untuk menampilkan data inverter
//        try {
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    byte[] buffer = new byte[]{0x13, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00};
//                    mDockingActivity.getActivity().getData(buffer);
//                    SystemClock.sleep(100);
//                }
//            });
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//
//        //kirim data selama 30 menit dengan keadaan battery minimum dari kecil ke besar
//        for(int i=1; i<18000; i++) {
//            byte[] buffer = new byte[]{0x13, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00};
//
//            mDockingActivity.getActivity().getData(buffer);
//            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
//            String tempBatteryString = tempBattery.getText().toString();
//            assertEquals(0, Integer.valueOf(tempBatteryString.replace("°C", "")), 1);
//
//            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
//            String valBatteryString = valBattery.getText().toString();
//            assertEquals(0, Integer.valueOf(valBatteryString.replace("%", "")), 1);
//
////            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
////            String tempBatteryString = tempBattery.getText().toString();
////            assertEquals(0, Integer.valueOf(tempBatteryString.replace("°C", "")), 1);
////
////            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
////            String valBatteryString = valBattery.getText().toString();
////            assertEquals(0, Integer.valueOf(valBatteryString.replace("%", "")), 1);
//
//            SystemClock.sleep(100);
//        }
//    }

//    @Test
//    public void nilaiMaksimum(){
//        onView(withId(R.id.on_off_button)).perform(click());
//
//        //thread untuk inisialisasi view untuk menampilkan data inverter
//        try {
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    byte[] buffer = new byte[]{0x13, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00};
//                    mDockingActivity.getActivity().getData(buffer);
//                    SystemClock.sleep(100);
//                }
//            });
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//
//        //kirim data selama 30 menit dengan keadaan battery minimum dari kecil ke besar
//        for(int i=1; i<18000; i++) {
//            byte[] buffer = new byte[]{0x13, (byte) 0x64,  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x64,  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//
//            mDockingActivity.getActivity().getData(buffer);
//            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
//            String tempBatteryString = tempBattery.getText().toString();
//            assertEquals(255, Integer.valueOf(tempBatteryString.replace("°C", "")), 1);
//
//            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
//            String valBatteryString = valBattery.getText().toString();
//            assertEquals(100, Integer.valueOf(valBatteryString.replace("%", "")), 1);
//
////            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
////            String tempBatteryString = tempBattery.getText().toString();
////            assertEquals(0, Integer.valueOf(tempBatteryString.replace("°C", "")), 1);
////
////            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
////            String valBatteryString = valBattery.getText().toString();
////            assertEquals(0, Integer.valueOf(valBatteryString.replace("%", "")), 1);
//
//            SystemClock.sleep(100);
//        }
//    }

    @Test
    public void nilaiMaksimum(){
        onView(withId(R.id.on_off_button)).perform(click());

        //thread untuk inisialisasi view untuk menampilkan data inverter
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    byte[] buffer = new byte[]{0x13, (byte) 0x64,  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x64,  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
                    mDockingActivity.getActivity().getData(buffer);
                    SystemClock.sleep(100);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //kirim data selama 30 menit dengan keadaan battery minimum dari kecil ke besar
        for(int i=0; i<100; i++) {
            for(int j=0; j<180; j++) {

                byte[] buffer1 = new byte[]{0x13, (byte) (parseInt(String.valueOf(50), 16) & 0xff), (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x64, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

                mDockingActivity.getActivity().getData(buffer1);
//            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
//            String tempBatteryString = tempBattery.getText().toString();
//            assertEquals((parseInt(String.valueOf(x),16) & 0xff), Integer.valueOf(tempBatteryString.replace("°C", "")), 1);
//
//            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
//            String valBatteryString = valBattery.getText().toString();
//            assertEquals((parseInt(String.valueOf(x-1),16) & 0xff), Integer.valueOf(valBatteryString.replace("%", "")), 1);

//            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
//            String tempBatteryString = tempBattery.getText().toString();
//            assertEquals(0, Integer.valueOf(tempBatteryString.replace("°C", "")), 1);
//
//            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
//            String valBatteryString = valBattery.getText().toString();
//            assertEquals(0, Integer.valueOf(valBatteryString.replace("%", "")), 1);

                SystemClock.sleep(100);
            }
        }
    }
}