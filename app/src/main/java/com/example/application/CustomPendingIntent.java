package com.example.application;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomPendingIntent implements Parcelable {

    public static final Creator<CustomPendingIntent> CREATOR = new Creator<CustomPendingIntent>() {
        @Override
        public CustomPendingIntent createFromParcel(Parcel in) {
            return new CustomPendingIntent(in);
        }

        @Override
        public CustomPendingIntent[] newArray(int size) {
            return new CustomPendingIntent[size];
        }
    };
    @NonNull
    private final CustomPendingIntentType customPendingIntentType;
    private final int request_code;
    private final int flags;
    @NonNull
    private Intent intent = null;

    private CustomPendingIntent(@NonNull CustomPendingIntentType customPendingIntentType, int request_code, @NonNull Intent intent, int flags) {
        this.customPendingIntentType = customPendingIntentType;
        this.request_code = request_code;
        this.intent = intent;
        this.flags = flags;
    }

    protected CustomPendingIntent(Parcel in) {
        this.customPendingIntentType = CustomPendingIntentType.values()[in.readInt()];
        this.request_code = in.readInt();
        this.flags = in.readInt();
        this.intent = in.readParcelable(Intent.class.getClassLoader());
    }

    public static CustomPendingIntent getServices(int request_code, @NonNull Intent intent, int flags) {
        return new CustomPendingIntent(CustomPendingIntentType.SERVICE, request_code, intent, flags);
    }

    public static CustomPendingIntent getBroadcast(int request_code, @NonNull Intent intent, int flags) {
        return new CustomPendingIntent(CustomPendingIntentType.BROADCAST, request_code, intent, flags);
    }

    public static CustomPendingIntent getActivity(int request_code, @NonNull Intent intent, int flags) {
        return new CustomPendingIntent(CustomPendingIntentType.ACTIVITY, request_code, intent, flags);
    }

    @Nullable
    public PendingIntent getPendingIntent(@NonNull Context context) {
        PendingIntent pendingIntent = null;
        switch (this.customPendingIntentType) {
            case SERVICE:
                pendingIntent = PendingIntent.getService(context, request_code, intent, flags);
                break;
            case BROADCAST:
                pendingIntent = PendingIntent.getBroadcast(context, request_code, intent, flags);
                break;
            case ACTIVITY:
                pendingIntent = PendingIntent.getActivity(context, request_code, intent, flags);
                break;
        }
        return pendingIntent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.customPendingIntentType.ordinal());
        parcel.writeInt(this.request_code);
        parcel.writeInt(this.flags);
        parcel.writeParcelable(this.intent, i);
    }

    public CustomPendingIntentType getCustomPendingIntentType() {
        return customPendingIntentType;
    }

    public int getRequest_code() {
        return request_code;
    }

    private enum CustomPendingIntentType {SERVICE, BROADCAST, ACTIVITY}
}
