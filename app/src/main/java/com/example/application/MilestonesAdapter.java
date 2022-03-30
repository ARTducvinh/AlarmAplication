package com.example.application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MilestonesAdapter extends RecyclerView.Adapter<MilestonesAdapter.ViewHolder> {

    private List<MilestonesItem> milestonesItemList;

    public MilestonesAdapter(List<MilestonesItem> list) {
        this.milestonesItemList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewMilestones = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_item_milestones, parent, false);
        return new ViewHolder(viewMilestones);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MilestonesItem milestonesItem = milestonesItemList.get(position);
        holder.textViewOrderItem.setText(String.valueOf(milestonesItem.getOrder()));
        holder.textViewTimePlus.setText(milestonesItem.getTimePlus());
        holder.textViewMilestones.setText(milestonesItem.getTimeMilestone());
    }

    @Override
    public int getItemCount() {
        return milestonesItemList.size();
    }

    public void addMilestonesItem(MilestonesItem item) {
        int size = milestonesItemList.size();
        if (size == 0) {
            milestonesItemList.add(item);
            notifyDataSetChanged();
        } else {
            milestonesItemList.add(size, item);
            notifyItemInserted(size);
        }
    }

    public void clearAllItems() {
        int size = milestonesItemList.size();
        milestonesItemList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public List<MilestonesItem> getListItemMilestones() {
        return this.milestonesItemList;
    }

    public void updateRecyclerView(List<MilestonesItem> tempList) {
        milestonesItemList = tempList;
        notifyDataSetChanged();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewIconMilestones;
        private final TextView textViewOrderItem;
        private final TextView textViewTimePlus;
        private final TextView textViewMilestones;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIconMilestones = itemView.findViewById(R.id.iconMilestones);
            textViewOrderItem = itemView.findViewById(R.id.textViewOrder);
            textViewTimePlus = itemView.findViewById(R.id.textViewTimePlusToMilestones);
            textViewMilestones = itemView.findViewById(R.id.textViewTimeMilestones);
        }
    }
}
