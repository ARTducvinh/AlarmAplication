package com.example.application;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationCountDownFullScreen extends AppCompatActivity {

    TextView timeCurrent, dateCurrent, timeCountDown, buttonCloseFullScreen;
    int hour, minutes, seconds;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorStatusBarCountDownOverScreen));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_notification_count_down_full_screen);

        timeCurrent = findViewById(R.id.textViewTimeCurrent);
        dateCurrent = findViewById(R.id.textViewDateCurrent);
        timeCountDown = findViewById(R.id.textViewTimeCountDownEnd);
        buttonCloseFullScreen = findViewById(R.id.buttonCloseFullScreen);

        bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            hour = bundle.getInt("hour", 23);
            minutes = bundle.getInt("minutes", 59);
            seconds = bundle.getInt("seconds", 59);
        }

        Calendar calendar = Calendar.getInstance();

        timeCurrent.post(new Runnable() {
            @Override
            public void run() {
                timeCurrent.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
            }
        });

        dateCurrent.post(new Runnable() {
            @Override
            public void run() {
                dateCurrent.setText(convertDateInternationalToVietnamese(calendar));
            }
        });

        timeCountDown.post(new Runnable() {
            @Override
            public void run() {
                timeCountDown.setText(renderHourMinutesSecondsToString(hour, minutes, seconds));
            }
        });

        buttonCloseFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
                manager.cancel(2908);
                StartForeGroundServicesNotification.vibrator.cancel();
                finishAndRemoveTask();
            }
        });

    }


    public String convertDateInternationalToVietnamese(Calendar calendar) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/E");
        String[] dateFormat = simpleDateFormat.format(calendar.getTime()).split("/");
        return dateFormat[0] + " thg " + dateFormat[1] + dayInWeekInVietNamese(dateFormat[2]);
    }

    public String dayInWeekInVietNamese(String dayInWeek) {
        switch (dayInWeek) {
            case "Th 2":
                return " Thứ 2";
            case "Th 3":
                return " Thứ 3";
            case "Th 4":
                return " Thứ 4";
            case "Th 5":
                return " Thứ 5";
            case "Th 6":
                return " Thứ 6";
            case "Th 7":
                return " Thứ 7";
        }
        return " Chủ nhật";
    }

    public String renderHourMinutesSecondsToString(int a, int b, int c) {

        String temp = (b < 10 ? "0" + b : String.valueOf(b)) + ":" + (c < 10 ? "0" + c : String.valueOf(c));

        if (a == 0) {
            return temp;
        }

        return (a < 10 ? "0" + a : String.valueOf(a)) + ":" + temp;
    }

}