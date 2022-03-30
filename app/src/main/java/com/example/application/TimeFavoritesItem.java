package com.example.application;

import java.util.Objects;

public class TimeFavoritesItem {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_SPECIAL = 1;
    int hour;
    int minute;
    int type;

    public TimeFavoritesItem(int hour, int minute, int type) {
        this.hour = hour;
        this.minute = minute;
        this.type = type;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int calculateTimeToMinutes() {
        return (hour * 60) + minute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFavoritesItem item = (TimeFavoritesItem) o;
        return hour == item.hour && minute == item.minute && type == item.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, minute, type);
    }
}
