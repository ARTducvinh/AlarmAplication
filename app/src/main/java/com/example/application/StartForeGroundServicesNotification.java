package com.example.application;

import static com.example.application.MusicsItemAdapter.context;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SpecifyJobSchedulerIdRange")
public class StartForeGroundServicesNotification extends JobService {

    public static int posItemHours = 0,posItemMinutes = 0 ,posItemSeconds = 0;
    int tempHour,tempMinutes,tempSeconds;
    public static Timer timer;
    public static TimerTask timerTask;
    public static NotificationManager manager;
    public static final String NOTIFICATION_CHANNEL_ID = "1904";
    public static final int NOTIFICATION_NOTIFY_ID = 2908;
    public static int posPause = 0;


    public static MediaPlayer mediaPlayer;
    public static int[] songs = new int[]{R.raw.perfect_ed};
//    ,R.raw.mp3_jungle_music, R.raw.mpe_summer_night_sound,
//    R.raw.mp3_beach_sound,R.raw.mp3_raining_sound,
//    R.raw.mp3_fire_sound
    public static boolean[] hasStarted = new boolean[]{false,false,false,false,false,false};
    public static CountDownTimer countDownTimer;

    public static int posSongChoice = 0;


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i("AAA","ON START SERVICES");

        //GET BUNDLE EXTRAS
        posItemHours = jobParameters.getExtras().getInt("hour",23);
        posItemMinutes = jobParameters.getExtras().getInt("minutes",59);
        posItemSeconds = jobParameters.getExtras().getInt("seconds",59);

        tempHour = jobParameters.getExtras().getInt("hour",23);
        tempMinutes = jobParameters.getExtras().getInt("minutes",59);
        tempSeconds = jobParameters.getExtras().getInt("seconds",59);

        timer = new Timer();
        createNotificationChannelID();
        NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if(!CountDownTimerFragment.isRunning){
                    pauseMusic();
                    showNotification(posItemHours,posItemMinutes,posItemSeconds,manager);
                    return;
                }
                if(posItemSeconds != 0){
                    posItemSeconds--;
                }
                else{
                    if(posItemHours == 0 && posItemMinutes == 0 && posItemSeconds == 0){
                        if(timerTask != null && timer != null){
                            timerTask.cancel();
                            timer.cancel();
                            timer = null;
                            timerTask = null;
                        }
                        stopForeground(true);
                        showNotificationOnTime();
                        jobFinished(jobParameters,false);
                        return;
                    }
                    posItemSeconds = 59;
                    posItemMinutes--;
                    if(posItemMinutes == -1){
                        posItemMinutes = 59;
                        posItemHours--;

                        if(posItemHours == -1){
                            if(timerTask != null && timer != null){
                                timerTask.cancel();
                                timer.cancel();
                                timer = null;
                                timerTask = null;
                            }
                            stopForeground(true);
                            jobFinished(jobParameters,false);
                            showNotificationOnTime();
                        }
                    }
                }
                showNotification(posItemHours,posItemMinutes,posItemSeconds,manager);
            }
        };

        timer.schedule(timerTask,800,1000);
        return true;
    }


    public void createNotificationChannelID(){
        manager = getApplicationContext().getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"TimeCountDown", NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
    }



    public void showNotification(int hour,int minutes,int seconds,NotificationManager manager){

        //PENDING INTENT FOR CLICK ON NOTIFICATION
        Intent intentClicked = new Intent(this,MainActivity.class);
        Bundle bundleClicked = new Bundle();
        bundleClicked.putBoolean("fromClicked",true);
        bundleClicked.putInt("hour",hour);
        bundleClicked.putInt("minutes",minutes);
        bundleClicked.putInt("seconds",seconds);
//        bundleClicked.putInt("hourOriginal",tempHour);
//        bundleClicked.putInt("minutesOriginal",tempMinutes);
//        bundleClicked.putInt("secondsOriginal",tempSeconds);
        bundleClicked.putBoolean("isRunning", CountDownTimerFragment.isRunning);
        intentClicked.putExtra("bundle",bundleClicked);
        PendingIntent pendingIntentClickedOnNotification = PendingIntent.getActivity(getApplicationContext(),4567,intentClicked,PendingIntent.FLAG_UPDATE_CURRENT);

        //CUSTOM VIEW FOR NOTIFICATION
        Intent intentStopCountDown = new Intent(this, CountDownTimerFragment.SubServices.class);
        Bundle bundleStopCountDown = new Bundle();
        bundleStopCountDown.putBoolean("fromButtonStop",true);
        intentStopCountDown.putExtra("bundle",bundleStopCountDown);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntentStopCountDown = PendingIntent.getService(getApplicationContext(),3456,intentStopCountDown,PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),R.layout.layout_notification_count_down);
        remoteViews.setOnClickPendingIntent(R.id.buttonStopCountDown,pendingIntentStopCountDown);

        if(CountDownTimerFragment.isRunning){
            remoteViews.setTextViewText(R.id.textViewTimeCountdownNotification,renderHourMinutesSecondsToString(hour,minutes,seconds));
            remoteViews.setTextViewText(R.id.textViewStateNotification,"Hẹn giờ đang chạy");
        }
        else{
            remoteViews.setTextViewText(R.id.textViewTimeCountdownNotification,"Hẹn giờ đã tạm dừng");
            remoteViews.setTextViewText(R.id.textViewStateNotification,"Mở hẹn giờ để trở lại");
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(),NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.meow)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(null)
                .setSilent(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntentClickedOnNotification)
                .setCustomContentView(remoteViews)
                .build();

        startForeground(NOTIFICATION_NOTIFY_ID,notification);
    }

    public String renderHourMinutesSecondsToString(int a,int b, int c){

        String temp = (b < 10 ? "0" + b : String.valueOf(b)) +":"+ (c < 10 ? "0" + c : String.valueOf(c));

        if(a == 0){
            return temp;
        }

        return (a < 10 ? "0" + a : String.valueOf(a)) +":"+ temp;
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        manager.cancelAll();
        stopForeground(true);
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(timer != null && timerTask != null){
            timer.cancel();
            timerTask.cancel();
            timerTask = null;
            timer = null;
        }
        stopSelf();
        return true;
    }

    public void showNotificationOnTime(){
        stopMusic();
        //WAKE UP SCREEN ON
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if(!pm.isInteractive()){
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock((268435482), "TAG");
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
        }
        //SET ONCLICK FOR BUTTON "OKE"
        //FULLSCREEN INTENT
        Intent intentClicked = new Intent(this,NotificationCountDownFullScreen.class);
        intentClicked.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        Bundle bundleClicked = new Bundle();
        bundleClicked.putInt("hour",tempHour);
        bundleClicked.putInt("minutes",tempMinutes);
        bundleClicked.putInt("seconds",tempSeconds);
        intentClicked.putExtra("bundle",bundleClicked);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntentClicked = PendingIntent.getActivity(getApplicationContext(),1234,intentClicked,PendingIntent.FLAG_UPDATE_CURRENT);

        //INTENT FOR BUTTON "OK"
        Intent intentButtonOkClicked = new Intent(this, CountDownTimerFragment.SubServices.class);
        Bundle bundleButtonOke = new Bundle();
        bundleButtonOke.putBoolean("fromButtonOke",true);
        intentButtonOkClicked.putExtra("bundle",bundleButtonOke);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntentButtonOkClicked = PendingIntent.getService(getApplicationContext(),2345,intentButtonOkClicked,PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.layout_notitification_on_time_count_down);
        remoteViews.setTextViewText(R.id.textViewOnTimeCountDown,textViewSumTime(tempHour,tempMinutes,tempSeconds));
        remoteViews.setOnClickPendingIntent(R.id.buttonCountDownEnd,pendingIntentButtonOkClicked);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(),NOTIFICATION_CHANNEL_ID)
                                    .setSmallIcon(R.drawable.meow)
                                    .setCustomContentView(remoteViews) // CUSTOM LAYOUT CHO THÔNG BÁO
                                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                                    .setFullScreenIntent(pendingIntentClicked,true) //SET FULLSCREEN
                                    .setAutoCancel(false)
                                    .setContentIntent(pendingIntentClicked)
                                    .setOngoing(true)
                                    .build();

        manager.notify(NOTIFICATION_NOTIFY_ID,notification);
    }


    public String textViewSumTime(int hour,int minute,int seconds){
        return ((hour == 0 ? "": hour + " giờ ")+
                (minute == 0 ? "": minute + " phút ")+
                (seconds == 0 ? "": seconds + " giây")).replaceAll("\\s{2,}"," ").trim();
    }


    public static void startMusic(){
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        if (mediaPlayer == null && posSongChoice != 0) {
            mediaPlayer = MediaPlayer.create(context, songs[posSongChoice]);
            mediaPlayer.setLooping(true);
        }
        if(posSongChoice != 0){
            mediaPlayer.start();
        }
    }

    public static void stopMusic(){
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void pauseMusic(){
        if(mediaPlayer != null){
            posPause = mediaPlayer.getCurrentPosition();
            if(posPause >= mediaPlayer.getDuration()){
                posPause = 0;
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void restartMusic(){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(context, songs[posSongChoice]);
            mediaPlayer.setLooping(true);
        }
        mediaPlayer.seekTo(posPause);
        mediaPlayer.start();
    }


}
