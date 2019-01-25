package com.gbmaniac.smartdashboard;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

/**
 * Class untuk menampilkan dialog status baterai
 */
public class DialogBattery extends Dialog {
    private Context context;
    private BatterySystem mBSI;
    private TextView text_id, text_level, text_voltage, text_current, text_temperature;

    public DialogBattery(@NonNull Context context) {
        super(context);
        this.context = context;
        mBSI = new BatterySystem();
    }

    public void setmBSI(BatterySystem mBSI) {
        this.mBSI = mBSI;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_battery);
        Objects.requireNonNull(getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        text_id = findViewById(R.id.bat_id);
        text_level = findViewById(R.id.bat_level);
        text_voltage = findViewById(R.id.bat_voltage);
        text_current = findViewById(R.id.bat_current);
        text_temperature = findViewById(R.id.bat_temperature);
        init();
    }

    public void init(){
        if(mBSI.isFailure()){
            return;
        }

        String text;
        //text = mBSI.get_id()==-1?"N/A":String.format(Locale.US,"%d", mBSI.get_id());
        text_id.setText(mBSI.getString_id());
        if(mBSI.getPercentage()<=30)
            text_level.setTextColor(context.getColor(R.color.baseOrange));
        else
            text_level.setTextColor(context.getColor(R.color.baseDay));

        text = mBSI.getStringPercentage(" %");//==-1?"N/A":String.format(Locale.US,"%d %%", mBSI.getPercentage());
        text_level.setText(text);

        text = mBSI.getStringVoltage(" V");//==-1?"N/A":String.format(Locale.US,"%d V", mBSI.getVoltage());
        text_voltage.setText(text);

        text = mBSI.getStringCurrent(" A");//==-1?"N/A":String.format(Locale.US,"%d A", mBSI.getCurrent());
        text_current.setText(text);

        text = mBSI.getStringTemperature(" °C");//==-1?"N/A":String.format(Locale.US,"%d °C", mBSI.getTemperature());
        text_temperature.setText(text);
    }

    /**
     * Fungsi untuk memuat ulang informasi pada dialog
     * @param batterySystemInfo Battery System yang akan ditampilkan informasinya
     */
    public void invalidate_data(BatterySystem batterySystemInfo){
        try {
            this.mBSI = batterySystemInfo;
            init();
        }catch (Exception ignored){ }
    }
}
