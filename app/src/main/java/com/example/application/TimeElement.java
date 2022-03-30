package com.example.application;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class TimeElement implements Serializable{

    //CÁC THUỘC TÍNH
    private String hour, minute;
    private String note;
    private String regular;
    private String timeCountdown;
    private boolean stateOnOrOff = true;
    private boolean vibrate = true;
    private int idAlarm;
    private boolean checkedDelete = false;
    //THUỘC TÍNH PUBLIC
    public static final int STATE_FIX_ALARM = 1,STATE_ADD_ALARM = 0;
    public static final int STATE_ALARM_ON = 1,STATE_ALARM_OFF=0;
    public static final int STATE_VIBRATE_ON = 1,STATE_VIBRATE_OFF=0;

    //HÀM KHỞI TẠO KHÔNG THAM SỐ
    public TimeElement(){}

    //HÀM KHỞI TẠO CÓ THAM SỐ
    public TimeElement(String hour, String minute, String note, String regular, String timeCountdown,boolean state,boolean vibrateE) {
        this.hour = hour;
        this.minute = minute;
        this.note = note;
        this.regular = regular;
        this.timeCountdown = timeCountdown;
        this.stateOnOrOff = state;
        this.vibrate = vibrateE;
        this.checkedDelete = false;
    }


    //GET HOUR
    public String getHour() { return hour; }
    //SET HOUR
    public void setHour(String hour) {
        this.hour = hour;
    }
    //GET MINUTE
    public String  getMinute() {
        return minute;
    }
    //SET MINUTE
    public void setMinute(String minute) {
        this.minute = minute;
    }
    //GET NOTE
    public String getNote() {
        return note;
    }
    //SET NOTE
    public void setNote(String note) {
        this.note = note;
    }
    //GET REGULAR
    public String getRegular() {
        return regular;
    }
    //SET REGULAR
    public void setRegular(String regular) {
        this.regular = regular;
    }
    //GET TIME REMAIN
    public String getTimeCountdown() {
        return timeCountdown;
    }
    //SET  TIME REMAIN
    public void setTimeCountdown(String timeCountdown) {
        this.timeCountdown = timeCountdown;
    }
    //GET STATE ON OR OFF
    public boolean getOnOrOff(){return this.stateOnOrOff;}
    //SET STATE ON OR OFF
    public void setStateOnOrOff(boolean state){this.stateOnOrOff = state;}
    //SET VIBRATE
    public void setVibrate(boolean then){this.vibrate = then;}
    //GET VIBRATE
    public boolean getVibrate(){return this.vibrate;}
    //SET ID FOR ALARM
    public void setIdAlarm(int id){this.idAlarm = id;}
    //GET ID OF ALARM
    public int getIdAlarm(){return this.idAlarm;}
    //GET STATE CHECKED DELETE OR NOT
    public boolean getCheckedDelete(){return this.checkedDelete;}
    //SET STATE CHECKED DELETE OR NOT
    public void setCheckedDelete(boolean checkedDelete){this.checkedDelete = checkedDelete;}
    //OVERRIDE function equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeElement)) return false;
        TimeElement that = (TimeElement) o;
        return stateOnOrOff == that.stateOnOrOff
                && getIdAlarm() == that.getIdAlarm()
                && getHour().equals(that.getHour())
                && getMinute().equals(that.getMinute())
                && getNote().equals(that.getNote())
                && getRegular().equals(that.getRegular())
                && getTimeCountdown().equals(that.getTimeCountdown());
    }

    //OVERRIDE function hashCode
    @Override
    public int hashCode() {
        return Objects.hash(getHour(), getMinute(), getNote(), getRegular(), getTimeCountdown(), stateOnOrOff);
    }

    public int compareTwoObject(TimeElement timeElement){
        Calendar calendar = Calendar.getInstance();
        Date a,b;
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(this.hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(this.minute));
        a = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeElement.hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeElement.minute));
        b = calendar.getTime();
        return Math.toIntExact(a.getTime() - b.getTime());
    }

}