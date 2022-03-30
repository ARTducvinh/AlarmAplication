package com.example.application;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class AlarmFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static AlarmAdapter alarmAdapter;
    private final MainActivity mMainActivity;
    //CÁC THUỘC TÍNH
    private RecyclerView recyclerViewListAlarm;
    private List<TimeElement> timeElementList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private ImageView imageViewEmptyAlarm;
    private TextView textViewEmptyAlarm;

    //public static boolean checkHadInitRecyclerview = false;
    //HÀM KHỞI TẠO
    public AlarmFragment(List<TimeElement> timeElementList, MainActivity mainActivity) {
        this.timeElementList = timeElementList;
        this.mMainActivity = mainActivity;
    }

    @SuppressLint("NotifyDataSetChanged")
    private static void callUpdate(TimeElement timeElement) {
        //alarmAdapter.notifyItemChanged(0);
        alarmAdapter.callUpdate(timeElement);
    }

    //TẠO VIEW
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //ÁNH XẠ VIEW
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        recyclerViewListAlarm = view.findViewById(R.id.recyclerListAlarm);
        textViewEmptyAlarm = view.findViewById(R.id.textViewEmptyAlarm);
        imageViewEmptyAlarm = view.findViewById(R.id.imageViewEmptyAlarm);

        initRecyclerView();

        return view;
    }

    //GỌI ĐẾN HÀM addTimeElement TRONG AlarmAdapter để UPDATE ITEM MỚI ĐƯỢC THÊM
    @SuppressLint("NotifyDataSetChanged")
    public void getItemUpdate(TimeElement insertElement, int INT_ADD_OR_FIX_ALARM, int POSITION_ALARM_FIX) {
        alarmAdapter.addTimeElement(insertElement, INT_ADD_OR_FIX_ALARM, POSITION_ALARM_FIX);
    }

    //SCROLL TỚI ITEM VỪA MỚI ĐƯỢC TẠO HOẶC CẬP NHẬT LẠI
    public void scrollToPosition(int position) {
        recyclerViewListAlarm.smoothScrollToPosition(position);
    }

    //GỌI KHI DANH SÁCH BÁO THỨC KHÁC RỖNG ĐỂ KHỞI TẠO RECYCLERVIEW VÀ ADAPTER
    public void setAdapterForRecyclerview() {

    }

    //KHỞI TẠO RECYCLERVIEW VÀ SET ADAPTER CHO RECYCLERVIEW
    public void initRecyclerView() {
        alarmAdapter = new AlarmAdapter(timeElementList, getContext(), mMainActivity, this);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerViewListAlarm.setLayoutManager(layoutManager);
        recyclerViewListAlarm.setHasFixedSize(true);
//        recyclerViewListAlarm.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                recyclerViewListAlarm.setPadding(0,0,0,0);
//                if(layoutManager.findFirstCompletelyVisibleItemPosition()==0){
//                    recyclerViewListAlarm.setPadding(0,0,0,0);
//                }
//                if (layoutManager.findLastCompletelyVisibleItemPosition() == timeElementList.size()-1) {
//                    recyclerViewListAlarm.setPadding(0,0,0,250);
//                }
//            }
//        });
        recyclerViewListAlarm.setAdapter(alarmAdapter);
    }

    //HIDE ICON KHI KHÔNG CÓ BÁO THỨC NÀO
    public void hideEmptyAlarmIcon(boolean mode) {
        if (mode) {
            textViewEmptyAlarm.setVisibility(View.INVISIBLE);
            imageViewEmptyAlarm.setVisibility(View.INVISIBLE);
        } else {
            textViewEmptyAlarm.setVisibility(View.VISIBLE);
            imageViewEmptyAlarm.setVisibility(View.VISIBLE);
        }
    }

    //GỌI HÀM ONBACKPRESSEDTRUE TRONG ALARM ADAPTER
    public void callOnBackPressedFunctionInAlarmAdapter() {
        alarmAdapter.onBackPressedTrue();
    }

    //
    public void callSetCheckHasShowToastToFalse() {
        alarmAdapter.setCheckHasShowToastToFalseFromMainActivity();
    }

    //GỌI HÀM XÓA CÁC ITEMS ĐƯỢC CHỌN
    public void callDeleteItemSelectedAndUpdateUI() throws InterruptedException {
        alarmAdapter.deleteItemsSelected();
    }

    //GỌI HÀM ĐỂ CHỌN TẤT CẢ CÁC ITEMS HIỆN CÓ
    public void callFunctionSelectAllAlarm() {
        alarmAdapter.selectAllAlarmToDelete();
    }

    //INNER CLASS
    //BROADCAST RECEIVER
    public static class CustomBroadcast extends BroadcastReceiver {

        @SuppressLint("UnsafeProtectedBroadcastReceiver")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("AAA", "ON GET BROADCAST");
            //NHẬN DỮ LIỆU TỪ INTENT ĐÃ HỆN TRƯỚC TRUYỀN VÀO KHI ĐÚNG GIỜ
            Bundle getBundleFromBroadcastAndUIOverScreen = intent.getBundleExtra("bundle");
            Intent intentMusic = new Intent(context.getApplicationContext(), MusicServices.class);
            intentMusic.putExtra("bundle", getBundleFromBroadcastAndUIOverScreen);
            String state = "off";
            if (getBundleFromBroadcastAndUIOverScreen != null) {
                state = getBundleFromBroadcastAndUIOverScreen.getString("state");
            }

            // KIỂM TRA XEM MÀN HÌNH ĐANG BẬT HAY TẮT, NẾU TẮT THÌ MỞ MÀN HÌNH
            if (state.equals("on")) {
                PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
                if (!pm.isInteractive()) {
                    @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                    wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                }
            }

            Log.i("AAA", "ON WAKE UP SCREEN");

            // BẮT ĐẦU 1 SERVICE MỞ ÂM THANH VÀ RUNG
            TimeElement timeElement = (TimeElement) getBundleFromBroadcastAndUIOverScreen.getSerializable("timeEelement");

            if (!state.equals("on")) {
                String fromButtonTat = getBundleFromBroadcastAndUIOverScreen.getString("fromButtonTat", "false");
                SharedPreferences sharedPreferences = context.getSharedPreferences("metadata", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (fromButtonTat.equals("true")) {
                    editor.putString("fromButtonTat", "true");
                    editor.commit();
                    Log.i("AAA", "ID BROADCAST : " + timeElement.getIdAlarm());
                    callUpdate(timeElement);
                } else {
                    editor.putString("fromButtonTat", "false");
                    editor.commit();
                }
            } else {
//                SharedPreferences sharedPreferences = context.getSharedPreferences("metadata",Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                boolean destroyed = sharedPreferences.getBoolean("onMainDestroy",false);
//                if(!destroyed){
//                    callUpdate(timeElement);
//                }
            }
//            Log.i("AAA","ON START SERVICES");
            context.startService(intentMusic);
        }
    }


}