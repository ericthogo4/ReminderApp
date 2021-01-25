package com.tasks.medicine.study.reminderapp;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>{

    private Context reminderAdapterContext;
    private List<Reminder> reminderList;
    private List<Integer> selectedPositions = new ArrayList<>();

    public ReminderAdapter(Context reminderAdapterContext, List<Reminder> reminderList) {
        this.reminderAdapterContext = reminderAdapterContext;
        this.reminderList = reminderList;
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_list_row, parent, false);

        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {

        holder.rTitleTextView.setText(reminderList.get(position).getReminderTitle());
        holder.rTOFTextView.setText(reminderList.get(position).getReminderTOF());
        holder.rDOFTextView.setText(reminderList.get(position).getReminderDOF());

        if (selectedPositions.contains(position)){
            holder.reminderListRowFrameLayout.setForeground(new ColorDrawable(ContextCompat.getColor(reminderAdapterContext,R.color.recyclerViewItemSelectionColor)));
        }
        else {
            holder.reminderListRowFrameLayout.setForeground(new ColorDrawable(ContextCompat.getColor(reminderAdapterContext,android.R.color.transparent)));
        }

    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    protected class ReminderViewHolder extends RecyclerView.ViewHolder {
        protected TextView rTitleTextView;
        protected TextView rTOFTextView;
        protected TextView rDOFTextView;
        protected FrameLayout reminderListRowFrameLayout;

        public ReminderViewHolder(View view) {
            super(view);
            rTitleTextView = (TextView) view.findViewById(R.id.reminder_title_tvw);
            rTOFTextView = (TextView) view.findViewById(R.id.reminder_tof_tvw);
            rDOFTextView = view.findViewById(R.id.reminder_dof_tvw);
            reminderListRowFrameLayout = view.findViewById(R.id.reminder_list_row_frame_layout);
        }
    }


    public void setSelectedPositions(int previousPosition, List<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;

        if(previousPosition!=-1){
            notifyItemChanged(previousPosition);
        }

        if(this.selectedPositions.size()>0){
            notifyItemChanged(this.selectedPositions.get(0));
        }

    }


    public Reminder getItem(int position){
        return reminderList.get(position);
    }


}