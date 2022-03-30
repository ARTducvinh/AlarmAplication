package com.example.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeFavoritesItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static List<TimeFavoritesItem> favoritesItemList;
    public static Context context;
    @SuppressLint("StaticFieldLeak")
    public static CountDownTimerFragment countDownTimerFragment;
    @SuppressLint("StaticFieldLeak")
    public static BottomSheetDialog bottomSheetDialogAddTimeFavorites;
    public static List<ViewHolderNormal> viewList = new ArrayList<>();
    public static List<Boolean> stateDelete = new ArrayList<>();
    List<String> hoursPickerValues = new ArrayList<>();
    List<String> minutesPickerValues = new ArrayList<>();
    NumberPicker hoursPicker, minutesPicker;
    TextView buttonCancel, buttonOke;


    public TimeFavoritesItemAdapter(Context context, List<TimeFavoritesItem> list, CountDownTimerFragment countDownTimerFragment) {
        TimeFavoritesItemAdapter.context = context;
        favoritesItemList = list;
        TimeFavoritesItemAdapter.countDownTimerFragment = countDownTimerFragment;
        bottomSheetDialogAddTimeFavorites = new BottomSheetDialog(context, R.style.CustomBottomSheetBorder);
        initHoursPickerValues();
        initMinutesPickerValues();
        createBottomSheetDialogAddTimeFragment();
        //initStateDelete();
    }

    public static void sortCollection() {
        viewList.sort(new Comparator<ViewHolderNormal>() {
            @Override
            public int compare(ViewHolderNormal viewHolderNormal, ViewHolderNormal t1) {
                return Integer.parseInt(viewHolderNormal.textViewTime.getText().toString()) - Integer.parseInt(t1.textViewTime.getText().toString());
            }
        });
    }

    public static void changeStateViewAtIndex(int index) {
        viewList.get(index).textViewDelete.setVisibility(View.GONE);
        viewList.get(index).textViewTime.setVisibility(View.VISIBLE);
        viewList.get(index).textViewMinutesText.setVisibility(View.VISIBLE);
        stateDelete.set(index, false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("AAA", "ITEM ON BIND");
        View view;
        if (viewType == TimeFavoritesItem.TYPE_NORMAL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_favorites_item, parent, false);
            return new ViewHolderNormal(view, this);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add, parent, false);
            return new ViewHolderSpecial(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TimeFavoritesItem item = favoritesItemList.get(position);
        if (holder.getItemViewType() == TimeFavoritesItem.TYPE_NORMAL) {
            ViewHolderNormal normal = (ViewHolderNormal) holder;
            normal.textViewTime.setText(String.valueOf(item.calculateTimeToMinutes()));
            viewList.add(normal);
            sortCollection();
            changeStateAllViews();
        } else {
            if (favoritesItemList.size() == 1) {
                countDownTimerFragment.recyclerViewTimeListNotes.setPadding(410, 0, 0, 0);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (favoritesItemList == null) {
            return 0;
        }
        return favoritesItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        TimeFavoritesItem item = favoritesItemList.get(position);
        if (item.getType() == TimeFavoritesItem.TYPE_NORMAL) {
            return TimeFavoritesItem.TYPE_NORMAL;
        } else {
            return TimeFavoritesItem.TYPE_SPECIAL;
        }
    }

    public void initHoursPickerValues() {
        for (int i = 0; i <= 23; i++) {
            hoursPickerValues.add(i < 10 ? "0" + i : String.valueOf(i));
        }
    }

    public void initMinutesPickerValues() {
        for (int i = 0; i <= 59; i++) {
            minutesPickerValues.add(i < 10 ? "0" + i : String.valueOf(i));
        }
    }

    public void changeStateAllViews() {
        for (int i = 0; i < viewList.size(); i++) {
            changeStateViewAtIndex(i);
        }
    }

    public void changeStateDeleteForFAll() {
        for (int i = 0; i < stateDelete.size(); i++) {
            stateDelete.set(i, false);
        }
    }

    @SuppressLint("CutPasteId")
    public void createBottomSheetDialogAddTimeFragment() {

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add_time_favorites, null, false);
        //HOOKS VIEWS
        hoursPicker = view.findViewById(R.id.pickerHours);
        minutesPicker = view.findViewById(R.id.pickerMinutes);
        buttonCancel = view.findViewById(R.id.buttonCancelSetTimeFavorites);
        buttonOke = view.findViewById(R.id.buttonSetTimeFavorites);
        //SET VALUES FOR HOURS AND MINUTES PICKER
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(hoursPickerValues.size() - 1);
        hoursPicker.setDisplayedValues(hoursPickerValues.toArray(new String[]{}));

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(minutesPickerValues.size() - 1);
        minutesPicker.setDisplayedValues(minutesPickerValues.toArray(new String[]{}));

        hoursPicker.setValue(0);
        minutesPicker.setValue(5);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialogAddTimeFavorites.dismiss();
            }
        });

        buttonOke.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                TimeFavoritesItem item = new TimeFavoritesItem(hoursPicker.getValue(), minutesPicker.getValue(), TimeFavoritesItem.TYPE_NORMAL);
                int index = findPositionToInsertNewItem(item.calculateTimeToMinutes());
                stateDelete.add(false);
                favoritesItemList.add(index, item);
                notifyItemInserted(index);
                hoursPicker.setValue(0);
                minutesPicker.setValue(5);
                bottomSheetDialogAddTimeFavorites.dismiss();
                if (favoritesItemList.size() > 1) {
                    countDownTimerFragment.recyclerViewTimeListNotes.setPadding(0, 0, 0, 0);
                }
            }
        });

        bottomSheetDialogAddTimeFavorites.setContentView(view);
    }

    public int findPositionToInsertNewItem(int values) {
        List<Integer> listValuesMinutes = new ArrayList<>();

        for (int i = 0; i < favoritesItemList.size() - 1; i++) {
            listValuesMinutes.add(favoritesItemList.get(i).calculateTimeToMinutes());
        }

        listValuesMinutes.add(values);
        Collections.sort(listValuesMinutes);

        return listValuesMinutes.indexOf(values);
    }

    public static class ViewHolderNormal extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView textViewTime, textViewDelete, textViewMinutesText;
        RelativeLayout backgroundTimeFavoritesItem;
        TimeFavoritesItemAdapter adapter;

        public ViewHolderNormal(@NonNull View itemView, TimeFavoritesItemAdapter adapter) {
            super(itemView);
            textViewTime = itemView.findViewById(R.id.timeMinutes);
            textViewDelete = itemView.findViewById(R.id.textDelete);
            textViewMinutesText = itemView.findViewById(R.id.textViewMinutesText);
            backgroundTimeFavoritesItem = itemView.findViewById(R.id.backgroundTimeFavoritesItem);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (stateDelete.get(getAdapterPosition())) {
                stateDelete.remove(getAdapterPosition());
                viewList.remove(getAdapterPosition());

                favoritesItemList.remove(getAdapterPosition());
                adapter.notifyItemRemoved(getAdapterPosition());
                if (favoritesItemList.size() == 1) {
                    countDownTimerFragment.recyclerViewTimeListNotes.setPadding(410, 0, 0, 0);
                }
                sortCollection();
            } else {
                CountDownTimerFragment.mMainActivity.changeStateEnableButtonStartCountDown(0);
                countDownTimerFragment.showRecyclerViewTimeFavorites(View.GONE);
                CountDownTimerFragment.mMainActivity.changeStateButtonSetTimeFavoritesClose();
                countDownTimerFragment.hoursPicker.setValue(favoritesItemList.get(getAdapterPosition()).getHour());
                countDownTimerFragment.minutesPicker.setValue(favoritesItemList.get(getAdapterPosition()).getMinute());
                countDownTimerFragment.secondsPicker.setValue(0);
                for (int i = 0; i < viewList.size(); i++) {
                    changeStateViewAtIndex(i);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {


            for (int i = 0; i < stateDelete.size(); i++) {
                stateDelete.set(i, false);
            }

            stateDelete.set(getAdapterPosition(), true);


            //FIX STATEDELETE TRUE BUT UPDATE WRONG VIEW
            for (int i = 0; i < viewList.size(); i++) {
                if (stateDelete.get(i)) {
                    viewList.get(getAdapterPosition()).textViewTime.setVisibility(View.GONE);
                    viewList.get(getAdapterPosition()).textViewMinutesText.setVisibility(View.GONE);
                    viewList.get(getAdapterPosition()).textViewDelete.setVisibility(View.VISIBLE);
                } else {
                    changeStateViewAtIndex(i);
                }
            }

            return true;
        }
    }

    public static class ViewHolderSpecial extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewHolderSpecial(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            bottomSheetDialogAddTimeFavorites.show();
            for (int i = 0; i < viewList.size(); i++) {
                changeStateViewAtIndex(i);
            }
        }
    }
}
