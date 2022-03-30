package com.example.application;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class TimeCountUpFragment extends Fragment {


    private static int tempSec = 0;
    //PROPERTIES
    private boolean startCountTimeUp = false;
    private TextView textViewTimeCountUp, textViewText;
    private Timer timer;
    private TimerTask doCountTimeUp;
    private Integer currentTimeCountUp = 0;
    private RecyclerView recyclerViewShowMilestonesItems;
    private MilestonesAdapter milestonesAdapter;
    private int order = 0;
    private String oldHour = "00", oldMinutes = "00", oldSeconds = "00", oldSecs = "00";
    private String hourCurr = "00", minutesCurr = "00", secondCurr = "00", secsCurr = "00";
    private List<MilestonesItem> list = new ArrayList<>();
    private Intent intentServices;
    private boolean overStep = true;
    private Context context;
    //private CountUpBroadcast receiver = new CountUpBroadcast();
    private ChangeStateButtonCallBack changeStateButtonCallBack;

    public TimeCountUpFragment() {
    }

    public TimeCountUpFragment(Context context, ChangeStateButtonCallBack changeStateButtonCallBackTemp) {
        this.context = context;
        changeStateButtonCallBack = changeStateButtonCallBackTemp;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        timer = new Timer("TIMER");
        View view = inflater.inflate(R.layout.time_count_up_fragment, container, false);
        textViewTimeCountUp = view.findViewById(R.id.textViewTimeCountUp);
        textViewText = view.findViewById(R.id.textViewTimeC);
        recyclerViewShowMilestonesItems = (RecyclerView) view.findViewById(R.id.recyclerViewShowMilestones);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        //REVERSE RECYCLER VIEW LAYOUT
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewShowMilestonesItems.setLayoutManager(linearLayoutManager);

        milestonesAdapter = new MilestonesAdapter(list);

        recyclerViewShowMilestonesItems.setAdapter(milestonesAdapter);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (overStep) {

            SharedPreferences sharedPreferences = context.getSharedPreferences("metadata_time_count_up", MODE_PRIVATE);
            currentTimeCountUp = sharedPreferences.getInt("timeCurrentCountUp", 0);
            tempSec = sharedPreferences.getInt("timeSecsCountUp", 0);
            startCountTimeUp = sharedPreferences.getBoolean("state", false);
            int code = sharedPreferences.getInt("code", 1);

            if (code == -1 && currentTimeCountUp != 0 && tempSec != 0) {
                //USING GSON TO RETRIEVE OBJECT FROM JSON
                Gson gson = new Gson();
                String json = sharedPreferences.getString("list", null);
                Type type = new TypeToken<ArrayList<MilestonesItem>>() {
                }.getType();
                list = gson.fromJson(json, type);

                //.i("AAA","LIST SIZE AFTER IS : "+list.size());
                if (list != null) {
                    scaleLayoutViewToTopLeft(true);
                    showOrHideRecyclerviewShowMilestoneItems(View.VISIBLE);
                    order = list.size();
                    milestonesAdapter.updateRecyclerView(list);

                    MilestonesItem itemLast = list.get(order - 1);

                    String[] milestonesItemArray = itemLast.getTimeMilestone().split(":");
                    int sizeMilestoneItem = milestonesItemArray.length;

                    if (sizeMilestoneItem == 3) {
                        setOldTime(milestonesItemArray[0], milestonesItemArray[1]
                                , milestonesItemArray[2].split("\\.")[0],
                                milestonesItemArray[2].split("\\.")[1]);
                    } else {
                        String[] itemArr = milestonesItemArray[1].split("\\.");
                        setOldTime("00", milestonesItemArray[0]
                                , itemArr[0]
                                , itemArr[1]);
                    }

                } else {
                    list = new ArrayList<>();
                }
            }

            if (startCountTimeUp) {
                Calendar calendarOld = Calendar.getInstance();
                calendarOld.setTimeInMillis(sharedPreferences.getLong("timeLast", 0));
                long timeSub = Calendar.getInstance().getTimeInMillis() - calendarOld.getTimeInMillis();
                timeSub = (timeSub / 1000) % 60;
                tempSec = 0;
                currentTimeCountUp = currentTimeCountUp + Integer.parseInt(String.valueOf(timeSub));
                startCountTimeUp = false;

                startCountTimeUp();
            } else {
                if (currentTimeCountUp != 0 || tempSec != 0) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            textViewTimeCountUp.post(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    String temp = String.valueOf(tempSec);
                                    String a = temp.length() == 1 ? "0" + temp : temp;
                                    textViewTimeCountUp.setText(getTimeCountUpToText() + "." + a);
                                }
                            });
                        }
                    }).start();
                    changeStateButtonCallBack.changeStateButton(true);
                }
                startCountTimeUp = false;
            }
            overStep = false;
        } else {

        }
    }


    //HÀM BẮT ĐẦU ĐẾM THỜI GIAN
    public void startCountTimeUp() {
        if (!startCountTimeUp) {
            //START COUNT TIME UP
            startCountTimeUp = true;
            doCountTimeUp = new TimerTask() {
                @Override
                public void run() {
                    textViewTimeCountUp.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            tempSec++;
                            //NẾU TEMPSEC ĐỦ 100 THÌ TĂNG GIÂY LÊN 1
                            if (tempSec == 100) {
                                currentTimeCountUp++;
                            }
                            if (tempSec < 10) {
                                textViewTimeCountUp.setText(getTimeCountUpToText() + ".0" + tempSec);
                            } else {
                                if (tempSec != 100) {
                                    textViewTimeCountUp.setText(getTimeCountUpToText() + "." + tempSec);
                                } else {
                                    textViewTimeCountUp.setText(getTimeCountUpToText() + ".00");
                                }
                            }
                            //ĐỦ 100 THÌ SET LẠI VỀ 0
                            if (tempSec == 100) {
                                tempSec = 0;
                            }
                        }
                    });
                }
            };
            //DÙNG TIMER ĐỂ THỰC HIỆN 1 HÀM THEO CHU KÌ
            timer.schedule(doCountTimeUp, 0, 10);
        }
    }

    //HÀM DỪNG TÍNH THỜI GIAN
    public void pauseCountTimeUp() {
        doCountTimeUp.cancel();
        startCountTimeUp = false;
    }

    //HÀM DỪNG THỜI GIAN
    @SuppressLint("SetTextI18n")
    public void resetCountTimeUp() {
        if (doCountTimeUp == null) {
            textViewTimeCountUp.setText("00:00.00");
        } else {
            doCountTimeUp.cancel();
        }
        startCountTimeUp = false;
        currentTimeCountUp = 0;
        tempSec = 0;
        textViewTimeCountUp.setText("00:00.00");

        //
        oldHour = "00";
        oldMinutes = "00";
        oldSecs = "00";
        oldSecs = "00";
        hourCurr = "00";
        minutesCurr = "00";
        secondCurr = "00";
        secsCurr = "00";
        order = 0;
        milestonesAdapter.clearAllItems();
        SharedPreferences sharedPreferences = context.getSharedPreferences("metadata_time_count_up", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("timeCurrentCountUp", currentTimeCountUp);
        editor.putInt("timeSecsCountUp", tempSec);
        editor.putBoolean("state", false);
        editor.commit();
    }

    //HÀM TÍNH THỜI GIAN TỪ CURRENTTIMECOUTUP RA GIỜ, PHÚT , GIÂY
    public String getTimeCountUpToText() {
        int rounded = (int) Math.round(currentTimeCountUp);
        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);
        return formatTime(seconds, minutes, hours);
    }

    //FORMAT THỜI GIAN
    @SuppressLint("DefaultLocale")
    public String formatTime(int seconds, int minutes, int hours) {
        if (hours != 0) {
            return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        }
        return String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

    //GET TEXT VIEW OF TIME UP
    public String timeUpGetText() {
        return textViewTimeCountUp.getText().toString();
    }


    //TRẢ VỀ TRẠNG THÁI ĐANG CHẠY HAY DỪNG
    public boolean getIsStartCountUp() {
        return this.startCountTimeUp;
    }

    public void setStartCountTimeUp(boolean bool) {
        this.startCountTimeUp = bool;
    }

    public void showOrHideRecyclerviewShowMilestoneItems(int visibility) {
        recyclerViewShowMilestonesItems.setVisibility(visibility);
    }

    //LẤY MỐC THỜI GIAN
    public void takeMilestones() {
        String[] timeCurr = textViewTimeCountUp.getText().toString().split(":");
        String[] timeSecondsAndSecs = timeCurr[1].split("\\.");
        minutesCurr = timeCurr[0].length() != 1 ? timeCurr[0] : "0" + timeCurr[0];
        secondCurr = timeSecondsAndSecs[0].length() != 1 ? timeSecondsAndSecs[0] : "0" + timeSecondsAndSecs[0];
        secsCurr = timeSecondsAndSecs[1].length() != 1 ? timeSecondsAndSecs[1] : "0" + timeSecondsAndSecs[1];

        int sizeTimeStr = timeCurr.length;
        if (sizeTimeStr == 3) {
            timeSecondsAndSecs = timeCurr[2].split("\\.");
            hourCurr = timeCurr[0].length() != 1 ? timeCurr[0] : "0" + timeCurr[0];
            minutesCurr = timeCurr[1].length() != 1 ? timeCurr[1] : "0" + timeCurr[1];
            secondCurr = timeSecondsAndSecs[0].length() != 1 ? timeSecondsAndSecs[0] : "0" + timeSecondsAndSecs[0];
            secsCurr = timeSecondsAndSecs[1].length() != 1 ? timeSecondsAndSecs[1] : "0" + timeSecondsAndSecs[1];
        }


        if (order == 0) {
            String timePlus = "+ " + minutesCurr + ":" + secondCurr + "." + secsCurr;
            oldHour = hourCurr;
            oldMinutes = minutesCurr;
            oldSeconds = secondCurr;
            oldSecs = secsCurr;
            MilestonesItem temp = MilestonesItem.createMilestonesItem(order, timePlus, timePlus.replace("+ ", ""));
            milestonesAdapter.addMilestonesItem(temp);
        } else {

            Integer secsSub = Integer.parseInt(secsCurr) - Integer.parseInt(oldSecs);
            Integer secondsSub = Integer.parseInt(secondCurr) - Integer.parseInt(oldSeconds);
            Integer minutesSub = Integer.parseInt(minutesCurr) - Integer.parseInt(oldMinutes);
            Integer hoursSub = Integer.parseInt(hourCurr) - Integer.parseInt(oldHour);

            if (secsSub < 0) {
                secsSub = 100 + secsSub;
                secondsSub = secondsSub - 1;
            }

            if (secondsSub < 0) {
                secondsSub = 60 + secondsSub;
                minutesSub = minutesSub - 1;
            }

            if (minutesSub < 0) {
                hoursSub = hoursSub - 1;
            }

            String hoursStr, minutesStr, secondsStr, secsStr;

            hoursStr = String.valueOf(hoursSub);
            minutesStr = String.valueOf(minutesSub);
            secondsStr = String.valueOf(secondsSub);
            secsStr = String.valueOf(secsSub);


            hoursStr = hoursStr.length() == 1 ? "0" + hoursStr : hoursStr;
            minutesStr = minutesStr.length() == 1 ? "0" + minutesStr : minutesStr;
            secondsStr = secondsStr.length() == 1 ? "0" + secondsStr : secondsStr;
            secsStr = secsStr.length() == 1 ? "0" + secsStr : secsStr;

            String timePlusSub = minutesStr + ":" + secondsStr + "." + secsStr;

            if (hoursSub > 0) {
                timePlusSub = hoursStr + ":" + minutesStr + ":" + secondsStr + "." + secsStr;
            }

            oldHour = hourCurr;
            oldMinutes = minutesCurr;
            oldSeconds = secondCurr;
            oldSecs = secsCurr;

            //TẠO MỐC THỜI GIAN VÀ ĐẨY VÀO RECYCLERVIEW
            MilestonesItem temp = MilestonesItem.createMilestonesItem(order, "+ " + timePlusSub, minutesCurr + ":" + secondCurr + "." + secsCurr);
            milestonesAdapter.addMilestonesItem(temp);
            recyclerViewShowMilestonesItems.smoothScrollToPosition(order);
        }
        order++;
    }

    //XÓA TẤT CẢ CÁC MỐC THỜI GIAN
    public void clearAllItems() {
        milestonesAdapter.clearAllItems();
    }


    //ĐƯA LAYOUT HIỂN THỊ ĐẾM LÊN BÊN TRÁI
    public void scaleLayoutViewToTopLeft(boolean bool) {
        if (bool) {
            if (textViewText.getVisibility() != View.VISIBLE) {
                textViewText.setVisibility(View.VISIBLE);
                textViewTimeCountUp.setPadding(55, 120, 0, 0);
                recyclerViewShowMilestonesItems.setVisibility(View.VISIBLE);
            }
        } else {
            textViewText.setVisibility(View.GONE);
            textViewTimeCountUp.setPadding(200, 500, 0, 0);
            recyclerViewShowMilestonesItems.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (doCountTimeUp != null && timer != null) {
            doCountTimeUp.cancel();
            timer.cancel();
        }
        saveTheLastState(-1);
    }

    @Override
    public void onStop() {
        super.onStop();
        overStep = false;
        if (startCountTimeUp) {
            overStep = true;
            doCountTimeUp.cancel();
            saveTheLastState(-1);
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences("metadata_time_count_up", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("state", false);
            editor.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        overStep = false;
    }

    public void saveTheLastState(int code) {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences("metadata_time_count_up", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("timeCurrentCountUp", currentTimeCountUp);
        editor.putInt("timeSecsCountUp", tempSec);
        editor.putBoolean("state", startCountTimeUp);
        editor.putLong("timeLast", calendar.getTimeInMillis());
        editor.putInt("code", code);

        boolean checkListAdapterIsNull = milestonesAdapter.getListItemMilestones().isEmpty();
        if (code == -1 && currentTimeCountUp != 0 && tempSec != 0 && !checkListAdapterIsNull) {
            Gson gson = new Gson();
            String json = gson.toJson(milestonesAdapter.getListItemMilestones());
            editor.putString("list", json);
        } else {
            editor.remove("list");
        }
        editor.commit();
    }

    public void setOldTime(String hour, String minutes, String seconds, String secs) {
        oldHour = hourCurr;
        oldMinutes = minutesCurr;
        oldSeconds = seconds;
        oldSecs = secs;
    }
}