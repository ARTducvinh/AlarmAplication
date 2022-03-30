package com.example.application;

import static com.example.application.StartForeGroundServicesNotification.mediaPlayer;
import static com.example.application.StartForeGroundServicesNotification.pauseMusic;
import static com.example.application.StartForeGroundServicesNotification.posPause;
import static com.example.application.StartForeGroundServicesNotification.posSongChoice;
import static com.example.application.StartForeGroundServicesNotification.songs;

import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CountDownTimerFragment extends Fragment {

    public static MainActivity mMainActivity;
    public static LinearLayout pickerLayout, layoutCountDown;
    public static boolean stateFromButtonStopNotification = false;
    public static int posItemSeconds = 0, posItemMinutes = 0, posItemHours = 0;
    public static int JOB_ID = 1904;
    public static boolean isRunning = false, isStarted = false;
    public static TimerTask timerTask;
    public static Timer timer;
    public boolean isIsRunning = false;
    Context context;
    NumberPicker hoursPicker, minutesPicker, secondsPicker;
    List<String> hours = new ArrayList<>();
    List<String> minutes = new ArrayList<>();
    List<String> seconds = new ArrayList<>();
    List<MusicsItem> itemListMusics = new ArrayList<>();
    List<TimeFavoritesItem> timeFavoritesItems = new ArrayList<>();
    RecyclerView recyclerViewMusics, recyclerViewTimeListNotes;
    TimeFavoritesItemAdapter favoritesItemAdapter;
    MusicsItemAdapter musicsItemAdapter;
    int posItemSecondsOriginal = 0, posItemMinutesOriginal = 0, posItemHoursOriginal = 0;
    TextView colonFirst, colonSecond, textViewHours, textViewMinutes, textViewSeconds, textViewSumTime;
    boolean isSubtractSeconds = false, isSubtractMinutes = false,
            isSubtractHours = false, hasChangedStateButtonStartCountDownToDisable = false, hasChangedStateButtonStartCountDownToEnable = false;
    private View view;


    public CountDownTimerFragment(MainActivity mainActivity, Context context, int a, int b, int c, boolean d, int e, int f, int g) {
        mMainActivity = mainActivity;
        this.context = context;
        this.posItemHoursOriginal = a;
        this.posItemMinutesOriginal = b;
        this.posItemSecondsOriginal = c;
        posItemHours = e;
        posItemMinutes = f;
        posItemSeconds = g;
        this.isIsRunning = d;
        initHoursList();
        initMinutesList();
        initSecondsList();
        initMusicsItemList();
        initTimeFavoritesItemList();
        initTheLastItemChoice();
        //createNotificationChannelID();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.count_down_timer_fragment, container, false);

        //HOOKS VIEWS
        hoursPicker = view.findViewById(R.id.hoursPicker);
        minutesPicker = view.findViewById(R.id.minutesPicker);
        secondsPicker = view.findViewById(R.id.secondsPicker);
        recyclerViewMusics = view.findViewById(R.id.recyclerViewMusics);
        recyclerViewTimeListNotes = view.findViewById(R.id.recyclerViewListTimeFavorites);


        pickerLayout = view.findViewById(R.id.layoutNumberPicker);
        layoutCountDown = view.findViewById(R.id.layoutCountDown);
        colonFirst = view.findViewById(R.id.colonFirst);
        colonSecond = view.findViewById(R.id.colonSecond);
        textViewHours = view.findViewById(R.id.textViewHourCountDown);
        textViewMinutes = view.findViewById(R.id.textViewMinutesCountDown);
        textViewSeconds = view.findViewById(R.id.textViewSecondsCountDown);
        textViewSumTime = view.findViewById(R.id.textViewSumTime);
        //SET VALUES FOR NUMBER PICKER
        //HOURS PICKER
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(hours.size() - 1);
        hoursPicker.setDisplayedValues(hours.toArray(new String[]{}));
        hoursPicker.setValue(posItemHoursOriginal);
        //MINUTES PICKER
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(minutes.size() - 1);
        minutesPicker.setDisplayedValues(minutes.toArray(new String[]{}));
        minutesPicker.setValue(posItemMinutesOriginal);
        //SECONDS PICKER
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(seconds.size() - 1);
        secondsPicker.setDisplayedValues(seconds.toArray(new String[]{}));
        secondsPicker.setValue(posItemSecondsOriginal);

        //HIDE AND SHOW VIEW FROM BUTTON STOP NOTIFICATION
        if (stateFromButtonStopNotification) {
            if (mMainActivity.viewPager2.getCurrentItem() == 3) {
                mMainActivity.showFloatingButtonAtFragment3(View.VISIBLE);
            } else {
                mMainActivity.showFloatingButtonAtFragment3(View.GONE);
            }
            mMainActivity.showButtonPauseAndStartCountDown(View.GONE);
            mMainActivity.showButtonResetCountDown(View.GONE);
            mMainActivity.showLayoutCountDown(View.GONE);
            mMainActivity.showLayoutNumberPicker(View.VISIBLE);
            stateFromButtonStopNotification = false;
        }

        //SET ON SCROLL FOR THREES PICKER
        hoursPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                changeStateButtonStartCountDown();
                showRecyclerViewTimeFavorites(View.GONE);
                showRecyclerViewMusicItemList(View.GONE);
                mMainActivity.changeStateButtonSetTimeFavoritesClose();
                mMainActivity.changeStateButtonSetMusicsClose();
            }
        });

        minutesPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                changeStateButtonStartCountDown();
                showRecyclerViewTimeFavorites(View.GONE);
                showRecyclerViewMusicItemList(View.GONE);
                mMainActivity.changeStateButtonSetTimeFavoritesClose();
                mMainActivity.changeStateButtonSetMusicsClose();
            }
        });

        secondsPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                changeStateButtonStartCountDown();
                showRecyclerViewTimeFavorites(View.GONE);
                showRecyclerViewMusicItemList(View.GONE);
                mMainActivity.changeStateButtonSetTimeFavoritesClose();
                mMainActivity.changeStateButtonSetMusicsClose();
            }
        });

        if (hoursPicker.getValue() == 0 && minutesPicker.getValue() == 0 && secondsPicker.getValue() == 0) {
            mMainActivity.changeStateEnableButtonStartCountDown(1);
        }

        //INIT 3 RECYCLERS VIEW
        textViewHours.setText(posItemHours < 10 ? "0" + posItemHours : String.valueOf(posItemHours));
        textViewMinutes.setText(posItemMinutes < 10 ? "0" + posItemMinutes : String.valueOf(posItemMinutes));
        textViewSeconds.setText(posItemSeconds < 10 ? "0" + posItemSeconds : String.valueOf(posItemSeconds));

        //SET ADAPTER FOR RECYCLER VIEW MUSICS LIST ITEM
        musicsItemAdapter = new MusicsItemAdapter(getContext(), itemListMusics);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewMusics.setHasFixedSize(true);
        recyclerViewMusics.setLayoutManager(layoutManager);
        recyclerViewMusics.setAdapter(musicsItemAdapter);

        //SET ADAPTER FOR RECYCLER VIEW TIME FAVORITES ITEM
        favoritesItemAdapter = new TimeFavoritesItemAdapter(getContext(), timeFavoritesItems, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewTimeListNotes.setHasFixedSize(true);
        recyclerViewTimeListNotes.setLayoutManager(linearLayoutManager);
        recyclerViewTimeListNotes.setAdapter(favoritesItemAdapter);

        if (isIsRunning && StartForeGroundServicesNotification.timer != null && StartForeGroundServicesNotification.timerTask != null) {
            Log.i("AAA", "IS RUNNING");
            isRunning = true;
            isStarted = true;
            mMainActivity.startCountDown = true;
            mMainActivity.showFloatingButtonAtFragment3(View.GONE);
            mMainActivity.showButtonResetCountDown(View.VISIBLE);
            mMainActivity.showButtonPauseAndStartCountDown(View.VISIBLE);
            mMainActivity.showFloatingStartCountTimeUp(View.GONE);
            mMainActivity.setTextViewSumTime(posItemHoursOriginal, posItemMinutesOriginal, posItemSecondsOriginal);
            showLayoutCountDown(View.VISIBLE);
            showLayoutNumberPicker(View.GONE);

            if (StartForeGroundServicesNotification.timer != null && StartForeGroundServicesNotification.timerTask != null) {
                StartForeGroundServicesNotification.timerTask.cancel();
                StartForeGroundServicesNotification.timer.cancel();
                StartForeGroundServicesNotification.timer = null;
                StartForeGroundServicesNotification.timerTask = null;
            }

            if (timer != null && timerTask != null) {
                timerTask.cancel();
                timer.cancel();
                timerTask = null;
                timer = null;
            }

            mMainActivity.floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
                @Override
                public void run() {
                    mMainActivity.floatingActionButtonStartAndPauseCountDown.setImageResource(R.drawable.ic_pause);
                }
            });
            startCountDownFunction(StartForeGroundServicesNotification.posItemHours, StartForeGroundServicesNotification.posItemMinutes, StartForeGroundServicesNotification.posItemSeconds - 1);
        } else if (!isIsRunning && StartForeGroundServicesNotification.timer != null && StartForeGroundServicesNotification.timerTask != null) {
            Log.i("AAA", "IS STOPPED");
            pauseMusic();
            isRunning = false;
            isStarted = false;
            mMainActivity.startCountDown = false;
            mMainActivity.showFloatingButtonAtFragment3(View.GONE);
            mMainActivity.showButtonResetCountDown(View.VISIBLE);
            mMainActivity.showButtonPauseAndStartCountDown(View.VISIBLE);
            mMainActivity.showFloatingStartCountTimeUp(View.GONE);
            mMainActivity.setTextViewSumTime(posItemHoursOriginal, posItemMinutesOriginal, posItemSecondsOriginal);
            showLayoutCountDown(View.VISIBLE);
            showLayoutNumberPicker(View.GONE);

            if (StartForeGroundServicesNotification.timer != null && StartForeGroundServicesNotification.timerTask != null) {
                StartForeGroundServicesNotification.timerTask.cancel();
                StartForeGroundServicesNotification.timer.cancel();
                StartForeGroundServicesNotification.timer = null;
                StartForeGroundServicesNotification.timerTask = null;
            }

            if (timer != null && timerTask != null) {
                timerTask.cancel();
                timer.cancel();
                timerTask = null;
                timer = null;
            }

            startCountDownFunction(StartForeGroundServicesNotification.posItemHours, StartForeGroundServicesNotification.posItemMinutes, StartForeGroundServicesNotification.posItemSeconds);
            isRunning = false;
            mMainActivity.floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
                @Override
                public void run() {
                    mMainActivity.floatingActionButtonStartAndPauseCountDown.setImageResource(R.drawable.ic_start_count_up);
                }
            });
        }

        return view;
    }

    public void initHoursList() {
        for (int i = 0; i <= 23; i++) {
            hours.add(i < 10 ? "0" + i : String.valueOf(i));
        }
    }

    public void initMinutesList() {
        for (int i = 0; i <= 59; i++) {
            minutes.add(i < 10 ? "0" + i : String.valueOf(i));
        }
    }

    public void initSecondsList() {
        for (int i = 0; i <= 59; i++) {
            seconds.add(i < 10 ? "0" + i : String.valueOf(i));
        }
    }

    public void initMusicsItemList() {
        MusicsItem item1 = new MusicsItem(R.raw.perfect_ed, R.drawable.ic_default, "Mặc định");
        MusicsItem item2 = new MusicsItem(R.raw.perfect_ed, R.drawable.ic_tree, "Tiếng rừng");
        MusicsItem item3 = new MusicsItem(R.raw.perfect_ed, R.drawable.ic_moon, "Đêm hè");
        MusicsItem item4 = new MusicsItem(R.raw.perfect_ed, R.drawable.ic_beach, "Biển cả");
        MusicsItem item5 = new MusicsItem(R.raw.perfect_ed, R.drawable.ic_rain, "Mưa xuân");
        MusicsItem item6 = new MusicsItem(R.raw.perfect_ed, R.drawable.ic_flame, "Bếp lửa");
        itemListMusics.add(item1);
        itemListMusics.add(item2);
        itemListMusics.add(item3);
        itemListMusics.add(item4);
        itemListMusics.add(item5);
        itemListMusics.add(item6);
    }

    public void initTimeFavoritesItemList() {
        TimeFavoritesItem item1 = new TimeFavoritesItem(0, 5, TimeFavoritesItem.TYPE_SPECIAL);
        timeFavoritesItems.add(item1);
    }

    public void setValuesForHourMinutesSeconds(int a, int b, int c) {
        posItemHours = a;
        posItemMinutes = b;
        posItemSeconds = c;
    }


    public void showRecyclerViewMusicItemList(int visibility) {
        recyclerViewMusics.setVisibility(visibility);
    }

    public void showRecyclerViewTimeFavoritesList(int visibility) {
        recyclerViewTimeListNotes.setVisibility(visibility);
    }

    public void showTextViewSecondsCountDown(int visibility) {
        textViewSeconds.setVisibility(visibility);
    }

    public void showTextViewMinutesCountDown(int visibility) {
        textViewMinutes.setVisibility(visibility);
    }

    public void showTextViewHoursCountDown(int visibility) {
        textViewHours.setVisibility(visibility);
    }

    public void showTextViewSumTime(int visibility) {
        textViewSumTime.setVisibility(visibility);
    }

    public void showRecyclerViewMusics(int visibility) {
        recyclerViewMusics.post(new Runnable() {
            @Override
            public void run() {
                recyclerViewMusics.setVisibility(visibility);
            }
        });
    }

    public void showRecyclerViewTimeFavorites(int visibility) {
        recyclerViewTimeListNotes.post(new Runnable() {
            @Override
            public void run() {
                recyclerViewTimeListNotes.setVisibility(visibility);
            }
        });
    }

    public void showColonFirst(int visibility) {
        colonFirst.setVisibility(visibility);
    }

    public void showColonSecond(int visibility) {
        colonSecond.setVisibility(visibility);
    }

    public void showLayoutNumberPicker(int visibility) {
        pickerLayout.post(new Runnable() {
            @Override
            public void run() {
                pickerLayout.setVisibility(visibility);
            }
        });
    }

    public void showLayoutCountDown(int visibility) {
        layoutCountDown.post(new Runnable() {
            @Override
            public void run() {
                layoutCountDown.setVisibility(visibility);
            }
        });
    }

    public void changeUICountDown(int hour, int minute, int seconds) {
        textViewHours.post(new Runnable() {
            @Override
            public void run() {
                textViewHours.setText(posItemHours < 10 ? "0" + posItemHours : String.valueOf(posItemHours));
            }
        });
        textViewMinutes.post(new Runnable() {
            @Override
            public void run() {
                textViewMinutes.setText(posItemMinutes < 10 ? "0" + posItemMinutes : String.valueOf(posItemMinutes));
            }
        });
        textViewSeconds.post(new Runnable() {
            @Override
            public void run() {
                textViewSeconds.setText(posItemSeconds < 10 ? "0" + posItemSeconds : String.valueOf(posItemSeconds));
            }
        });
    }

    public void changeStateVisibleAllViews() {
        favoritesItemAdapter.changeStateAllViews();
        //favoritesItemAdapter.changeStateDeleteForFAll();
    }

    public void startCountDownFunction(int hour, int minute, int seconds) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        isRunning = true;
        posItemHours = hour;
        posItemMinutes = minute;
        posItemSeconds = seconds;
        changeUICountDown(hour, minute, seconds);
        isStarted = true;
        //SCHEDULE TIME COUNT DOWN
        if (timer == null && timerTask == null) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (!isRunning) {
                        StartForeGroundServicesNotification.pauseMusic();
                        return;
                    }
                    if (posItemSeconds != 0) {
                        textViewSeconds.setText(posItemSeconds < 10 ? "0" + posItemSeconds : String.valueOf(posItemSeconds));
                        if (isSubtractMinutes) {
                            textViewMinutes.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewMinutes.setText(posItemMinutes < 10 ? "0" + posItemMinutes : String.valueOf(posItemMinutes));
                                }
                            });
                            isSubtractMinutes = false;
                        }
                        if (isSubtractHours) {
                            textViewHours.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewHours.setText(posItemHours < 10 ? "0" + posItemHours : String.valueOf(posItemHours));
                                }
                            });
                            isSubtractHours = false;
                        }
                        posItemSeconds--;
                    } else {

                        posItemSeconds = 59;
                        textViewSeconds.setText("00");

                        posItemMinutes--;
                        isSubtractMinutes = true;

                        if (posItemMinutes == -1) {

                            posItemMinutes = 59;

                            posItemHours--;
                            isSubtractHours = true;

                            if (posItemHours == -1) {
                                timerTask.cancel();
                                timer.cancel();
                                timerTask = null;
                                timer = null;
                                isStarted = false;
                                showLayoutNumberPicker(View.VISIBLE);
                                showLayoutCountDown(View.GONE);
                                mMainActivity.showFloatingButtonAtFragment3(View.VISIBLE);
                                mMainActivity.showButtonResetCountDown(View.GONE);
                                mMainActivity.showButtonPauseAndStartCountDown(View.GONE);
                                mMainActivity.startCountDown = false;
                            }
                        }
                    }
                }
            };
            timer.schedule(timerTask, 100, 1000);

            //START FOREGROUND SERVICES USING JOB SCHEDULER
            ComponentName componentName = new ComponentName(context, StartForeGroundServicesNotification.class);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt("hour", posItemHours);
            bundle.putInt("minutes", posItemMinutes);
            bundle.putInt("seconds", posItemSeconds);
            JOB_ID++;
            List<JobInfo> jobInfoList = jobScheduler.getAllPendingJobs();

            JobInfo info;
//            if(!jobInfoList.isEmpty()){
//                Log.i("AAA","JOB LIST NOT NULL");
//                info = jobInfoList.get(0);
//                bundle.putInt("hour",hour);
//                bundle.putInt("minutes",minute);
//                bundle.putInt("seconds",seconds);
//            }
//            else{
//            }
            info = new JobInfo.Builder(JOB_ID, componentName)
                    .setExtras(bundle)
                    .build();
            jobScheduler.schedule(info);

        }
    }

    public void stopCountDown() {
        isRunning = false;
        if (StartForeGroundServicesNotification.timer != null && StartForeGroundServicesNotification.timerTask != null) {
            StartForeGroundServicesNotification.timerTask.cancel();
            StartForeGroundServicesNotification.timer.cancel();
            StartForeGroundServicesNotification.timer = null;
            StartForeGroundServicesNotification.timerTask = null;
        }
        posItemHours = 23;
        posItemMinutes = 59;
        posItemSeconds = 59;
        isStarted = false;
        posItemSeconds++;
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancelAll();
        if (timer != null && timerTask != null) {
            timerTask.cancel();
            timer.cancel();
            timerTask = null;
            timer = null;
        }
    }


    public int getHoursReturn() {
        return hoursPicker.getValue();
    }

    public int getMinutesReturn() {
        return minutesPicker.getValue();
    }

    public int getSecondsReturn() {
        return secondsPicker.getValue();
    }


    public int getHoursReturnTextView() {
        return Integer.parseInt(textViewHours.getText().toString());
    }

    public int getMinutesReturnTextView() {
        return Integer.parseInt(textViewMinutes.getText().toString());
    }

    public int getSecondsReturnTextView() {
        return Integer.parseInt(textViewSeconds.getText().toString());
    }

    public void changeStateButtonStartCountDown() {
        if (!hasChangedStateButtonStartCountDownToDisable && hoursPicker.getValue() == 0 && minutesPicker.getValue() == 0 && secondsPicker.getValue() == 0) {
            mMainActivity.changeStateEnableButtonStartCountDown(1);
            hasChangedStateButtonStartCountDownToDisable = true;
            hasChangedStateButtonStartCountDownToEnable = false;
        }
        if (!hasChangedStateButtonStartCountDownToEnable && (hoursPicker.getValue() != 0 || minutesPicker.getValue() != 0 || secondsPicker.getValue() != 0)) {
            mMainActivity.changeStateEnableButtonStartCountDown(0);
            hasChangedStateButtonStartCountDownToDisable = false;
            hasChangedStateButtonStartCountDownToEnable = true;
        }
    }

    public void restoreTheLastTimeCountDown() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("timeCountDown", Context.MODE_PRIVATE);
        posItemHoursOriginal = sharedPreferences.getInt("hour", 0);
        posItemMinutes = sharedPreferences.getInt("minutes", 0);
        posItemSeconds = sharedPreferences.getInt("seconds", 0);
    }

    public void initTheLastItemChoice() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("timeCountDown", Context.MODE_PRIVATE);
        posSongChoice = sharedPreferences.getInt("posSongChoice", 0);
        Log.i("AAA", "POSSITION CHOICES : " + posSongChoice);
        if (mediaPlayer != null) {
            Log.i("AAA", "OBJECT NOT NULL");
            Log.i("AAA", "POSITION PAUSE START INIT BEFORE : " + mediaPlayer.getCurrentPosition());
            int posSeek = mediaPlayer.getCurrentPosition();
            if (posSeek >= mediaPlayer.getDuration()) {
                posSeek = 0;
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPlayer = MediaPlayer.create(context, songs[posSongChoice]);
            mediaPlayer.seekTo(posSeek);
            mediaPlayer.setLooping(true);
            Log.i("AAA", "POSITION PAUSE START INIT AFTER : " + mediaPlayer.getCurrentPosition());
            if (isIsRunning && posSongChoice != 0) {
                mediaPlayer.start();
            }
//            else{
//                if(!isIsRunning && posSongChoice != 0){
//
//                }
//            }
        } else {
            Log.i("AAA", "OBJECT NULL");
            if (posSongChoice != 0) {
                SharedPreferences sharedPreferencesPos = context.getSharedPreferences("timeCountDown", Context.MODE_PRIVATE);
                int pos = sharedPreferencesPos.getInt("posMediaPlayer", 0);
                Log.i("AAA", "POSSAVED GET : " + pos);
                mediaPlayer = MediaPlayer.create(context, songs[posSongChoice]);
                mediaPlayer.seekTo(pos);
                mediaPlayer.setLooping(true);
                if (isIsRunning && posSongChoice != 0) {
                    mediaPlayer.start();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SAVE THE LAST POSITION OF MUSICS WHEN PAUSE AND THEN DESTROY
        if (!isRunning) {
            if (timer != null && timerTask != null) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("timeCountDown", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("posMediaPlayer", posPause);
                Log.i("AAA", "POSSAVED : " + posPause);
                editor.commit();
            }
        }
    }

    public static class SubServices extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Bundle bundle = intent.getBundleExtra("bundle");

            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);

            boolean fromButtonStop = bundle.getBoolean("fromButtonStop", false);
            boolean fromButtonOke = bundle.getBoolean("fromButtonOke", false);

            if (fromButtonOke) {
                stopForeground(true);
                JobScheduler jobScheduler = getApplicationContext().getSystemService(JobScheduler.class);
                manager.cancelAll();
                jobScheduler.cancelAll();
            }

            if (fromButtonStop) {

                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                }

                stopForeground(true);

                if (StartForeGroundServicesNotification.timer != null && StartForeGroundServicesNotification.timerTask != null) {
                    StartForeGroundServicesNotification.timerTask.cancel();
                    StartForeGroundServicesNotification.timer.cancel();
                    StartForeGroundServicesNotification.timer = null;
                    StartForeGroundServicesNotification.timerTask = null;
                }
                JobScheduler jobScheduler = getApplicationContext().getSystemService(JobScheduler.class);
                manager.cancelAll();
                jobScheduler.cancelAll();

                if (pickerLayout == null && layoutCountDown == null) {
                    stateFromButtonStopNotification = true;
                } else {
                    if (mMainActivity.viewPager2.getCurrentItem() == 3) {
                        mMainActivity.showFloatingButtonAtFragment3(View.VISIBLE);
                    } else {
                        mMainActivity.showFloatingButtonAtFragment3(View.GONE);
                    }
                    mMainActivity.showButtonPauseAndStartCountDown(View.GONE);
                    mMainActivity.showButtonResetCountDown(View.GONE);
                    mMainActivity.showLayoutCountDown(View.GONE);
                    mMainActivity.showLayoutNumberPicker(View.VISIBLE);
                }
            }

            isRunning = false;
            isStarted = false;
            mMainActivity.startCountDown = false;
            if (timerTask != null && timer != null) {
                timerTask.cancel();
                timer.cancel();
                timerTask = null;
                timer = null;
            }
            return START_NOT_STICKY;
        }
    }
}