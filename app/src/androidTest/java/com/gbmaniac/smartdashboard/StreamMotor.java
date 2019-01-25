package com.gbmaniac.smartdashboard;

import android.os.SystemClock;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static java.lang.Integer.parseInt;
import static org.junit.Assert.*;

public class StreamMotor {

    DockingActivity activity;

    @Rule
    public ActivityTestRule<DockingActivity> mDockingActivity = new ActivityTestRule<DockingActivity>(DockingActivity.class);

    @UiThreadTest
    @Before
    public void setUp(){
        activity = mDockingActivity.getActivity();
    }

    @Test
    public void nilaiMaksimum(){
        onView(withId(R.id.on_off_button)).perform(click());

        //thread untuk inisialisasi view untuk menampilkan data inverter
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    byte[] buffer = new byte[]{0x14, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C, (byte) 0x00};
                    mDockingActivity.getActivity().getData(buffer);
                    SystemClock.sleep(100);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //kirim data selama 30 menit dengan keadaan battery minimum dari kecil ke besar
        for(int i=0; i<18000; i++) {
            byte[] buffer1 = new byte[]{0x14, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C, (byte) 0x00};
            mDockingActivity.getActivity().getData(buffer1);

            SystemClock.sleep(100);
        }
    }
}