package com.gbmaniac.smartdashboard;

import android.widget.TextView;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class DockingActivityTest {

    Inverter mInverter = new Inverter();
    DockingActivity da = new DockingActivity();

    @Test
    public void sendData(){
        byte[] buffer = new byte[]{0x12, (byte) 0x00, (byte) 0x0F, (byte) 0x00, (byte) 0x03};
        da.getData(buffer);

        TextView sp = (TextView) da.findViewById(R.id.speed);
        String s = sp.getText().toString();
        assertEquals("11", s);
    }

}