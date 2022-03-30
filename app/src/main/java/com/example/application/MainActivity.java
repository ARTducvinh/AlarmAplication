package com.example.application;

import static com.example.application.StartForeGroundServicesNotification.mediaPlayer;
import static com.example.application.StartForeGroundServicesNotification.pauseMusic;
import static com.example.application.StartForeGroundServicesNotification.posSongChoice;
import static com.example.application.StartForeGroundServicesNotification.restartMusic;
import static com.example.application.StartForeGroundServicesNotification.songs;
import static com.example.application.StartForeGroundServicesNotification.startMusic;
import static com.example.application.StartForeGroundServicesNotification.stopMusic;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements ChangeStateButtonCallBack{
    //KHAI BÁO CÁC THUỘC TÍNH
    private final String[] titles = {"","","",""};
    public FloatingActionButton addAlarmButton,buttonStartCountTimeUp,
                                buttonPauseAndRestart,buttonTakeTimeAndReset,
                                floatingButtonStartCountDown,
                                floatingActionButtonResetCountDown,floatingActionButtonStartAndPauseCountDown;
    private CircleImageView floatingButtonSetMusics,floatingButtonSetTimeFavorites;
    public ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private ViewPagerFragmentAlarm viewPagerFragmentAlarm;
    private TimeElement timeElement;
    private final List<Fragment> listFragment = new ArrayList<>();
    private AlarmFragment alarmFragment;
    private SetTimeSystemFragment homeFragment;
    private TimeCountUpFragment timeCountUpFragment;
    private CountDownTimerFragment countDownTimerFragment;
    public static List<TimeElement> timeElementList = new ArrayList<>();

    private RelativeLayout topToolBarDeleteAlarm,layoutDeleteAlarm;
    private ImageView buttonCancelDelete,buttonSelectedAllAlarm;
    private RelativeLayout buttonDeleteAlarmLayout;

    boolean isButtonSetMusicsClicked = false;
    boolean isButtonSetTimeClicked = false;
    boolean startCountDown = false;

    int hourGet=0,minutesGet=0,secondsGet=0;
    int hourGetCountDown=0,minutesGetCountDown=0,secondsGetCountDown=0;
    boolean isRunningNotification = false;
    //NƠI NHẬN DỮ LIỆU TRẢ VỀ TỪ CREATEALARMACTIVITY.CLASS
    public final ActivityResultLauncher<Intent> mResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                //SAU KHI NHẬN DỮ LIỆU THÌ GỬI SANG ALARMADAPTER ĐỂ CẬP NHẬT DỮ LIỆU
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        assert intent != null;
                        int state_add_or_fix = intent.getIntExtra("STATE_ALARM",0);
                        int position_alarm_fix = intent.getIntExtra("POSITION",-1);
                        timeElement = (TimeElement) intent.getSerializableExtra("timeElement");
                        updateAlarmFragmentUI(timeElement,state_add_or_fix,position_alarm_fix);
                    }
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DISABLE BATTERY SAVER TRÊN XIAOMI NOTE 7
        //disableBatterySaverInXiaomi();

        //GET BUNDLE FROM EVENT CLICKED ON NOTIFICATION
        Bundle bundle = getIntent().getBundleExtra("bundle");
        boolean fromClickedOnNotification = false;
        if(bundle != null){
            fromClickedOnNotification = bundle.getBoolean("fromClicked",false);
        }

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("timeCountDown", Context.MODE_PRIVATE);
        hourGet = sharedPreferences.getInt("hourTheLast",0);
        minutesGet = sharedPreferences.getInt("minutesTheLast",0);
        secondsGet = sharedPreferences.getInt("secondsTheLast",0);

        if(fromClickedOnNotification){
            hourGetCountDown = bundle.getInt("hour",0);
            minutesGetCountDown = bundle.getInt("minutes",0);
            secondsGetCountDown = bundle.getInt("seconds",0);
            isRunningNotification = bundle.getBoolean("isRunning",false);
        }
        else{
            hourGetCountDown = StartForeGroundServicesNotification.posItemHours;
            minutesGetCountDown = StartForeGroundServicesNotification.posItemMinutes;
            secondsGetCountDown = StartForeGroundServicesNotification.posItemSeconds;
            isRunningNotification = CountDownTimerFragment.isRunning;
        }

        Thread a;
        a = new Thread(new Runnable() {
            @Override
            public void run() {
                initFragment(hourGet,minutesGet,secondsGet,isRunningNotification,hourGetCountDown,minutesGetCountDown,secondsGetCountDown);
            }
        });
        a.start();
        try {
            a.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //  initFragment();

        //ÁNH XẠ VIEWPAGER, TABLAYOUT VÀ SET ADAPTER CHO VIEWPAGER
        topToolBarDeleteAlarm = findViewById(R.id.topToolbarDeleteAlarm);
        layoutDeleteAlarm = findViewById(R.id.layoutDeleteAlarm);
        viewPager2 = findViewById(R.id.viewPagerAlarm);
        tabLayout = findViewById(R.id.tabLayoutAlarm);
        buttonCancelDelete = findViewById(R.id.buttonCloseDeleteAlarm);
        buttonSelectedAllAlarm = findViewById(R.id.buttonSelectAll);
        buttonDeleteAlarmLayout = findViewById(R.id.buttonDeleteAlarmLayout);
        viewPagerFragmentAlarm = new ViewPagerFragmentAlarm(this,listFragment);
        //SET ADAPTER FOR VIEW PAGER
        viewPager2.setAdapter(viewPagerFragmentAlarm);
        if(fromClickedOnNotification){
            viewPager2.setCurrentItem(3);
        }
        //NÚT FLOATING BUTTON ĐỂ CHUYỂN SANG ACTIVITY CREATE ALARM
        buttonStartCountTimeUp = findViewById(R.id.floatingButtonStartCountTimeUp);
        buttonPauseAndRestart = findViewById(R.id.floatingButtonStartAndPause);
        buttonTakeTimeAndReset = findViewById(R.id.floatingButtonTakeTimeAndReset);
        floatingButtonStartCountDown = findViewById(R.id.floatingButtonStartCountDown);
        floatingButtonSetMusics = findViewById(R.id.floatingButtonSetMusic);
        floatingButtonSetTimeFavorites = findViewById(R.id.floatingButtonFavoritesTime);
        floatingActionButtonResetCountDown = findViewById(R.id.floatingButtonResetCountDown);
        floatingActionButtonStartAndPauseCountDown = findViewById(R.id.floatingButtonStartAndPauseCountDown);
        //ỨNG VỚI MỖI TAB THÌ TA SET TITLE CHO NÓ
        new TabLayoutMediator(tabLayout,viewPager2,((tab,pos)->tab.setText(titles[pos]))).attach();

        //SET ICON CHO MỖI TAB TRONG TAB LAYOUT
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_alarm);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_time_local);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_timer_countdown);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_hourglass);

        //BUTTON ADD ALARM
        addAlarmButton = findViewById(R.id.floatingButtonAddAlarm);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position == 2){
                    boolean checkRun = timeCountUpFragment.getIsStartCountUp();
                    hideFloatingButtonAddAlarm(View.GONE);
                    showFloatingButtonAtFragment3(View.GONE);
                    showButtonResetCountDown(View.GONE);
                    showButtonPauseAndStartCountDown(View.GONE);
                    if(checkRun){
                        showButtonPauseAndStart(View.VISIBLE);
                        showButtonTakeAndReset(View.VISIBLE);
                        showFloatingStartCountTimeUp(View.GONE);
                        hideFloatingButtonAddAlarm(View.GONE);
                    }
                    else{
                        if(!timeCountUpFragment.getTimeCountUpToText().equals("00:00")){
                            hideFloatingButtonAddAlarm(View.GONE);
                            showFloatingStartCountTimeUp(View.GONE);
                            showButtonTakeAndReset(View.VISIBLE);
                            showButtonPauseAndStart(View.VISIBLE);
                        }
                        else{
                            showFloatingStartCountTimeUp(View.VISIBLE);
                        }
                    }
                }
                else{
                    if(position == 3){
                        if(countDownTimerFragment.isStarted){
                            showFloatingButtonAtFragment3(View.GONE);
                            showButtonResetCountDown(View.VISIBLE);
                            showButtonPauseAndStartCountDown(View.VISIBLE);
                        }
                        else{
                            showFloatingButtonAtFragment3(View.VISIBLE);
                            showButtonResetCountDown(View.GONE);
                            showButtonPauseAndStartCountDown(View.GONE);
                        }
                        hideFloatingButtonAddAlarm(View.GONE);
                        //show and hide button reset and pause and start count down
                    }
                    else{
                        showButtonResetCountDown(View.GONE);
                        showButtonPauseAndStartCountDown(View.GONE);
                        showFloatingButtonAtFragment3(View.GONE);
                        hideFloatingButtonAddAlarm(View.VISIBLE);
                    }
                    showFloatingStartCountTimeUp(View.GONE);
                    showButtonPauseAndStart(View.GONE);
                    showButtonTakeAndReset(View.GONE);
                }
            }

        });




        addAlarmButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view){
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                Intent i = new Intent(MainActivity.this, CreateAlarmActivity.class);
                //SET STATE LÀ THÊM BÁO THỨC
                i.putExtra("STATE_ALARM",TimeElement.STATE_ADD_ALARM);
                //MODE THÊM BÁO THỨC
                i.putExtra("MODE_ADD_ALARM",1);
                if (pm.isIgnoringBatteryOptimizations(packageName))
                    i.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                else {
                    i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    i.setData(Uri.parse("package:" + packageName));
                }
                alarmFragment.callSetCheckHasShowToastToFalse();
                //CHẠY INTENT SANG ACTIVITY MỚI
                mResultLauncher.launch(i);
            }
        });

        //BUTTON START COUNT TIME UP
        buttonStartCountTimeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeCountUpFragment.startCountTimeUp();
                showFloatingStartCountTimeUp(View.GONE);
                showButtonPauseAndStart(View.VISIBLE);
                showButtonTakeAndReset(View.VISIBLE);
            }
        });


        //BUTTON PAUSE AND RESTART COUNT TIME UP
        buttonPauseAndRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeCountUpFragment.getIsStartCountUp()){
                    //STOP
                    buttonPauseAndRestart.setImageResource(R.drawable.ic_start_count_up);
                    buttonTakeTimeAndReset.setImageResource(R.drawable.custom_button_start_count_up_time);
                    timeCountUpFragment.pauseCountTimeUp();
                }
                else{
                    //RESTART
                    buttonPauseAndRestart.setImageResource(R.drawable.ic_pause);
                    buttonTakeTimeAndReset.setImageResource(R.drawable.ic_baseline_pan_tool_alt_24);
                    timeCountUpFragment.startCountTimeUp();
                }
            }
        });
        //BUTTON TAKE AND RESET TIME COUNT UP
        buttonTakeTimeAndReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeCountUpFragment.getIsStartCountUp()){
                    timeCountUpFragment.scaleLayoutViewToTopLeft(true);
                    timeCountUpFragment.takeMilestones();
                }
                else{
                    timeCountUpFragment.scaleLayoutViewToTopLeft(false);
                    timeCountUpFragment.clearAllItems();
                    timeCountUpFragment.resetCountTimeUp();
                    showFloatingStartCountTimeUp(View.VISIBLE);
                    buttonTakeTimeAndReset.setImageResource(R.drawable.ic_baseline_pan_tool_alt_24);
                    buttonPauseAndRestart.setImageResource(R.drawable.ic_pause);
                    showButtonPauseAndStart(View.GONE);
                    showButtonTakeAndReset(View.GONE);
                }
            }
        });

        //BUTTON SET MUSICS FOR COUNT DOWN TIMER
        floatingButtonSetMusics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isButtonSetMusicsClicked){
                    changeStateButtonSetMusicsOpen();
                    changeStateButtonSetTimeFavoritesClose();
                }else{
                    changeStateButtonSetMusicsClose();
                }
                countDownTimerFragment.changeStateVisibleAllViews();
            }
        });
        //BUTTON SET TIME FAVORITES FOR COUNT DOWN TIMER
        floatingButtonSetTimeFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isButtonSetTimeClicked){
                    changeStateButtonSetTimeFavoritesOpen();
                    changeStateButtonSetMusicsClose();
                }else{
                    changeStateButtonSetTimeFavoritesClose();
                    countDownTimerFragment.changeStateVisibleAllViews();
                }
            }
        });

        //BUTTON START COUNT DOWN
        floatingButtonStartCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!startCountDown){
                    //START PLAY SOUND
                    CountDownTimerFragment.isRunning = true;
                    if(mediaPlayer != null){
                        Log.i("AAA","START COUNT DOWN NOT NULL :");
                        if (!mediaPlayer.isPlaying())
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), songs[posSongChoice]);
                            mediaPlayer.setLooping(true);
                            if (posSongChoice != 0) {
                                Log.i("AAA", "START NOT NULL SUBBBBBBBBBB 1");
                                startMusic();
                            }
                    }
                    else{
                        Log.i("AAA","START COUNT DOWN NULL");
                        mediaPlayer = MediaPlayer.create(getApplicationContext(),songs[posSongChoice]);
                        mediaPlayer.setLooping(true);
                        if(posSongChoice != 0){
                            Log.i("AAA","START NOT NULL CLMMM");
                            startMusic();
                        }
                    }
                    int a = getHoursReturn(), b = getMinutesReturn(), c = getSecondsReturn();
                    countDownTimerFragment.showRecyclerViewMusics(View.GONE);
                    countDownTimerFragment.showRecyclerViewTimeFavorites(View.GONE);
                    changeStateButtonSetMusicsClose();
                    changeStateButtonSetTimeFavoritesClose();

                    countDownTimerFragment.showLayoutCountDown(View.VISIBLE);
                    countDownTimerFragment.showColonFirst(View.VISIBLE);
                    countDownTimerFragment.showColonSecond(View.VISIBLE);
                    countDownTimerFragment.showLayoutNumberPicker(View.GONE);
                    showFloatingButtonAtFragment3(View.INVISIBLE);
                    showButtonResetCountDown(View.VISIBLE);
                    showButtonPauseAndStartCountDown(View.VISIBLE);
                    setTextViewSumTime(a,b,c);
                    saveTheLastTimeCountDown(getHoursReturn(),getMinutesReturn(),getSecondsReturn());
                    countDownTimerFragment.startCountDownFunction(a,b,c);
                    floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
                        @Override
                        public void run() {
                            floatingActionButtonStartAndPauseCountDown.setImageResource(R.drawable.ic_pause);
                        }
                    });
                    startCountDown = true;
                }
                else {
                    CountDownTimerFragment.isRunning = false;
                    countDownTimerFragment.showLayoutNumberPicker(View.VISIBLE);
                    countDownTimerFragment.showTextViewSecondsCountDown(View.GONE);
                    countDownTimerFragment.showTextViewMinutesCountDown(View.GONE);
                    countDownTimerFragment.showTextViewHoursCountDown(View.GONE);
                    countDownTimerFragment.showColonFirst(View.GONE);
                    countDownTimerFragment.showColonSecond(View.GONE);
                    showFloatingButtonAtFragment3(View.VISIBLE);
                    showButtonResetCountDown(View.GONE);
                    showButtonPauseAndStartCountDown(View.GONE);
                    startCountDown = false;
                }
            }
        });


        floatingActionButtonStartAndPauseCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startCountDown){
                    pauseMusic();
                    CountDownTimerFragment.isRunning = false;
                    startCountDown = false;
                    //countDownTimerFragment.stopCountDown();
                    floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
                        @Override
                        public void run() {
                            floatingActionButtonStartAndPauseCountDown.setImageResource(R.drawable.ic_start_count_up);
                        }
                    });
                }
                else {
                    Log.i("AAA", String.valueOf("MUSIC NULL : "+mediaPlayer == null ? true:false));
                    startCountDown = true;
                    CountDownTimerFragment.isRunning = true;
                    //countDownTimerFragment.startCountDownFunction(countDownTimerFragment.getHoursReturnTextView(), countDownTimerFragment.getMinutesReturnTextView(), countDownTimerFragment.getSecondsReturnTextView());
                    floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
                        @Override
                        public void run() {
                            floatingActionButtonStartAndPauseCountDown.setImageResource(R.drawable.ic_pause);
                        }
                    });
                    if(posSongChoice != 0){
                        restartMusic();
                    }
                }
            }
        });

        floatingActionButtonResetCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMusic();
                showButtonPauseAndStartCountDown(View.GONE);
                showFloatingButtonAtFragment3(View.VISIBLE);
                countDownTimerFragment.showLayoutCountDown(View.GONE);
                countDownTimerFragment.showLayoutNumberPicker(View.VISIBLE);
                floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
                    @Override
                    public void run() {
                        floatingActionButtonStartAndPauseCountDown.setImageResource(R.drawable.ic_pause);
                    }
                });
                startCountDown = false;
                showButtonResetCountDown(View.GONE);
                countDownTimerFragment.stopCountDown();
            }
        });

        //NÚT "X" HỦY XÓA BÁO THỨC
        buttonCancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTopAppBarLayout(false);
                showFloatingButton();
                hideLayoutDeleteAlarm();
                alarmFragment.callOnBackPressedFunctionInAlarmAdapter();
            }
        });
        buttonSelectedAllAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmFragment.callFunctionSelectAllAlarm();
            }
        });

        buttonDeleteAlarmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    alarmFragment.callDeleteItemSelectedAndUpdateUI();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showTopAppBarLayout(false);
                showFloatingButton();
                hideLayoutDeleteAlarm();
            }
        });

    }

    //UPDATE UI MỖI KHI THÊM MỚI 1 BÁO THỨC THÀNH CÔNG
    public void updateAlarmFragmentUI(TimeElement insertElement,int INT_ADD_OR_FIX_ALARM, int POSITION_ALARM_FIX){
        alarmFragment.getItemUpdate(insertElement,INT_ADD_OR_FIX_ALARM,POSITION_ALARM_FIX);
    }

    //KHỞI TẠO CÁC FRAGMENT
    public void initFragment(int a, int b, int c,boolean d,int e,int f, int g){
        alarmFragment = new AlarmFragment(timeElementList,this);
        homeFragment = new SetTimeSystemFragment();
        timeCountUpFragment = new TimeCountUpFragment(this,this);
        countDownTimerFragment = new CountDownTimerFragment(this,getApplicationContext(),a,b,c,d,e,f,g);
        countDownTimerFragment.initTheLastItemChoice();
        listFragment.add(alarmFragment);
        listFragment.add(homeFragment);
        listFragment.add(timeCountUpFragment);
        listFragment.add(countDownTimerFragment);
    }


    //HIDE OR SHOW LAYOUT CHỨA NÚT HỦY XÓA VÀ CHỌN TẤT CẢ
    public void showTopAppBarLayout(boolean then){
        if(then){
            tabLayout.setVisibility(View.INVISIBLE);
            topToolBarDeleteAlarm.setVisibility(View.VISIBLE);
            buttonCancelDelete.setEnabled(true);
            buttonCancelDelete.setVisibility(View.VISIBLE);
            buttonSelectedAllAlarm.setEnabled(true);
            buttonSelectedAllAlarm.setVisibility(View.VISIBLE);
            viewPager2.setUserInputEnabled(false);
        }
        else{
            tabLayout.setVisibility(View.VISIBLE);
            topToolBarDeleteAlarm.setVisibility(View.INVISIBLE);
            buttonCancelDelete.setEnabled(false);
            buttonCancelDelete.setVisibility(View.GONE);
            buttonSelectedAllAlarm.setEnabled(false);
            buttonSelectedAllAlarm.setVisibility(View.GONE);
            viewPager2.setUserInputEnabled(true);
        }
    }

    //NẾU ONBACKPRESSED THÌ ĐƯA CÁC ITEM VỀ TRẠNG THÁI BÌNH THƯỜNG
    //VÀ TẠO BIẾN BOOLEAN ĐỂ CHECK STATE NẾU ONBACKPRESSED TRUE THÌ SET CHECKHASSHOWTOAST = TRUE
    //
    //BẮT SỰ KIỆN NHẤN NÚT BACK VỀ
    //NẾU ĐANG Ở TRẠNG THÁI XÓA BÁO THỨC THÌ HỦY XÓA BỞI GỌI HÀM callOnBackPressedFunctionInAlarmAdapter();
    @Override
    public void onBackPressed() {
        if(topToolBarDeleteAlarm.getVisibility() == View.VISIBLE){
            showTopAppBarLayout(false);
            showFloatingButton();
            hideLayoutDeleteAlarm();
            alarmFragment.callOnBackPressedFunctionInAlarmAdapter();
            //alarmFragment.callSetCheckHasShowToastToFalse();
        }
        else{
            super.onBackPressed();
        }
    }

    // HIDE HOẶC SHOW NÚT THÊM BÁO THỨC KHI NHẤN VÀO MỖI ITEM SHOW DIALOG CÀI ĐẶT THÊM
    public void hideFloatingButtonAddAlarm(int viewState){
        addAlarmButton.setVisibility(viewState);
        addAlarmButton.setEnabled(viewState == View.VISIBLE);
    }

    //HIDE NÚT THÊM BÁO THỨC VỚI ANIMATION
    public void hideFloatingButton(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.floating_button_hide);
        addAlarmButton.startAnimation(animation);
        addAlarmButton.setEnabled(false);
        addAlarmButton.setVisibility(View.GONE);
    }
    //SHOW NÚT THÊM BÁO THỨC VỚI ANIMATION
    public void showFloatingButton(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.floating_button_show);
        addAlarmButton.startAnimation(animation);
        addAlarmButton.setEnabled(true);
        addAlarmButton.setVisibility(View.VISIBLE);
    }


    //SHOW OR HIDE BUTTON START COUNT TIME UP
    public void showFloatingStartCountTimeUp(int visibility){
        buttonStartCountTimeUp.setVisibility(visibility);
        buttonStartCountTimeUp.setEnabled(visibility == View.VISIBLE);
    }

    //SHOW OR HIDE BUTTON START COUNT DOWN, BUTTON SET MUSICS, BUTTON SET TIME FAVOURITES
    public void showFloatingButtonAtFragment3(int visibility){
        floatingButtonStartCountDown.post(new Runnable() {
            @Override
            public void run() {
                floatingButtonStartCountDown.setVisibility(visibility);
                floatingButtonStartCountDown.setEnabled(visibility == View.VISIBLE);
            }
        });
        floatingButtonSetMusics.post(new Runnable() {
            @Override
            public void run() {
                floatingButtonSetMusics.setVisibility(visibility);
                floatingButtonSetMusics.setEnabled(visibility == View.VISIBLE);
            }
        });
        floatingButtonSetTimeFavorites.post(new Runnable() {
            @Override
            public void run() {
                floatingButtonSetTimeFavorites.setVisibility(visibility);
                floatingButtonSetTimeFavorites.setEnabled(visibility == View.VISIBLE);
            }
        });

    }

    public void setTextViewSumTime(int hour,int minute,int seconds){
        countDownTimerFragment.textViewSumTime.setText(("Tổng "+ (hour == 0 ? "": hour + " giờ ")+
                (minute == 0 ? "": minute + " phút ")+
                (seconds == 0 ? "": seconds + " giây")).replaceAll("\\s{2,}"," ").trim());
    }

    //SHOW NÚT XÓA BÁO THỨC
    public void showLayoutDeleteAlarm(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.custom_anim_layout_delete_in);
        layoutDeleteAlarm.startAnimation(animation);
        buttonDeleteAlarmLayout.setEnabled(true);
        buttonDeleteAlarmLayout.setVisibility(View.VISIBLE);
    }

    //HIDE NÚT XÓA BÁO THỨC
    public void hideLayoutDeleteAlarm(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.custom_anim_layout_delete_out);
        layoutDeleteAlarm.startAnimation(animation);
        buttonDeleteAlarmLayout.setEnabled(false);
        buttonDeleteAlarmLayout.setVisibility(View.INVISIBLE);
    }

    //ENABLE CÁC NÚT CANCEL VÀ SELECT ALL KHI TRỞ VỀ MÀN HÌNH CHÍNH
    public void enableButtonCancelAndSelectAll(){
        buttonCancelDelete.setEnabled(true);
        buttonSelectedAllAlarm.setEnabled(true);
        buttonSelectedAllAlarm.setVisibility(View.VISIBLE);
    }

    //SHOW OR HIDE BUTTON PAUSE AND START
    public void showButtonPauseAndStart(int visibility){
        buttonPauseAndRestart.setVisibility(visibility);
        buttonPauseAndRestart.setEnabled(visibility == View.VISIBLE);
    }

    //SHOW OR HIDE BUTTON TAKE TIME AND RESET
    public void showButtonTakeAndReset(int visibility){
        buttonTakeTimeAndReset.setVisibility(visibility);
        buttonTakeTimeAndReset.setEnabled(visibility == View.VISIBLE);
    }

    //SHOW OR HIDE BUTTON RESET COUNT DOWN
    public void showButtonResetCountDown(int visibility){
        floatingActionButtonResetCountDown.post(new Runnable() {
            @Override
            public void run() {
                floatingActionButtonResetCountDown.setVisibility(visibility);
                floatingActionButtonResetCountDown.setEnabled(visibility == View.VISIBLE);
                floatingActionButtonResetCountDown.setClickable(visibility == View.VISIBLE);
            }
        });
    }

    //SHOW OR HIDE BUTTON PAUSE AND START COUNT DOWN
    public void showButtonPauseAndStartCountDown(int visibility){
        floatingActionButtonStartAndPauseCountDown.post(new Runnable() {
            @Override
            public void run() {
                floatingActionButtonStartAndPauseCountDown.setVisibility(visibility);
                floatingActionButtonStartAndPauseCountDown.setEnabled(visibility == View.VISIBLE);
                floatingActionButtonStartAndPauseCountDown.setClickable(visibility == View.VISIBLE);
            }
        });
    }

    public void showLayoutCountDown(int visibility){
        countDownTimerFragment.showLayoutCountDown(visibility);
    }

    public void showLayoutNumberPicker(int visibility){
        countDownTimerFragment.showLayoutNumberPicker(visibility);
    }

    public void changeStateButtonSetMusicsOpen(){
        floatingButtonSetMusics.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.custom_ic_musics_white_clicked));
        countDownTimerFragment.showRecyclerViewMusicItemList(View.VISIBLE);
        isButtonSetMusicsClicked = true;
    }

    public void changeStateButtonSetMusicsClose(){
        floatingButtonSetMusics.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.custom_ic_musics_normal));
        countDownTimerFragment.showRecyclerViewMusicItemList(View.GONE);
        isButtonSetMusicsClicked = false;
    }

    public void changeStateButtonSetTimeFavoritesOpen(){
        floatingButtonSetTimeFavorites.post(new Runnable() {
            @Override
            public void run() {
                floatingButtonSetTimeFavorites.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.custom_ic_list_notes_white_clicked));
            }
        });
        countDownTimerFragment.showRecyclerViewTimeFavoritesList(View.VISIBLE);
        isButtonSetTimeClicked = true;
    }

    public void changeStateButtonSetTimeFavoritesClose(){
        floatingButtonSetTimeFavorites.post(new Runnable() {
            @Override
            public void run() {
                floatingButtonSetTimeFavorites.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.custom_ic_list_notes_normal));
            }
        });
        countDownTimerFragment.showRecyclerViewTimeFavoritesList(View.GONE);
        isButtonSetTimeClicked = false;
    }


    public void changeStateButton(boolean state) {
        if(state){
            //STOP
            buttonPauseAndRestart.setImageResource(R.drawable.ic_start_count_up);
            buttonTakeTimeAndReset.setImageResource(R.drawable.custom_button_start_count_up_time);
        }
        else{
            //RESTART
            buttonPauseAndRestart.setImageResource(R.drawable.ic_pause);
            buttonTakeTimeAndReset.setImageResource(R.drawable.ic_baseline_pan_tool_alt_24);
        }
    }

    public void changeStateEnableButtonStartCountDown(int state){
        if(state == 1){
            floatingButtonStartCountDown.post(new Runnable() {
                @Override
                public void run() {
                    //floatingButtonStartCountDown.setEnabled(false);
                    floatingButtonStartCountDown.setClickable(false);
                    floatingButtonStartCountDown.setImageResource(R.drawable.ic_start_count_up_blur);
                }
            });
        }
        else{
            floatingButtonStartCountDown.post(new Runnable() {
                @Override
                public void run() {
                    //floatingButtonStartCountDown.setEnabled(true);
                    floatingButtonStartCountDown.setClickable(true);
                    floatingButtonStartCountDown.setImageResource(R.drawable.ic_start_count_up);
                }
            });
        }
    }

    //FUNCTION DISABLE BATTERY SAVER IN XIAOMI
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void disableBatterySaverInXiaomi(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"Permission granted !",Toast.LENGTH_LONG).show();
        }
        else{
            requestPermission();
        }
    }

    public int getHoursReturn(){
        return countDownTimerFragment.getHoursReturn();
    }

    public int getMinutesReturn(){
        return countDownTimerFragment.getMinutesReturn();
    }

    public int getSecondsReturn(){
        return countDownTimerFragment.getSecondsReturn();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Cấp quyền đi nào chàng trai !")
                    .setNegativeButton("Đéo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("Oke, baby", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS},1900);
                        }
                    }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},1900);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1900){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission is just granted !",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this,"Permission is just denied !",Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void saveTheLastTimeCountDown(int a, int b, int c){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("timeCountDown",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hourTheLast",a);
        editor.putInt("minutesTheLast",b);
        editor.putInt("secondsTheLast",c);
        editor.commit();
    }
}



//REQUIREMENT
// START COUNT DOWN AND DESTROY APP AND RESTORE STATE AND DATA WHEN OPEN AGAIN
// CLICK BUTTON "DỪNG" ON NOTIFICATION STOP COUNT COUNT
// UPDATE VIEW FOR ITEM ALARM WHEN 1 MINuTES GONE...

//LỖI KHI DỪNG BÁO THỨC TẮT ACTIVITY VÀ CLICK THÔNG BÁO THÌ NÓ VẪN CHẠY
