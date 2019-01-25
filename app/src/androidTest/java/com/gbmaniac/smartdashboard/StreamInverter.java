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

import java.nio.charset.Charset;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static java.lang.Integer.*;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class StreamInverter {
    DockingActivity activity;

    /* Created by Annisa Alifiani
     * Date 17/12/18
     *
     * BMS Stream berisi informasi baterai pada motor
     * byte[0] = 0x12 >> header
     * byte[1] & byte[2] = speed
     * byte[3] 4 bit = start, stop, throttle on warning, dan ivc
     * byte[3] 4 bit & byte[4] & byte[5] = odometer
     * byte[6] & byte[7] = tachometer
     * byte[8] = torque
     *
     * Parse yang dilakukan pada streamBMS hanya battery 1
     */

    @Rule
    public ActivityTestRule<DockingActivity> mDockingActivity = new ActivityTestRule<DockingActivity>(DockingActivity.class);

    @UiThreadTest
    @Before
    public void setUp(){
        activity = mDockingActivity.getActivity();
    }

    public static byte[] intToBytes(String string) {
        return string.getBytes(Charset.forName("UTF-8"));
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
//                    byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//                    mDockingActivity.getActivity().getData(buffer);
//                    SystemClock.sleep(100);
//                }
//            });
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//
//        //kirim data selama 30 menit dengan semua nilai minimum
//        for(int i=1; i<18000; i++) {
//            String hex = Integer.toHexString(i);
//
//            byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte)0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//
//            mDockingActivity.getActivity().getData(buffer);
//            TextView tvSpeed = (TextView) activity.findViewById(R.id.speed);
//            String speed = tvSpeed.getText().toString();
//            assertEquals(0, Integer.valueOf(speed), 1);
//
//            TextView tvOdometer = (TextView) activity.findViewById(R.id.odometer);
//            String odometer = tvOdometer.getText().toString();
//            assertEquals(0, Float.valueOf(odometer), 1);
//
//            TextView tvTrip = (TextView) activity.findViewById(R.id.trip);
//            String trip = tvTrip.getText().toString();
//            assertEquals(0, Float.valueOf(trip), 1);
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
//                    byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//                    mDockingActivity.getActivity().getData(buffer);
//                    SystemClock.sleep(100);
//                }
//            });
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//
//        //set angka awal, khusus untuk speed diberi set awal terbesar karena terkendala dengan root hierarchy.
//        byte[] set = new byte[]{0x12, (byte) 0x00,  (byte)0xFF, (byte) 0x00, (byte) 0x0F, (byte) 0xFF, (byte) 0x4A, (byte) 0x38, (byte) 0xFF};
//        mDockingActivity.getActivity().getData(set);
//        SystemClock.sleep(100);
//
//        //kirim data selama 30 menit dengan semua nilai minimum
//        for(int i=1; i<18000; i++) {
//            String hex = Integer.toHexString(i);
//
//            byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte)0xFF, (byte) 0x00, (byte) 0x0F, (byte) 0xFF, (byte) 0x4A, (byte) 0x38, (byte) 0xFF};
//
//            mDockingActivity.getActivity().getData(buffer);
//            TextView tvSpeed = (TextView) activity.findViewById(R.id.speed);
//            String speed = tvSpeed.getText().toString();
//            assertEquals(255, Integer.valueOf(speed), 1);
//
//            TextView tvOdometer = (TextView) activity.findViewById(R.id.odometer);
//            String odometer = tvOdometer.getText().toString();
//            assertEquals(409, Float.valueOf(odometer), 1);
//
//            TextView tvTrip = (TextView) activity.findViewById(R.id.trip);
//            String trip = tvTrip.getText().toString();
//            assertEquals(409, Float.valueOf(trip), 1);
//
//            SystemClock.sleep(100);
//        }
//    }

    @Test
    public void speedNaik(){
        onView(withId(R.id.on_off_button)).perform(click());

        //thread untuk inisialisasi view untuk menampilkan data inverter
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    mDockingActivity.getActivity().getData(buffer);
                    SystemClock.sleep(100);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //set angka awal, khusus untuk speed diberi set awal terbesar karena terkendala dengan root hierarchy.
        byte[] set = new byte[]{0x12, (byte) 0x00,  (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        mDockingActivity.getActivity().getData(set);
        SystemClock.sleep(100);

        //kirim data selama 1 menit dengan speed dari kecil ke besar
        for(int i=1; i<18000; i++) {
            int x = 0;
            if(i > 100){
                x = i % 100;
            }

            byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte)(parseInt(String.valueOf(x),16) & 0xff), (byte) 0x00, (byte) 0x00 , (byte) (parseInt(String.valueOf(x+1),16) & 0xff), (byte) 0x00, (byte) (parseInt(String.valueOf(x+1),16) & 0xff), (byte) (parseInt(String.valueOf(x+2),16) & 0xff)};

            mDockingActivity.getActivity().getData(buffer);
            TextView speedText = (TextView) activity.findViewById(R.id.speed);
            String s = speedText.getText().toString();
            assertEquals(Integer.valueOf(parseInt(String.valueOf(x),16)), Integer.valueOf(s), 1);

//            TextView tvOdometer = (TextView) activity.findViewById(R.id.odometer);
//            String odometer = tvOdometer.getText().toString();
//            assertEquals((parseInt(String.valueOf(x+1),16) & 0xff), Float.valueOf(odometer), 1);
//
//            TextView tvTrip = (TextView) activity.findViewById(R.id.trip);
//            String trip = tvTrip.getText().toString();
//            assertEquals((parseInt(String.valueOf(x+1),16) & 0xff), Float.valueOf(trip), 1);

            SystemClock.sleep(100);
        }
    }

//    @Test
//    public void speedTurun(){
//        onView(withId(R.id.on_off_button)).perform(click());
//
//        //thread untuk inisialisasi view untuk menampilkan data inverter
//        try {
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//
//                    mDockingActivity.getActivity().getData(buffer);
//                    SystemClock.sleep(100);
//                }
//            });
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//
//        //set angka awal, khusus untuk speed diberi set awal terbesar karena terkendala dengan root hierarchy.
//        byte[] set = new byte[]{0x12, (byte) 0x00,  (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//        mDockingActivity.getActivity().getData(set);
//        SystemClock.sleep(100);
//
//        //kirim data selama 1 menit dengan speed dari besar ke kecil
//        for(int i=60; i>1; i--) {
//            byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) (parseInt(String.valueOf(i),16) & 0xff), (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) (parseInt(String.valueOf(i+2),16) & 0xff)};
//
//            mDockingActivity.getActivity().getData(buffer);
//            TextView speedText = (TextView) activity.findViewById(R.id.speed);
//            String s = speedText.getText().toString();
//            assertEquals(String.valueOf(parseInt(String.valueOf(i),16)), s);
//            SystemClock.sleep(100);
//        }
//    }
}
