package com.gbmaniac.smartdashboard;

import java.util.Locale;

import static com.gbmaniac.smartdashboard.Utilities.NOT_AVAILABLE;

public class Inverter {
    private long _id;
    private int speed;
    private boolean startFailure;
    private boolean stopFailure;
    private boolean throttleOnFailure;
    private boolean IVCFailure;
    private int odometer;
    private int tachometer;
    private int torque;

    public Inverter(){
        speed = -1;
        odometer = -1;
        tachometer = -1;
        torque = -1;
        startFailure = false;
        stopFailure = false;
        throttleOnFailure = false;
        IVCFailure = true;
    }

    public boolean set_id(long _id) {
        boolean ret = this._id != _id;
        this._id = _id;
        return ret;
    }

    public boolean setSpeed(int speed) {
        boolean ret = this.speed != speed;
        this.speed = speed<0?0:speed;
        return ret;
    }

    public boolean setStartFailure(boolean startFailure) {
        boolean ret = this.startFailure != startFailure;
        this.startFailure = startFailure;
        return ret;
    }

    public boolean setStopFailure(boolean stopFailure) {
        boolean ret = this.stopFailure != stopFailure;
        this.stopFailure = stopFailure;
        return ret;
    }

    public boolean setThrottleOnFailure(boolean throttleOnFailure) {
        boolean ret = this.throttleOnFailure != throttleOnFailure;
        this.throttleOnFailure = throttleOnFailure;
        return ret;
    }

    public boolean setIVCFailure(boolean IVCFailure) {
        boolean ret =this.IVCFailure != IVCFailure;
        this.IVCFailure = IVCFailure;
        return ret;
    }

    public boolean setOdometer(int odometer) {
        boolean ret = this.odometer != odometer;
        this.odometer = odometer<0?0:odometer;
        return ret;
    }

    public boolean setTachometer(int tachometer) {
        boolean ret = this.tachometer != tachometer;
        this.tachometer = tachometer<0?0:tachometer;
        return ret;
    }

    public boolean setTorque(int torque) {
        boolean ret = this.torque != torque;
        this.torque = torque<0?-torque:torque;
        return ret;
    }

    public long get_id() {
        return _id;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isStartFailure() {
        return startFailure;
    }

    public boolean isStopFailure() {
        return stopFailure;
    }

    public boolean isThrottleOnFailure() {
        return throttleOnFailure;
    }

    public boolean isIVCFailure() {
        return IVCFailure;
    }

    public int getOdometer() {
        return odometer;
    }

    public int getTachometer() {
        return tachometer;
    }

    public int getTorque() {
        return torque;
    }

    /**
     *  Mengembalikan data ke bentuk String untuk langsung ditampilkan
     */
    public String getStringSpeed(String unit) {
        return speed<0?NOT_AVAILABLE:String.valueOf(speed)+unit;
    }

    public String getStringOdometer(String unit) {
        return odometer<0?NOT_AVAILABLE:String.format(Locale.US,"%d.%d%s",odometer/10,odometer%10,unit);
    }

    public String getStringTachometer(String unit) {
        return tachometer<0?NOT_AVAILABLE:String.valueOf(tachometer)+unit;
    }

    public String getStringTorque(String unit) {
        return torque<0?NOT_AVAILABLE:String.valueOf(torque)+unit;
    }
}
