package com.gbmaniac.smartdashboard;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TripLog {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final TripLog ourInstance = new TripLog();
    private static final long INVALID_ID = 0x494E56414C494421L ; //INVALID!
    private static final byte[] HEADER_ID = new byte[]{117,117,-23,-123};   //7575E985
    private static final byte[] HEADER_DATA = new byte[]{26,98,-116,-60};   //1A628CC4
    private static final byte[] HEADER_WARNING = new byte[]{-65,109,-99,91};//BF6D9D5B

    private static LogFile log;
    private static long log_id, uname, motor_id, frame_id, bat_id, ivc_id;
    private static int distance, time, spd_max, spd_avg, pwr_avg, trq_max, trq_avg;
    private static int start_hm, stop_hm, spd_sum, spd_ctr, trq_sum, trq_ctr;
    private static long start_time, stop_time;

    private static class LogFile{
        private byte[] buffer = new byte[1024];
        private int length;

        LogFile(){
            length = 0;
        }
        public void addByte(byte data){
            buffer[length] = data;
            length++;
        }
        void addBytes(byte[] data){
            System.arraycopy(data,0,buffer, length,data.length);
            length +=data.length;
        }

        void addInt(int data){
            addBytes(ByteBuffer.allocate(4).putInt(data).array());
        }

        void addLong(long data){
            addBytes(ByteBuffer.allocate(8).putLong(data).array());
        }

        byte[] getLog(){
            return Arrays.copyOf(buffer, length);
        }

        String getLogString(){
            return bytesToHex(getLog());
        }
        static String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for ( int j = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }

    static TripLog getInstance() {
        return ourInstance;
    }

    private TripLog() {
        log_id = INVALID_ID;
        uname = INVALID_ID;
        motor_id = INVALID_ID;
        frame_id = INVALID_ID;
        bat_id = INVALID_ID;
        ivc_id = INVALID_ID;
    }
    private static void createLog(){
        log = new LogFile();
        //IDs
        log.addBytes(HEADER_ID);
        log.addLong(log_id);
        log.addLong(uname);
        log.addLong(motor_id);
        log.addLong(frame_id);
        log.addLong(bat_id);
        log.addLong(ivc_id);
        //Datas
        distance = stop_hm - start_hm;
        time = (int)(stop_time - start_time);
        if(spd_ctr != 0)
            spd_avg = spd_sum/spd_ctr;
        else
            spd_avg = -1;
        if(trq_ctr != 0)
            trq_avg = trq_sum/trq_ctr;
        else
            trq_avg = -9999;
        log.addBytes(HEADER_DATA);
        log.addInt(distance);
        log.addInt(time);
        log.addInt(spd_max);
        log.addInt(spd_avg);
        log.addInt(trq_max);
        log.addInt(trq_avg);
        //WARNINGS
        log.addBytes(HEADER_WARNING);
    }
    static void StartLogging(int start_hm1, long start_time1){
        start_hm = start_hm1;
        start_time = start_time1;
        spd_sum = 0;
        spd_ctr = 0;
        spd_max = -1;
        trq_sum = 0;
        trq_ctr = 0;
        trq_max = -99999;
    }

    static void StopLogging(int stop_hm1, long stop_time1){
        stop_hm = stop_hm1;
        stop_time = stop_time1;
        createLog();
    }

    public void AddSpeedSample(int spd){
        if(spd > spd_max) spd_max = spd;
        spd_sum += spd;
        spd_ctr++;
    }

    public void AddTorqueSample(int trq){
        if(trq > trq_max) trq_max = trq;
        trq_sum += trq;
        trq_ctr++;
    }

    public void setLog_id(long log_id) {
        TripLog.log_id = log_id;
    }

    public void setUname(long uname) {
        TripLog.uname = uname;
    }

    public void setMotor_id(long motor_id) {
        TripLog.motor_id = motor_id;
    }

    public void setFrame_id(long frame_id) {
        TripLog.frame_id = frame_id;
    }

    public void setBat_id(long bat_id) {
        TripLog.bat_id = bat_id;
    }

    public void setIvc_id(long ivc_id) {
        TripLog.ivc_id = ivc_id;
    }

    public void setDistance(int distance) {
        TripLog.distance = distance;
    }

    public void setTime(int time) {
        TripLog.time = time;
    }

    public void setSpd_avg(int spd_avg) {
        TripLog.spd_avg = spd_avg;
    }

    public void setSpd_max(int spd_max) {
        TripLog.spd_max = spd_max;
    }

    public void setPwr_avg(int pwr_avg) {
        TripLog.pwr_avg = pwr_avg;
    }

    public void setTrq_avg(int trq_avg) {
        TripLog.trq_avg = trq_avg;
    }

    public void setTrq_max(int trq_max) {
        TripLog.trq_max = trq_max;
    }

    public String getLog(){
        return  "\nMotor ID  : "+motor_id+
                "\nFrame ID  : "+frame_id+
                "\nBatt ID   : "+bat_id+
                "\nIVC ID    : "+ivc_id+
                "\nDistance  : "+distance+
                "\nTime      : "+time+
                "\nMax Speed : "+spd_max+
                "\nAvg Speed : "+spd_avg+
                "\nMax Torque: "+trq_max+
                "\nAvg Torque: "+trq_avg+
                "\n";
    }
    public String showLog(){
        return log.getLogString();
    }
}
