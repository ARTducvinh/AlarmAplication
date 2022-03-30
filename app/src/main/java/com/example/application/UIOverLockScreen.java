package com.example.application;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UIOverLockScreen extends AppCompatActivity {

    TextView swipeUpToClose, textViewShowTimeOnTime, textViewNote,buttonSetAlarmRingAfter5Minutes
            ,textViewDateUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("AAA","ON CREATE UI\n");
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorStatusBarUIOverScreen));
        setContentView(R.layout.ui_over_lock_screen);
        //NHẬN BUNDLE GỬI ĐẾN TỪ MUSIC SERVICES
        Bundle bundle = getIntent().getBundleExtra("bundle");
        TimeElement timeElement = (TimeElement) bundle.getSerializable("timeEelement");
        //ÁNH XẠ VIEW
        swipeUpToClose = (TextView) findViewById(R.id.swipUpToClose);
        textViewShowTimeOnTime = (TextView) findViewById(R.id.textViewHourUI);
        textViewNote = (TextView)findViewById(R.id.textViewGoalUI);
        buttonSetAlarmRingAfter5Minutes = (TextView)findViewById(R.id.imageViewTimeAgainUI);
        textViewDateUI = (TextView)findViewById(R.id.textViewDateUI);
        arrowStartAnimation();
        setTextViewShowTimeOnTime(timeElement.getHour() + ":" + timeElement.getMinute(),timeElement.getNote());
        swipeUpToClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    cancelAlarm(timeElement);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finishAndRemoveTask();
            }
        });

        //NÚT ĐẶT LẠI BÁO THỨC VÀ BÁO THỨC SAU 5 PHÚT NỮA
        buttonSetAlarmRingAfter5Minutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //KHỞI TẠO 1 CALENDAR VÀ SET THỜI GIAN HIỆN TẠO LÀ CỦA BÁO THỨC VỪA BÁO XONG
                //ĐỂ BÁO LẠI SAU 5 PHÚT
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");

                Calendar calendar = Calendar.getInstance();
                //TẠO BÁO THỨC BÁO SAU 5 PHÚT NỮA
                calendar.add(Calendar.MINUTE,5);

                String timeAfter5Minutes = simpleDateFormat.format(calendar.getTime());
                String hour = timeAfter5Minutes.split(":")[0];
                String minutes = timeAfter5Minutes.split(":")[1];
                Log.i("AAA","TIME AFTER 5 MINUTES : "+hour+":"+minutes);
                timeElement.setHour(hour);timeElement.setMinute(minutes);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), AlarmFragment.CustomBroadcast.class);
                Bundle bundleSendToBroadcast = new Bundle();
                bundleSendToBroadcast.putString("state","on");
                bundleSendToBroadcast.putSerializable("timeEelement",timeElement);

                intent.setAction("runBackground");
                intent.putExtra("bundle",bundleSendToBroadcast);

                Bundle bundlee = getIntent().getBundleExtra("bundle");
                TimeElement timeElementt = (TimeElement) bundlee.getSerializable("timeEelement");
                //HỦY BÁO THỨC TRƯỚC ĐANG BÁO
                try {
                    cancelAlarm(timeElementt);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //TẮT XONG THÌ PHẢI UPDATE LẠI UI CỦA BÁO THỨC VỪA BÁO XONG(TẮT QUA THÔNG BÁO)
                //TẠO BÁO THỨC LẶP LẠI SAU 5 PHÚT
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent_ = PendingIntent.getBroadcast(getApplicationContext(),timeElement.getIdAlarm(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
                //SAVE PENDING INTENT DATA TO DATABASE
                Parcel parcel = Parcel.obtain();
                CustomPendingIntent customPendingIntent = CustomPendingIntent.getBroadcast(timeElement.getIdAlarm(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
                parcel.writeValue(customPendingIntent);
                byte[] bytes = parcel.marshall();
                parcel.recycle();
                Thread a;
                a = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SaveDataToSQLite(getApplicationContext()).saveDataToTablePendingIntent(timeElement.getIdAlarm(),bytes);
                    }
                });
                a.start();
                try {
                    a.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //REMOVE ACTIVITY HIỆN TẠI
                finishAndRemoveTask();
            }
        });
    }

    //ANIMATION CHO NÚT MŨI TÊN
    public void arrowStartAnimation(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.anim_arrow_move_up);
        swipeUpToClose.setAnimation(animation);
    }

    public void showWhenLockedAndTurnScreenOn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
    }


    //SET TEXT NOTE VÀ THỜI GIAN HẸN BÁO THỨC
    public void setTextViewShowTimeOnTime(String time,String note){
        new Thread(new Runnable() {
            @Override
            public void run() {
                textViewShowTimeOnTime.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewShowTimeOnTime.setText(time);
                        Log.i("AAA","CLMMM CHAY DI\n");
                    }
                });
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                     textViewNote.post(new Runnable() {
                         @Override
                         public void run() {
                            textViewNote.setText(note);
                         }
                     });
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                textViewDateUI.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewDateUI.setText(convertDateInternationalToVietnamese());
                    }
                });
            }
        }).start();
    }

    //HỦY BÁO THỨC VỪA BÁO
    public void cancelAlarm(TimeElement timeElement) throws InterruptedException {
        Thread a;
        a = new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle bundleOff = new Bundle();
                Intent intent = new Intent(UIOverLockScreen.this, AlarmFragment.CustomBroadcast.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                bundleOff.putString("state","off");
                bundleOff.putSerializable("timeEelement",timeElement);
                //bundleOff.putParcelable("pendingIntent",pendingIntent);
                intent.putExtra("bundle",bundleOff);
                //intent.putExtra("pendingIntent",pendingIntent);
                Log.i("AAA","ON UI SENDBROADCAST\n");
                //DELETE ALARM IN DATABASE
                new SaveDataToSQLite(getApplicationContext()).queryToSaveDataToDatabase("UPDATE "+SaveDataToSQLite.TABLE_NAME +
                        " SET "+SaveDataToSQLite.COLUMN_NAME_STATE_ALARM+"="+"0"+
                        " WHERE "+
                        SaveDataToSQLite.COLUMN_NAME_ID+"="+timeElement.getIdAlarm());

                sendBroadcast(intent);
            }
        });
        a.start();
        a.join();

    }


    public String convertDateInternationalToVietnamese(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/E");
        String[] dateFormat = simpleDateFormat.format(calendar.getTime()).split("/");
        Log.i("AAA","TIME IS SETTES : "+dateFormat[0]);
        Log.i("AAA","TIME IS SETTES : "+dateFormat[1]);
        Log.i("AAA","TIME IS SETTES : "+dateFormat[2]);
        return dateFormat[0] + " thg " + dateFormat[1] + dayInWeekInVietNamese(dateFormat[2]);
    }

    public String dayInWeekInVietNamese(String dayInWeek){
        switch (dayInWeek){
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("AAA","ON UI DESTROY\n");
    }
}