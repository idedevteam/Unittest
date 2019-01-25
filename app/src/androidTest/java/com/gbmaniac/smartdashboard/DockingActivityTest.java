package com.gbmaniac.smartdashboard;

import android.os.SystemClock;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.AppCompatImageView;
import android.widget.Button;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.Charset;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasBackground;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static java.lang.Integer.parseInt;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DockingActivityTest {

    DockingActivity activity;

    @Rule
    public ActivityTestRule<DockingActivity> mDockingActivity = new ActivityTestRule<DockingActivity>(DockingActivity.class);

    @UiThreadTest
    @Before
    public void setUp(){
         activity = mDockingActivity.getActivity();
    }

//    @Test
//    public void checkDisplayed(){
//        //display informasi inverter
//        onView(withId(R.id.torque)).check(matches(isDisplayed()));
//        onView(withId(R.id.speed)).check(matches(isDisplayed()));
//        onView(withId(R.id.odometer)).check(matches(isDisplayed()));
//        onView(withId(R.id.trip)).check(matches(isDisplayed()));
//
//        //display motor
////        onView(withId(R.id.motor_mode)).check(matches(isDisplayed()));
////        onView(withId(R.id.temp_motor)).check(matches(isDisplayed()));
//
//        //display informasi baterai 1
//        onView(withId(R.id.gauge_battery1)).check(matches(isDisplayed()));
//        onView(withId(R.id.bat1)).check(matches(isDisplayed()));
//        onView(withId(R.id.temp_bat1)).check(matches(isDisplayed()));
//
//        //display informasi baterai 2
//        onView(withId(R.id.gauge_battery2)).check(matches(isDisplayed()));
//        onView(withId(R.id.bat2)).check(matches(isDisplayed()));
//        onView(withId(R.id.temp_bat2)).check(matches(isDisplayed()));
//
//        //display informasi lampu
//        onView(withId(R.id.sign_left)).check(matches(isDisplayed()));
//        onView(withId(R.id.sign_right)).check(matches(isDisplayed()));
//        onView(withId(R.id.headlamp)).check(matches(isDisplayed()));
//
//        //lain-lain
//        onView(withId(R.id.on_off_button)).check(matches(isDisplayed()));
//        onView(withId(R.id.on_off_button)).check(matches(isClickable()));
//    }

//    @Test
//    public void interaktifTest(){
//        //click
//        onView(withId(R.id.right_button)).perform(click());
//        onView(withId(R.id.right_button)).perform(click());
//
//        //longClick
//        onView(withId(R.id.trip)).perform(longClick());
//    }

//    @Test
//    public void onOffMotor(){
//        onView(withId(R.id.on_off_button)).perform(click());
//        onView(withId(R.id.on_off_button)).perform(click());
//
//        onView(withId(R.id.right_button)).check(matches(hasBackground(drawable/btn_start_dark_mode)));
//
//        AppCompatImageView button = (AppCompatImageView) activity.findViewById(R.id.on_off_button);
//        String buttonText = button.getBackground()
//        assertEquals(buttonText);
//    }

//    @Test
//    public void vehicleInfo(){
//        try {
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    byte[] buffer = new byte[]{0x08,
//                            (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                            (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
//                    };
//
//                    mDockingActivity.getActivity().getData(buffer);
//                }
//                });
//            } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//
//        onView(withId(R.id.right_button)).perform(click());
//    }

    @Test
    public void streamInverter(){
        onView(withId(R.id.on_off_button)).perform(click());

        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte)0xFF, (byte) 0x00, (byte) 0x0F, (byte) 0xFF, (byte) 0x4A, (byte) 0x38, (byte) 0xFF,
                            0x13, (byte) 0xFF, (byte) 0x30, (byte) 0x02, (byte) 0x05, (byte) 0xFF, (byte) 0x30, (byte) 0x02, (byte) 0x05,
                            0x14, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C, (byte) 0x00
                    };

                    mDockingActivity.getActivity().getData(buffer);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        byte[] buffer = new byte[]{0x12, (byte) 0x00,  (byte) 0xFF, (byte) 0x00, (byte) 0x00 , (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte)0xFF,
                0x13, (byte) 0x64, (byte) 0x30, (byte) 0x02, (byte) 0x05, (byte) 0x64, (byte) 0x30, (byte) 0x02, (byte) 0x05,
                0x14, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C, (byte) 0x00
        };

        mDockingActivity.getActivity().getData(buffer);
        SystemClock.sleep(100);

        for(int i=1; i<=18000; i++) {
            int x = 0;
            if(i > 100){
                x = i % 100;
            }

            byte[] buffer1 = new byte[]{0x12, (byte) 0x00,  (byte)(parseInt(String.valueOf(x),16) & 0xff), (byte) 0x00, (byte) 0x00 , (byte) (parseInt(String.valueOf(x+1),16) & 0xff), (byte) 0x00, (byte) (parseInt(String.valueOf(x+1),16) & 0xff), (byte) (parseInt(String.valueOf(x+2),16) & 0xff),
                    0x13, (byte) 0x64, (byte) 0x30, (byte) 0x02, (byte) 0x05, (byte) 0x64, (byte) 0x30, (byte) 0x02, (byte) 0x05,
                    0x14, (byte) 0x3C, (byte) 0x3C, (byte) 0x3C, (byte) 0x00
            };

//            byte[] buffer1 = new byte[]{0x12, (byte) 0x00,  (byte)0xFF, (byte) 0x00, (byte) 0x0F, (byte) 0xFF, (byte) 0x0F, (byte) 0xFF, (byte) 0xFF,
//                    0x13, (byte) 0x64,  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x64,  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
//                    0x14, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x0F
//            };

            mDockingActivity.getActivity().getData(buffer1);

            TextView tvSpeed = (TextView) activity.findViewById(R.id.speed);
            String speed = tvSpeed.getText().toString();
            assertEquals(Integer.valueOf(parseInt(String.valueOf(x),16)), Integer.valueOf(speed), 1);

//            TextView tvOdometer = (TextView) activity.findViewById(R.id.odometer);
//            String odometer = tvOdometer.getText().toString();
//            assertEquals(409, Float.valueOf(odometer), 1);

//            TextView tvTrip = (TextView) activity.findViewById(R.id.trip);
//            String trip = tvTrip.getText().toString();
//            assertEquals(409, Float.valueOf(trip), 1);

            TextView tempBattery = (TextView) activity.findViewById(R.id.temp_bat1);
            String tempBatteryString = tempBattery.getText().toString();
            assertEquals(48, Integer.valueOf(tempBatteryString.replace("°C", "")), 1);

            TextView valBattery = (TextView) activity.findViewById(R.id.bat1);
            String valBatteryString = valBattery.getText().toString();
            assertEquals(100, Integer.valueOf(valBatteryString.replace("%", "")), 1);

            SystemClock.sleep(100);
        }
    }
//
//    public static byte[] stringToBytes(String string) {
//        return string.getBytes(Charset.forName("UTF-8"));
//    }

//    @Test
//    public void vehicleInfo(){
//
//    }

//    @Test
//    public void dockingACK(){
//
//    }
}