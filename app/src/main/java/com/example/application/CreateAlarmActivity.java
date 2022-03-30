package com.example.application;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowId;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TimePicker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private ImageView buttonSetTime,buttonCLose;
    private TextView textViewTimeRemain,textNote,textRegular,textViewOnce,
            textViewAllDate,textViewFrom2To6,textViewOptions,textViewLabelAddAlarm;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent intent,intentResult;
    private SwitchMaterial switchMaterialVibrate,switchMaterialOffAfterRing;
    private PowerManager pm;
    private TimeElement timeElement;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private Date timeFirst,timeSecond;
    private long timeRemain;
    private String textHour,textMinute;
    private Calendar currentCalendar;
    private final Calendar calendarCurrent = Calendar.getInstance();
    private TimeElement tempElement;
    private CustomPendingIntent customPendingIntent;
    private Parcel parcel;
    private byte[] bytesArrayPendingIntent;
    private String tempStr = "Một lần";
    private boolean checkFirstTime = true;

    //INIT INSTANCE TO SAVE DATA TO DATABASE
    private SaveDataToSQLite saveDataToSQLite;

    // ID CHANNEL CỦA THÔNG BÁO VÀ REGULAR
    public static final int WAKE_REASON_APPLICATION = 2;
    public static int idNotification = 1111;
    //public static int idNotificationShowBefore1Hours =
    public static final String idChannel="notificationChannelID";
    public static final String nameChannel="notificationChannelName";
    public static final String notificationData="notification";

    public final static int REGULAR_ONCE = 0;
    public final static int REGULAR_ALL_DAY = 1;
    public final static int REGULAR_MONDAY_TO_FRIDAY = 2;
    public final static int REGULAR_OPTIONS = 3;

    // CÁC THUỘC TÍNH CỦA PHẦN GIAO DIỆN TẠO BÁO THỨC
    private ImageView regularOnceCheck,regularAllDateCheck
            ,regularFrom2To6Check,regularOptionsCheck;
    private int allIsChecked=0;

    //STRING LƯU CÁC LỰA CHỌN TỪ THỨ 2 ĐẾN CHỦ NHẬT
    private String textThu2="",textThu3="",textThu4="",textThu5="",textThu6="",textThu7="",textThu8="";

    //BOTTOM SHEET DIALOG CỦA LỰA CHỌN LẶP LẠI (MỘT LẦN, HẰNG NGÀY, THỨ 2 ĐẾN THỨ 6, TÙY CHỈNH)
    private RelativeLayout regularRepeat,onceRepeat,allDateRepeat,from2To6Repeat,optionsRepeat;
    private BottomSheetDialog bottomSheetDialogRegularRepeat,bottomSheetDialogOptionsMenu;

    //BOTTOM SHEET DIALOG CỦA TÙY CHỌN CỦA NGƯỜI DÙNG TỪ THỨ 2 ĐẾN CHỦ NHẬT
    private RelativeLayout layoutThu2,layoutThu3,layoutThu4,layoutThu5,layoutThu6,layoutThu7,layoutThu8;
    private CheckBox checkBoxThu2,checkBoxThu3,checkBoxThu4,checkBoxThu5,checkBoxThu6,checkBoxThu7,checkBoxThu8;
    private TextView buttonCancelFrom2To8,buttonOkFrom2To8;

    //CÁC THUỘC TÍNH TRONG BOTTOM SHEET DIALOG CỦA PHẦN THÊM GHI CHÚ CHO BÁO THỨC
    private RelativeLayout layoutNote,layoutAddNoteCustom;
    private BottomSheetDialog bottomSheetDialogAddNote;
    private EditText editTextAddNote;
    private TextView buttonCancelNote,buttonAddNote;
    private Cursor data;
    private int idAlarm = 0;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"ResourceAsColor", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);
        //GET DỮ LIỆU TỪ MAINACTIVITY.CLASS CHUYỂN QUA
        int mode = getIntent().getIntExtra("MODE_ADD_ALARM",1);
        int state_add_or_fix = getIntent().getIntExtra("STATE_ALARM",0);
        //RETRIEVE ID NOTIFICATION FROM SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("metadata", MODE_PRIVATE);
        int idTemp = sharedPreferences.getInt("idNotification",1111);

        if(idTemp != 1111){
            idNotification = idTemp;
        }

        // ÁNH XẠ CÁC VIEW VỚI CÁC ID
        // PHẦN TOOLBAR
        buttonSetTime = findViewById(R.id.buttonSetTime);// NÚT SET THỜI GIAN BÁO THỨC
        buttonCLose = findViewById(R.id.closeButton); // NÚT ĐÓNG ACTICITY TẠO BÁO THỨC
        textViewTimeRemain = findViewById(R.id.textViewDisplayTimeRemain);
        textViewLabelAddAlarm = findViewById(R.id.textViewLabelAddAlarm);
        textViewLabelAddAlarm.setText("Thêm báo thức");
        //INSTANCE DATABASE OBJECT
        saveDataToSQLite = new SaveDataToSQLite(this);
        //PHẦN CÁC LỰA CHỌN NHƯ NHẠC CHUÔNG, LẶP LẠI, RUNG KHI BÁO THỨC....
        //PHẦN ĐỒNG HỒ
        timePicker = findViewById(R.id.timePicker); // ĐỒNG HỒ BÁO THỨC
        timePicker.setIs24HourView(true);
        calendar = Calendar.getInstance(); // KHỞI TẠO CALENDAR
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        timeFirst = new Date();
        timeSecond = new Date();

        //PHẦN NHẠC CHUÔNG
        switchMaterialVibrate = findViewById(R.id.switchVibrate); // NÚT SWITCH RUNG KHI BÁO THỨC
        switchMaterialOffAfterRing = findViewById(R.id.switchOffAfterRing);// NÚT SWITCH XÓA BÁO THỨC SAU KHI ĐÃ BÁO THỨC
        switchMaterialVibrate.setChecked(true);
        switchMaterialVibrate.setBackground(switchMaterialVibrate.getResources().getDrawable(R.drawable.custom_background_switch_on));
        //PHẦN LẶP LẠI
        regularRepeat = findViewById(R.id.regularRepeat);// RELATIVELAYOUT CỦA LỰA CHỌN LẶP LẠI
        textRegular = findViewById(R.id.textRegular);// PHẦN TEXT HIỂN THỊ CHU KÌ LẶP LẠI BÁO THỨC
        //PHẦN GHI CHÚ
        layoutNote = findViewById(R.id.layoutAddNote);
        textNote = findViewById(R.id.textNote); // PHẦN TEXT HIỂN THỊ GHI CHÚ


        //BOTTOM SHEET DIALOG CỦA "LỰA CHỌN" LẶP LẠI
        bottomSheetDialogRegularRepeat = new BottomSheetDialog(CreateAlarmActivity.this,R.style.BottomSheetThemeCustom);
        //ÁNH XẠ VIEW LAYOUT CỦA PHẦN LỰA CHỌN LẶP LẠI (MỘT LẦN, HẰNG NGÀY, THỨ 2 ĐẾN THỨ 6,TÙY CHỌN)
        View viewBottomSheetDialog = getLayoutInflater().inflate(R.layout.custom_regular_repeat_options,(LinearLayout)findViewById(R.id.regularRepeatLayout));
        onceRepeat = viewBottomSheetDialog.findViewById(R.id.onceRepeat);
        allDateRepeat = viewBottomSheetDialog.findViewById(R.id.repeatAllDate);
        from2To6Repeat = viewBottomSheetDialog.findViewById(R.id.from2To6);
        optionsRepeat = viewBottomSheetDialog.findViewById(R.id.optionsDate);
        bottomSheetDialogRegularRepeat.setContentView(viewBottomSheetDialog);
        bottomSheetDialogRegularRepeat.setCanceledOnTouchOutside(true);
        //ÁNH XẠ CHECKBOX CỦA PHẦN LỰA CHỌN LẶP LẠI (MỘT LẦN, HẰNG NGÀY,.....)
        regularOnceCheck = viewBottomSheetDialog.findViewById(R.id.regularOnceCheck);
        regularAllDateCheck = viewBottomSheetDialog.findViewById(R.id.regularAllDateCheck);
        regularFrom2To6Check = viewBottomSheetDialog.findViewById(R.id.regularFrom2To6Check);
        regularOptionsCheck = viewBottomSheetDialog.findViewById(R.id.regularOptionsCheck);
        //ÁNH XẠ TEXTVIEW CỦA LAYOUT "LẶP LẠI"
        textViewOnce = viewBottomSheetDialog.findViewById(R.id.textViewOnce);textViewAllDate = viewBottomSheetDialog.findViewById(R.id.textViewAllDate);
        textViewFrom2To6 = viewBottomSheetDialog.findViewById(R.id.textViewFrom2To6); textViewOptions = viewBottomSheetDialog.findViewById(R.id.textViewOptions);

        //BOTTOM SHEET DIALOG CỦA LỰA CHỌN "TÙY CHỌN" TRONG PHẦN LẶP LẠI
        bottomSheetDialogOptionsMenu = new BottomSheetDialog(CreateAlarmActivity.this,R.style.BottomSheetThemeCustom);
        //ÁNH XẠ LAYOUT
        View viewBottomSheetOptionsMenu = getLayoutInflater().inflate(R.layout.custom_layout_option_bottom_dialog,(LinearLayout)findViewById(R.id.optionsMenuRegular));
        layoutThu2 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu2);layoutThu3 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu3);
        layoutThu4 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu4);layoutThu5 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu5);
        layoutThu6 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu6);layoutThu7 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu7);
        layoutThu8 = viewBottomSheetOptionsMenu.findViewById(R.id.layoutThu8);
        //ÁNH XẠ CHECKBOX
        checkBoxThu2 = viewBottomSheetOptionsMenu.findViewById(R.id.checkBoxThu2);checkBoxThu3 = viewBottomSheetOptionsMenu.findViewById(R.id.checkBoxThu3);
        checkBoxThu4 = viewBottomSheetOptionsMenu.findViewById(R.id.checkBoxThu4);checkBoxThu5 = viewBottomSheetOptionsMenu.findViewById(R.id.checkboxThu5);
        checkBoxThu6 = viewBottomSheetOptionsMenu.findViewById(R.id.checkboxThu6);checkBoxThu7 = viewBottomSheetOptionsMenu.findViewById(R.id.checkboxThu7);
        checkBoxThu8 = viewBottomSheetOptionsMenu.findViewById(R.id.checkBoxThu8);
        buttonCancelFrom2To8 = viewBottomSheetOptionsMenu.findViewById(R.id.buttonCancelOptionsFrom2To8);buttonOkFrom2To8 = viewBottomSheetOptionsMenu.findViewById(R.id.buttonOkFrom2To8);
        bottomSheetDialogOptionsMenu.setCanceledOnTouchOutside(true);
        bottomSheetDialogOptionsMenu.setContentView(viewBottomSheetOptionsMenu);

        //BOTTOM SHEET DIALOG CỦA PHẦN GHI CHÚ
        bottomSheetDialogAddNote = new BottomSheetDialog(CreateAlarmActivity.this,R.style.BottomSheetThemeCustomNotes);
        layoutAddNoteCustom = findViewById(R.id.layoutInputNote);
        //ÁNH XẠ VIEW CỦA BOTTOMSHEETDIALOG THÊM GHI CHÚ
        View viewAddNote = getLayoutInflater().inflate(R.layout.custom_layout_write_note,(RelativeLayout)findViewById(R.id.layoutInputNote));
        editTextAddNote = viewAddNote.findViewById(R.id.editTextNote);
        buttonCancelNote = viewAddNote.findViewById(R.id.buttonCancelNote);
        buttonAddNote = viewAddNote.findViewById(R.id.buttonAddNote);
        bottomSheetDialogAddNote.setCanceledOnTouchOutside(true);
        bottomSheetDialogAddNote.setContentView(viewAddNote);


        //NẾU LÀ MODE = 2 THÌ ĐANG Ở CHẾ ĐỘ SỬA BÁO THỨC CÒN 1 THÌ TẠO BÁO THỨC MỚI
        if(mode==2){
            String tempHourMinute = getIntent().getStringExtra("TEMP_HOUR_MINUTE");
            String state = getIntent().getStringExtra("STATE");
            TimeElement item = (TimeElement) getIntent().getSerializableExtra("TIME_ELEMENT");
            String stateVibrateIntent = getIntent().getStringExtra("STATE_ALARM_VIBRATE");
            textViewLabelAddAlarm.setText("Sửa báo thức");
            idAlarm = item.getIdAlarm();
            if (state.equals("off")) {textViewTimeRemain.setText("Tắt");} else{textViewTimeRemain.setText(item.getTimeCountdown());}
            timePicker.setHour(Integer.parseInt(tempHourMinute.split(":")[0]));
            timePicker.setMinute(Integer.parseInt(tempHourMinute.split(":")[1]));
            textRegular.setText(item.getRegular());
            textNote.setText(item.getNote());
            editTextAddNote.setText(item.getNote());
            switchMaterialVibrate.setChecked(item.getVibrate());
            if(item.getVibrate()){
                switchMaterialVibrate.setBackground(switchMaterialVibrate.getResources().getDrawable(R.drawable.custom_background_switch_on));
            }
            else{
                switchMaterialVibrate.setBackground(switchMaterialVibrate.getResources().getDrawable(R.drawable.custom_background_switch_off));
            }
            if(item.getVibrate()){

            }
            String textRe = item.getRegular();
            switch (textRe){
                case "Một lần":
                    changeBackgroundItemRegular(0);
                    break;
                case "Hằng ngày":
                    changeBackgroundItemRegular(1);
                    break;
                case "Thứ hai đến Thứ sáu":
                    changeBackgroundItemRegular(2);
                    break;
                default:
                    changeBackgroundItemRegular(3);
                    break;
            }

        }

        // CLICK CỦA NÚT HỦY VÀ NÚT THÊM GHI CHÚ
        // NÚT HỦY GHI CHÚ
        buttonCancelNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogAddNote.dismiss();
            }
        });
        // NÚT THÊM GHI CHÚ CHO BÁO THỨC
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editTextAddNote.getText().toString().trim();
                if(!text.isEmpty()){
                    textNote.setText(text);
                }
                bottomSheetDialogAddNote.dismiss();
            }
        });
        // NÚT CHỌN ĐỂ MỞ BOTTOMSHEETDIALOG ĐỂ GHI CHÚ
        layoutNote.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                editTextAddNote.requestFocus();
                bottomSheetDialogAddNote.show();
            }
        });

        //BUTTON TẠO BÁO THỨC
        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UnspecifiedImmutableFlag")
            @Override
            public void onClick(View view) {
                //LẤY THỜI GIAN BAN ĐẦU
                timeFirst = calendar.getTime();

                TimeElement item = (TimeElement) getIntent().getSerializableExtra("TIME_ELEMENT");

                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                //SET THỜI GIAN BÁO THỨC
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                calendar.set(Calendar.MINUTE,minute);

                String hourStr = null,minuteStr = null,timeRemain=null;
                hourStr = String.valueOf(hour);minuteStr = String.valueOf(minute);
                if(hour < 10){ hourStr = "0"+ hour; }
                if(minute < 10){ minuteStr = "0"+ minute; }

                //LẤY KHOẢNG THỜI GIAN BÁO THỨC
                timeSecond = calendar.getTime();

                //TÍNH KHOẢNG THỜI GIAN BÁO THỨC CÒN LẠI
                timeRemain =  findTimeRemain(timeFirst,timeSecond);
                String time = hourStr+":"+minuteStr;

                //LẤY TEXT CỦA REGULAR, NOTE, VÀ XEM THỬ CÓ RUNG KHI BÁO THỨC HAY KHÔNG
                String regularStr = textRegular.getText().toString();
                String note = textNote.getText().toString();
                boolean vibrate = switchMaterialVibrate.isChecked();

                //TẠO INTENT ĐỂ ĐƯA DỮ LIỆU VÀ CHO MAIN_ACTIVITY
                intentResult = new Intent(CreateAlarmActivity.this,MainActivity.class);
                timeElement = new TimeElement(hourStr,minuteStr,note,regularStr,String.valueOf(timeRemain),true,vibrate);
                timeElement.setIdAlarm(idNotification);

                if (state_add_or_fix == TimeElement.STATE_FIX_ALARM){
                    int position_item_fix = getIntent().getIntExtra("POSITION",-1);
                    intentResult.putExtra("POSITION",position_item_fix);
                    timeElement.setIdAlarm(item.getIdAlarm());
                }

                intentResult.putExtra("timeElement",timeElement);
                intentResult.putExtra("STATE_ALARM",state_add_or_fix);

                //ĐÓNG GÓI DỮ LIỆU VÀO BUNDLE ĐỂ GỬI ĐẾN ALARM RECEIVER BROADCAST
                intent = new Intent(getApplicationContext(), AlarmFragment.CustomBroadcast.class);
                Bundle bundleSendToBroadcast = new Bundle();
                bundleSendToBroadcast.putString("state","on");
                bundleSendToBroadcast.putSerializable("timeEelement",timeElement);

                intent.setAction("runBackground");
                intent.putExtra("bundle",bundleSendToBroadcast);


                //TẠO PENDING INTENT VÀ TẠO BÁO THỨC
                pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),idNotification,intent,PendingIntent.FLAG_CANCEL_CURRENT);

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis()-10000,pendingIntent);

                //PROBLEM : SET TIME FOR SHOW NOTIFICATION BEFORE 1 HOUR


                //SAVE PENDING INTENT TO DATABASE TO RETRIEVE CANCEL.
                customPendingIntent = CustomPendingIntent.getBroadcast(idNotification,intent,PendingIntent.FLAG_CANCEL_CURRENT);
                parcel = Parcel.obtain();
                parcel.writeValue(customPendingIntent);
                bytesArrayPendingIntent = parcel.marshall();
                parcel.recycle();

                //SAVE PENDING INTENT TO DATABASE
                if(state_add_or_fix == TimeElement.STATE_FIX_ALARM){
                    Log.i("AAA","STATE FIX ALARM");
                    Thread a;
                    a = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveDataToSQLite.updateDataToTablePendingIntent(item.getIdAlarm(),bytesArrayPendingIntent);
                        }
                    });
                    a.start();
                    try {
                        a.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.i("AAA","STATE ADD ALARM");
                    Thread a;
                     a = new Thread(new Runnable() {
                         @Override
                         public void run() {
                            saveDataToSQLite.saveDataToTablePendingIntent(idNotification,bytesArrayPendingIntent);
                         }
                     });
                     a.start();
                    try {
                        a.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//

                int vibrateState = vibrate ? TimeElement.STATE_VIBRATE_ON : TimeElement.STATE_VIBRATE_OFF;

                //KIỂM TRA NẾU LÀ TRẠNG THÁI SỬA BÁO THỨC THÌ CHỈ UPDATE LẠI BÁO THỨC
                //ĐÃ LƯU Ở DATABASE VỚI ID BÁO THỨC ĐÓ
                //KHÔNG THÌ QUERY ĐỂ SAVE BÁO THỨC MỚI
                //NẾU LÀ THÊM BÁO THỨC THÌ INSERT
                if(state_add_or_fix == TimeElement.STATE_ADD_ALARM){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_ID,timeElement.getIdAlarm());
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_HOURS,timeElement.getHour());
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_MINUTES,timeElement.getMinute());
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_NOTES,timeElement.getNote());
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_REGULARS,timeElement.getRegular());
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_TIME_COUNTDOWN,timeElement.getTimeCountdown());
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_STATE_ALARM,1);
                    contentValues.put(SaveDataToSQLite.COLUMN_NAME_STATE_VIBRATE,vibrateState);

                    Thread a;
                    a = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveDataToSQLite.insertItemInDatabase(contentValues);
                        }
                    });
                    a.start();
                    try {
                        a.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //NẾU LÀ THÊM BÁO THỨC THÌ MỚI TĂNG idNotification
                    ++idNotification;
                    saveIDNotification();
                }
                //NẾU LÀ SỬA BÁO THỨC
                if(state_add_or_fix == TimeElement.STATE_FIX_ALARM){
                    Thread a;
                    a = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveDataToSQLite.queryToUpdateDataToDatabase(timeElement);
                        }
                    });
                    a.start();
                    try {
                        a.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //QUAY TRỞ VỀ MAINACTIVITY.CLASS
                setResult(RESULT_OK,intentResult);
                finish();
            }

        });

        //SET SCROLL CHO TIMEPICKER VÀ SET GIỜ, PHÚT ĐÓ LÊN TEXTVIEWHOUR
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourChange, int minuteChange) {
                Calendar calendarFromChange = Calendar.getInstance();
                calendarFromChange.set(Calendar.HOUR_OF_DAY,hourChange);
                calendarFromChange.set(Calendar.MINUTE,minuteChange);
                Date currentTime = calendarCurrent.getTime();
                Date newDate = calendarFromChange.getTime();
                int hourChangeE,minuteChangeE;
                long milliseconds = newDate.getTime() - currentTime.getTime();

                if (milliseconds <= 0) {
                    calendarFromChange.add(Calendar.DATE, 1);
                    Date dateNew = calendarFromChange.getTime();
                    milliseconds = dateNew.getTime() - currentTime.getTime();
                }

                hourChangeE   = (int) ((milliseconds / (1000*60*60)) % 24);
                minuteChangeE = (int) ((milliseconds / (1000*60)) % 60);

                if(hourChangeE==0){
                    textViewTimeRemain.setText("Báo thức sau "+minuteChangeE+" phút");
                }
                else{
                    if(minuteChangeE==0){
                        textViewTimeRemain.setText("Báo thức sau "+hourChangeE+" giờ ");
                    }
                    else{
                        textViewTimeRemain.setText("Báo thức sau "+hourChangeE+" giờ "+minuteChangeE+" phút");
                    }
                }
            }
        });

        //BUTTON THOÁT ACTIVITY TẠO BÁO THỨC CHUYỂN VỀ MainActivity.java
        buttonCLose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //ONCLICK CỦA SWITCH CÀI RUNG KHI BÁO THỨC
        switchMaterialVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    switchMaterialVibrate.setBackground(switchMaterialVibrate.getResources().getDrawable(R.drawable.custom_background_switch_on));
                    switchMaterialVibrate.setChecked(true);
                }
                else{
                    switchMaterialVibrate.setBackground(switchMaterialVibrate.getResources().getDrawable(R.drawable.custom_background_switch_off));
                    switchMaterialVibrate.setChecked(false);
                }
            }
        });

        //ONCLICK CỦA SWITCH XÓA BÁO THỨC SAU KHI ĐÃ BÁO THỨC
        switchMaterialOffAfterRing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    switchMaterialOffAfterRing.setBackground(switchMaterialOffAfterRing.getResources().getDrawable(R.drawable.custom_background_switch_on));
                }
                else{
                    switchMaterialOffAfterRing.setBackground(switchMaterialOffAfterRing.getResources().getDrawable(R.drawable.custom_background_switch_off));
                }
            }
        });


        //ONCLICK LAYOUT LẶP LẠI 1 LẦN
        onceRepeat.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) { tempStr = "Một lần";changeBackgroundItemRegular(0); }
        });

        //ONCLICK LAYOUT LẶP LẠI HẰNG NGÀY
        allDateRepeat.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) { tempStr = "Th2 Th3 Th4 Th5 Th6 Th7 CN";changeBackgroundItemRegular(1); }
        });

        //ONCLICK LAYOUT LẶP LẠI TỪ THỨ 2 ĐẾN THỨ 6
        from2To6Repeat.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) { tempStr = "Th2 Th3 Th4 Th5 Th6";changeBackgroundItemRegular(2); }
        });


        //ONCLICK LAYOUT LẶP LẠI THEO YÊU CẦU NGƯỜI DÙNG(THỨ 2 ĐẾN CHỦ NHẬT)
        optionsRepeat.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                changeBackgroundItemRegular(3);
                showOptionsMenuRegular();
            }
        });

        //ONCLICK ĐỂ SHOW BOTTOMSHEETDIALOG CÁC LỰA CHỌN(MỘT LẦN, HẰNG NGÀY...) CỦA LỰA CHỌN "LẶP LẠI"
        regularRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogRegularRepeat.show();
            }
        });


        //ONCLICK CỦA LAYOUT THỨ 2
        layoutThu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(0);
            }
        });

        //ONCLICK CỦA LAYOUT THỨ 3
        layoutThu3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(1);
            }
        });

        //ONCLICK CỦA LAYOUT THỨ 4
        layoutThu4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(2);
            }
        });

        //ONCLICK CỦA LAYOUT THỨ 5
        layoutThu5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(3);
            }
        });

        //ONCLICK CỦA LAYOUT THỨ 6
        layoutThu6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(4);
            }
        });

        //ONCLICK CỦA LAYOUT THỨ 7
        layoutThu7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(5);
            }
        });

        //ONCLICK CỦA LAYOUT CHỦ NHẬT
        layoutThu8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomSheetOptionsClicked(6);
            }
        });


        //NÚT HỦY BỎ ĐẶT LỰA CHỌN CỦA BOTTOMSHEETDIALOG TỪ THỨ 2 ĐẾN CHỦ NHẬT
        buttonCancelFrom2To8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogOptionsMenu.dismiss();
            }
        });

        //NÚT ĐẶT LỰA CHỌN LẶP LẠI CỦA NGƯỜI DÙNG TỪ THỨ 2 ĐẾN CHỦ NHẬT
        buttonOkFrom2To8.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String regular = getRegularSelection();
                tempStr = regular;
                if(!regular.isEmpty() && allIsChecked!=7){
                    if(checkBoxThu2.isChecked() && checkBoxThu3.isChecked() && checkBoxThu4.isChecked() &&
                            checkBoxThu5.isChecked() && checkBoxThu6.isChecked()){
                        textRegular.setText("Thứ hai đến Thứ sáu");
                    }
                    else{
                        textRegular.setText(regular);
                    }
                }
                else if(!regular.isEmpty() && allIsChecked==7){
                    textRegular.setText("Hằng ngày");
                }
                else{
                    textRegular.setText("Một lần");
                }
                bottomSheetDialogOptionsMenu.dismiss();
            }
        });

    }


    //THAY ĐỔI BACKGROUND CỦA CÁC ITEM TRONG BOTTOMSHEETDIALOG PHẦN LẶP LẠI
    @SuppressLint("UseCompatLoadingForDrawables")
    public void changeBackgroundItemRegular(int position){
        switch(position) {
            case 0: // LẶP LẠI 1 LẦN
                textRegular.setText("Một lần");
                onceRepeat.setBackground(getResources().getDrawable(R.color.backgroundItemRegularSelected));

                textViewOnce.setTextColor(getResources().getColor(R.color.textColorItemRegularSelected));
                textViewAllDate.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewFrom2To6.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewOptions.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));

                regularOnceCheck.setVisibility(View.VISIBLE);
                regularAllDateCheck.setVisibility(View.INVISIBLE);
                regularFrom2To6Check.setVisibility(View.INVISIBLE);
                regularOptionsCheck.setVisibility(View.INVISIBLE);

                allDateRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                from2To6Repeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                optionsRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                bottomSheetDialogRegularRepeat.dismiss();
                break;
            case 1:// LẶP LẠI HẰNG NGÀY
                textRegular.setText("Hằng ngày");
                allDateRepeat.setBackground(getResources().getDrawable(R.color.backgroundItemRegularSelected));

                textViewOnce.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewAllDate.setTextColor(getResources().getColor(R.color.textColorItemRegularSelected));
                textViewFrom2To6.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewOptions.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));

                regularOnceCheck.setVisibility(View.INVISIBLE);
                regularAllDateCheck.setVisibility(View.VISIBLE);
                regularFrom2To6Check.setVisibility(View.INVISIBLE);
                regularOptionsCheck.setVisibility(View.INVISIBLE);

                onceRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                from2To6Repeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                optionsRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                bottomSheetDialogRegularRepeat.dismiss();
                break;
            case 2:// LẶP LẠI TỪ THỨ 2 ĐẾN THỨ 6
                textRegular.setText("Thứ hai đến Thứ sáu");
                from2To6Repeat.setBackground(getResources().getDrawable(R.color.backgroundItemRegularSelected));

                textViewOnce.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewAllDate.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewFrom2To6.setTextColor(getResources().getColor(R.color.textColorItemRegularSelected));
                textViewOptions.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));

                regularOnceCheck.setVisibility(View.INVISIBLE);
                regularAllDateCheck.setVisibility(View.INVISIBLE);
                regularFrom2To6Check.setVisibility(View.VISIBLE);
                regularOptionsCheck.setVisibility(View.INVISIBLE);

                onceRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                allDateRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                optionsRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                bottomSheetDialogRegularRepeat.dismiss();
                break;
            case 3:// LẶP LẠI TỪ THỨ 2 ĐẾN CHỦ NHẬT
                optionsRepeat.setBackground(getResources().getDrawable(R.color.backgroundItemRegularSelected));

                textViewOnce.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewAllDate.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewFrom2To6.setTextColor(getResources().getColor(R.color.textColorItemRegularUnSelected));
                textViewOptions.setTextColor(getResources().getColor(R.color.textColorItemRegularSelected));

                regularOnceCheck.setVisibility(View.INVISIBLE);
                regularAllDateCheck.setVisibility(View.INVISIBLE);
                regularFrom2To6Check.setVisibility(View.INVISIBLE);
                regularOptionsCheck.setVisibility(View.VISIBLE);

                onceRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                allDateRepeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                from2To6Repeat.setBackground(getResources().getDrawable(R.color.custom_item_alarm_click));
                bottomSheetDialogRegularRepeat.dismiss();
                break;
            default:
                bottomSheetDialogRegularRepeat.dismiss();
                break;
        }
    }

    //function HIỂN THỊ BOTTOMSHEETDIALOG PHẦN TÙY CHỌN LẶP LẠI (THỨ 2 -> CHỦ NHẬT)
    // VÀ TICK CÁC CHECKBOX ĐÃ CHỌN
    public void showOptionsMenuRegular(){
        int mode = getIntent().getIntExtra("MODE_ADD_ALARM",1);
        TimeElement item = (TimeElement) getIntent().getSerializableExtra("TIME_ELEMENT");
        if(item != null && !checkFirstTime){
            item.setRegular(tempStr);
        }
        if(mode == 2){
            String regularStr = item.getRegular();
            if( !regularStr.equals("Một lần")){
                String[] arr = regularStr.split(" ");
                if(regularStr.equals("Hằng ngày")){
                    arr = "Th2 Th3 Th4 Th5 Th6 Th7 CN".split(" ");
                }
                if(regularStr.equals("Thứ hai đến Thứ sáu")){
                    arr = "Th2 Th3 Th4 Th5 Th6".split(" ");
                }
                checkBoxThu7.setChecked(false);checkBoxThu8.setChecked(false);
                for (String a : arr){
                    switchCaseTickCheckBox(a);
                }
            }
            else {
                checkBoxThu2.setChecked(false);checkBoxThu3.setChecked(false);
                checkBoxThu4.setChecked(false);checkBoxThu5.setChecked(false);
                checkBoxThu6.setChecked(false);checkBoxThu7.setChecked(false);
                checkBoxThu8.setChecked(false);
            }
        }
        else {
            if(!tempStr.equals("Một lần")){
                String[] arr = tempStr.split(" ");
                checkBoxThu7.setChecked(false);checkBoxThu8.setChecked(false);
                for (String a : arr){
                    switchCaseTickCheckBox(a);
                }
            }
            else{
                checkBoxThu2.setChecked(false);checkBoxThu3.setChecked(false);
                checkBoxThu4.setChecked(false);checkBoxThu5.setChecked(false);
                checkBoxThu6.setChecked(false);checkBoxThu7.setChecked(false);
                checkBoxThu8.setChecked(false);
            }
        }
        checkFirstTime = false;
        bottomSheetDialogOptionsMenu.show();
    }

    public void switchCaseTickCheckBox(String temp){
        switch (temp){
            case "Th2":
                checkBoxThu2.setChecked(true);break;
            case "Th3":
                checkBoxThu3.setChecked(true);break;
            case "Th4":
                checkBoxThu4.setChecked(true);break;
            case "Th5":
                checkBoxThu5.setChecked(true);break;
            case "Th6":
                checkBoxThu6.setChecked(true);break;
            case "Th7":
                checkBoxThu7.setChecked(true);break;
            case "CN":
                checkBoxThu8.setChecked(true);break;
            default:
                break;
        }
    }

    //SET CLICKED TRÊN MỖI CHECKBOX
    public void onBottomSheetOptionsClicked(int position){
        switch (position){
            case 0://TRƯỜNG HỢP LẶP LẠI VÀO THỨ 2 NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu2.setChecked(!checkBoxThu2.isChecked());
                break;
            case 1://TRƯỜNG HỢP LẶP LẠI VÀO THỨ 3 NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu3.setChecked(!checkBoxThu3.isChecked());
                break;
            case 2://TRƯỜNG HỢP LẶP LẠI VÀO THỨ 4 NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu4.setChecked(!checkBoxThu4.isChecked());
                break;
            case 3://TRƯỜNG HỢP LẶP LẠI VÀO THỨ 5 NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu5.setChecked(!checkBoxThu5.isChecked());
                break;
            case 4://TRƯỜNG HỢP LẶP LẠI VÀO THỨ 6 NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu6.setChecked(!checkBoxThu6.isChecked());
                break;
            case 5://TRƯỜNG HỢP LẶP LẠI VÀO THỨ 7 NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu7.setChecked(!checkBoxThu7.isChecked());
                break;
            case 6://TRƯỜNG HỢP LẶP LẠI VÀO CHỦ NHẬT NẾU CHECK BOX IS CHECKED THÌ SET NGƯỢC LẠI
                checkBoxThu8.setChecked(!checkBoxThu8.isChecked());
                break;
            default:
                break;
        }
    }

    //KIỂM TRA CHECKED CÁC CHECKBOX
    public String getRegularSelection(){
        allIsChecked=0;
        if(checkBoxThu2.isChecked()){
            textThu2="Th2";++allIsChecked;}
        else{
            textThu2="";--allIsChecked;}
        if(checkBoxThu3.isChecked()){
            textThu3="Th3";++allIsChecked;}
        else{
            textThu3="";--allIsChecked;}
        if(checkBoxThu4.isChecked()){
            textThu4="Th4";++allIsChecked;}
        else{
            textThu4="";--allIsChecked;}
        if(checkBoxThu5.isChecked()){
            textThu5="Th5";++allIsChecked;}
        else{
            textThu5="";--allIsChecked;}
        if(checkBoxThu6.isChecked()){
            textThu6="Th6";++allIsChecked;}
        else{
            textThu6="";--allIsChecked;}
        if(checkBoxThu7.isChecked()){
            textThu7="Th7";++allIsChecked;}
        else{
            textThu7="";--allIsChecked;}
        if(checkBoxThu8.isChecked()){
            textThu8="CN";++allIsChecked;}
        else{
            textThu8="";--allIsChecked;}
        String[] regularArray = {textThu2,textThu3,textThu4,textThu5,textThu6,textThu7,textThu8};

        return TextUtils.join(" ",regularArray).replaceAll("\\s{2,}", " ").trim();
    }


    //TÌM THỜI GIAN ĐẾM NGƯỢC CHO ĐẾN KHI BÁO THỨC
    public String findTimeRemain(Date current,Date setTime){
        int hours = 0,minutes = 0;
        long milliseconds = setTime.getTime() - current.getTime();

        if (milliseconds <= 0) {
            calendar.add(Calendar.DATE, 1);
            Date dateNew = calendar.getTime();
            milliseconds = dateNew.getTime() - current.getTime();
        }
        hours   = (int) ((milliseconds / (1000*60*60)) % 24);
        minutes = (int) ((milliseconds / (1000*60)) % 60);

        return hours > 0 ? minutes > 0 ? "Báo thức sau " + hours + " giờ " + minutes + " phút"
                                        : "Báo thức sau " + hours + " giờ " :
                "Báo thức sau " + minutes + " phút";
    }



    public void saveIDNotification(){
        SharedPreferences sharedPreferences = getSharedPreferences("metadata", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("idNotification",idNotification);
        editor.commit();
    }
}