package com.gbmaniac.smartdashboard;

public class Motor {
    private long _id;
    private long frame_id;
    private boolean failure;
    private int temperature;
    private int voltage;
    private int current;
    private boolean signLeft, signRight, headLamp;
    private boolean signLampFailure, brakeLampFailure, headLampFailure;


    public Motor(){
        _id = -1;
        failure = true;
        temperature = -1;
        voltage = -1;
        current = -1;
        signLeft = false;
        signRight = false;
        headLamp = false;
        signLampFailure = true;
        brakeLampFailure = true;
        headLampFailure = true;
    }

    public boolean set_id(long _id) {
        boolean ret = this._id != _id;
        this._id = _id;
        return ret;
    }

    public boolean setFrame_id(long frame_id) {
        boolean ret = this.frame_id != frame_id;
        this.frame_id = frame_id;
        return ret;
    }

    public boolean setFailure(boolean failure) {
        boolean ret = this.failure != failure;
        this.failure = failure;
        return ret;
    }

    public boolean setTemperature(int temperature) {
        boolean ret = this.temperature != temperature;
        this.temperature = temperature<0?0:temperature;
        return ret;
    }

    public boolean setVoltage(int voltage) {
        boolean ret = this.voltage != voltage;
        this.voltage = voltage<0?0:voltage;
        return ret;
    }

    public boolean setCurrent(int current) {
        boolean ret = this.current != current;
        this.current = current;
        return ret;
    }

    public boolean setSignLeft(boolean signLeft) {
        boolean ret = this.signLeft != signLeft;
        this.signLeft = signLeft;
        return ret;
    }

    public boolean setSignRight(boolean signRight) {
        boolean ret = this.signRight != signRight;
        this.signRight = signRight;
        return ret;
    }

    public boolean setHeadLamp(boolean headLamp) {
        boolean ret = this.headLamp != headLamp;
        this.headLamp = headLamp;
        return ret;
    }

    public boolean setSignLampFailure(boolean signLampFailure) {
        boolean ret = this.signLampFailure != signLampFailure;
        this.signLampFailure = signLampFailure;
        return ret;
    }

    public boolean setBrakeLampFailure(boolean brakeLampFailure) {
        boolean ret = this.brakeLampFailure != brakeLampFailure;
        this.brakeLampFailure = brakeLampFailure;
        return ret;
    }

    public boolean setHeadLampFailure(boolean headLampFailure) {
        boolean ret = this.headLampFailure != headLampFailure;
        this.headLampFailure = headLampFailure;
        return ret;
    }

    public long get_id() {
        return _id;
    }

    public long getFrame_id() {
        return frame_id;
    }

    public boolean isFailure() {
        return failure;
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

    public boolean isSignLeft() {
        return signLeft;
    }

    public boolean isSignRight() {
        return signRight;
    }

    public boolean isHeadLamp() {
        return headLamp;
    }

    public boolean isSignLampFailure() {
        return signLampFailure;
    }

    public boolean isBrakeLampFailure() {
        return brakeLampFailure;
    }

    public boolean isHeadLampFailure() {
        return headLampFailure;
    }
}
