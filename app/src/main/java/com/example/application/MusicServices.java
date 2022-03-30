package com.example.application;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Arrays;

public class MusicServices extends Service {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private VibrationEffect vibrationEffect;
    private SaveDataToSQLite saveDataToSQLite;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AAA", "ON START COMMAND\n");
        saveDataToSQLite = new SaveDataToSQLite(getApplicationContext());
        Bundle bundleGetDataFromBroadcast = intent.getBundleExtra("bundle");
        String state = bundleGetDataFromBroadcast.getString("state");
        TimeElement timeElement = (TimeElement) bundleGetDataFromBroadcast.getSerializable("timeEelement");
        Notification notification = null;
        //KIỂM TRA TRẠNG THÁI LÀ ON HAY OFF VÀ THỰC HIỆN CÁC CHỨC NĂNG TƯƠNG ỨNG
        if (state.equals("on")) {

            NotificationManagerCompat.from(getApplicationContext()).cancel(timeElement.getIdAlarm());
            createNotificationChannel();
            notification = createNotification(intent);
            startForeground(1, notification);
            //NHẠC NGƯỜI DÙNG CÀI CHO BÁO THỨC HOẶC MẶC ĐỊNH
            mediaPlayer = MediaPlayer.create(this, R.raw.perfect_ed);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            //RUNG KHI BÁO THỨC
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (timeElement.getVibrate()) {
                vibrationEffect = VibrationEffect.createWaveform(new long[]{0, 1000, 900, 1000, 900}, 1);
                vibrator.vibrate(vibrationEffect);
            }

            Thread a;
            a = new Thread(new Runnable() {
                @Override
                public void run() {
                    updateInDatabaseAndCancelPendingIntent(timeElement, state, "false");
                }
            });
            a.start();
            try {
                a.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        //TẮT THÔNG BÁO, ÂM THANH VÀ DỪNG RUNG KHI STATE LÀ OFF
        if (state.equals("off")) {

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                vibrator.cancel();
            }

            stopForeground(true);
            String fromButtonTat = bundleGetDataFromBroadcast.getString("fromButtonTat", "false");
            Log.i("AAA", "ON FROM BUTTON TAT 2: " + fromButtonTat);
            if (fromButtonTat.equals("true")) {
                timeElement.setStateOnOrOff(false);
                timeElement.setTimeCountdown("");
                NotificationManagerCompat.from(getApplicationContext()).cancel(timeElement.getIdAlarm());
                Log.i("AAA", "ID ALARM ON OFF IS : " + timeElement.getIdAlarm());
                timeElement.setTimeCountdown("");
                timeElement.setStateOnOrOff(false);
                Thread a;
                a = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveDataToSQLite.queryToUpdateDataToDatabase(timeElement);
                        saveDataToSQLite.updateDataToTablePendingIntent(timeElement.getIdAlarm(), new byte[]{});
                        saveDataToSQLite.close();
                    }
                });
                a.start();
                try {
                    a.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        return START_NOT_STICKY;
    }

    public void updateInDatabaseAndCancelPendingIntent(TimeElement timeElement, String state, String fromButtonTat) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Cursor cursor = new SaveDataToSQLite(getApplicationContext()).queryToGetDataReturn("SELECT * FROM " + SaveDataToSQLite.TABLE_NAME_PENDING_INTENT +
                " WHERE " +
                SaveDataToSQLite.COLUMN_NAME_REQUEST_CODE + "=" + timeElement.getIdAlarm());
        cursor.moveToFirst();
        String textFromCursor = Arrays.toString(cursor.getBlob(1)).replace("[]", "");
        if (!textFromCursor.isEmpty()) {

            if (state.equals("on") || fromButtonTat.equals("true")) {
                @SuppressLint("Recycle")
                Parcel parcel = Parcel.obtain();
                parcel.unmarshall(cursor.getBlob(1), 0, Arrays.toString(cursor.getBlob(1)).length());
                parcel.setDataPosition(0);
                CustomPendingIntent customPendingIntent = (CustomPendingIntent) parcel.readValue(CustomPendingIntent.class.getClassLoader());
                PendingIntent pendingIntent_ = customPendingIntent.getPendingIntent(getApplicationContext());
                pendingIntent_.cancel();
                alarmManager.cancel(pendingIntent_);
            }

            new SaveDataToSQLite(getApplicationContext()).queryToSaveDataToDatabase("UPDATE " + SaveDataToSQLite.TABLE_NAME +
                    " SET " + SaveDataToSQLite.COLUMN_NAME_STATE_ALARM + "=" + "0" +
                    " WHERE " +
                    SaveDataToSQLite.COLUMN_NAME_ID + "=" + timeElement.getIdAlarm());
        }
    }

    // TẠO THÔNG BÁO ĐẨY ĐẾN UI
    public Notification createNotification(Intent a) {

        Bundle sendBundleToUIOverScreen;//= new Bundle();
        sendBundleToUIOverScreen = a.getBundleExtra("bundle");
        TimeElement timeElementT = (TimeElement) sendBundleToUIOverScreen.getSerializable("timeEelement");
        Intent intentAction = new Intent(this, UIOverLockScreen.class);
        intentAction.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentAction.putExtra("bundle", sendBundleToUIOverScreen);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntentAction = PendingIntent.getActivity(this, Integer.parseInt("100"), intentAction, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentTo = new Intent(this, UIOverLockScreen.class);
        intentTo.putExtra("bundle", sendBundleToUIOverScreen);

        //TẠO BUNDLE VÀ THÊM DỮ LIỆU VÀO BUNDLE ĐỂ GỬI ĐI
        Bundle bundleOff = new Bundle();
        Intent intentForButtonTat = new Intent(getApplicationContext(), AlarmFragment.CustomBroadcast.class);

        bundleOff.putString("state", "off");
        bundleOff.putString("fromButtonTat", "true");
        bundleOff.putSerializable("timeEelement", timeElementT);
        intentForButtonTat.putExtra("bundle", bundleOff);

        // TẠO REMOTEVIES CHO CUSTOM NOTIFICATION
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntentForButtonTat = PendingIntent.getBroadcast(getApplicationContext(), 10, intentForButtonTat, PendingIntent.FLAG_CANCEL_CURRENT);
        @SuppressLint("RemoteViewLayout") RemoteViews notificationLayoutCustom = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        notificationLayoutCustom.setTextViewText(R.id.textViewHourNotification, timeElementT.getHour() + ":" + timeElementT.getMinute());
        notificationLayoutCustom.setTextViewText(R.id.textViewNote, timeElementT.getNote());
        notificationLayoutCustom.setOnClickPendingIntent(R.id.buttonTatAlarm, pendingIntentForButtonTat);

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentTo, PendingIntent.FLAG_CANCEL_CURRENT);
        //TẠO THÔNG BÁO
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1111")
                .setSmallIcon(R.drawable.meow)
                .setCustomContentView(notificationLayoutCustom) // CUSTOM LAYOUT CHO THÔNG BÁO
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_MAX)// thử dùng NotificationCompatManager và NotificationManager????
                .setCategory(NotificationCompat.CATEGORY_ALARM) // thử dùng CATEGORY_CALL và các các khác thử xem thế nào???
                .setFullScreenIntent(pendingIntent, true) //SET FULLSCREEN
                .setAutoCancel(true)
                .setContentIntent(pendingIntentAction)
                .setOngoing(false);

        return builder.build();
    }

    //TẠO CHANNEL CHO THÔNG BÁO
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("1111", "channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
//        stopSelf();
        Log.i("AAA", "ON DESTROY SERVICES\n");
    }
}

