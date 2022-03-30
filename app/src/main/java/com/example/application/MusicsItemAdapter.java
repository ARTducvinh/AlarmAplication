package com.example.application;

import static com.example.application.CountDownTimerFragment.isRunning;
import static com.example.application.StartForeGroundServicesNotification.countDownTimer;
import static com.example.application.StartForeGroundServicesNotification.hasStarted;
import static com.example.application.StartForeGroundServicesNotification.mediaPlayer;
import static com.example.application.StartForeGroundServicesNotification.posSongChoice;
import static com.example.application.StartForeGroundServicesNotification.songs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MusicsItemAdapter extends RecyclerView.Adapter<MusicsItemAdapter.ViewHolder> {

    public static List<ImageView> viewList;
    public static Context context;
    List<MusicsItem> itemList;


    public MusicsItemAdapter(Context context, List<MusicsItem> list) {
        MusicsItemAdapter.context = context;
        this.itemList = list;
        viewList = new ArrayList<>();
        //initTheLastItemChoice();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_musics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        viewList.add(holder.icon);
        MusicsItem item = itemList.get(position);
        holder.icon.setImageResource(item.getIcon());
        holder.title.setText(item.getTitleMusics());
        if (position == posSongChoice) {
            holder.icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F03A54EC")));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    //TRY SET SOUND FOR NOTIFICATION WHEN PLAY


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView icon;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconItemMusics);
            title = itemView.findViewById(R.id.textItemMusics);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //SAVE TO SHARED PREFERENCES.
            posSongChoice = getAdapterPosition();
            SharedPreferences sharedPreferences = context.getSharedPreferences("timeCountDown", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("posSongChoice", posSongChoice);
            editor.commit();
            //CHANGE VIEW CLICKED AND NOT
            for (int i = 0; i < viewList.size(); i++) {
                if (i == getAdapterPosition()) {
                    viewList.get(i).setImageTintList(ColorStateList.valueOf(Color.parseColor("#F03A54EC")));
                } else {
                    viewList.get(i).setImageTintList(ColorStateList.valueOf(Color.parseColor("#A5A4A4")));
                }
            }
            //START RUN TEST MUSICS FOR USERS LISTEN
            if (getAdapterPosition() != 0 && !hasStarted[getAdapterPosition()]) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                Arrays.fill(hasStarted, false);
                hasStarted[getAdapterPosition()] = true;
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                mediaPlayer = MediaPlayer.create(context, songs[getAdapterPosition()]);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                countDownTimer = new CountDownTimer(7000, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        if (!isRunning) {
                            mediaPlayer.reset();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    }
                }.start();
            } else {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        }
    }
}
