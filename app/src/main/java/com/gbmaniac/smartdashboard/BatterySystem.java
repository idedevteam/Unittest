package com.gbmaniac.smartdashboard;

import static com.gbmaniac.smartdashboard.Utilities.NOT_AVAILABLE;

public class BatterySystem {
    private long _id;
    private boolean failure;
    private int percentage;
    private int temperature;
    private int voltage;
    private int current;

    public BatterySystem(){
        _id = -1;
        failure = true;
        percentage = -1;
        temperature = -1;
        voltage = -1;
        current = -1;
    }
    public boolean set_id(long _id) {
        boolean ret = this._id != _id;
        this._id = _id;
        return ret;
    }

    public boolean setFailure(boolean failure) {
        boolean ret = this.failure != failure;
        this.failure = failure;
        return ret;
    }

    public boolean setPercentage(int percentage) {
        boolean ret = this.percentage != percentage;
        this.percentage = percentage;
        return ret;
    }

    public boolean setTemperature(int temperature) {
        boolean ret = this.temperature != temperature;
        this.temperature = temperature <0?0:temperature;
        return ret;
    }

    public boolean setVoltage(int voltage) {
        boolean ret = this.voltage != voltage;
        this.voltage = voltage<0?0:voltage;
        return ret;
    }

    public boolean setCurrent(int current) {
        boolean ret = this.current != current;
        this.current = current<0?0:current;
        return ret;
    }

    public long get_id() {
        return _id;
    }

    public boolean isFailure() {
        return failure;
    }

    public int getPercentage() {
        return percentage;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getVoltage() {
        return voltage;
    }

    public int getCurrent() {
        return current;
    }

    /**
     *  Mengembalikan data ke bentuk String untuk langsung ditampilkan
     */
    public String getString_id(){
        return _id == -1?"N\\A":String.valueOf(_id);
    }
    public String isStringFailure(){
        return String.valueOf(failure);
    }
    public String getStringPercentage(String unit){
        return percentage<0?NOT_AVAILABLE:String.valueOf(percentage)+unit;
    }
    public String getStringTemperature(String unit){
        return temperature<0?NOT_AVAILABLE:String.valueOf(temperature)+unit;
    }
    public String getStringVoltage(String unit){
        return voltage<0?NOT_AVAILABLE:String.valueOf(voltage)+unit;
    }
    public String getStringCurrent(String unit){
        return current<0?NOT_AVAILABLE:String.valueOf(current)+unit;
    }

}
