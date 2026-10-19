package com.example.scheduleapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.scheduleapp.R;
import java.util.List;
import java.util.zip.Inflater;

import com.example.scheduleapp.Classes.*;

public class AdapterRecyclerViewSchedule extends RecyclerView.Adapter<AdapterRecyclerViewSchedule.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Day> days;
    private final Context context;
    private final SharedPreferences spLessons;

    public AdapterRecyclerViewSchedule(Context context, List<Day> days, SharedPreferences spLessons) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.days = days;
        this.spLessons = spLessons;
    }

    @Override
    public AdapterRecyclerViewSchedule.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.include_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterRecyclerViewSchedule.ViewHolder holder, int position) {
        Day day = days.get(position);
        if (day.weekDay.isEmpty()) holder.weekDay.setVisibility(View.GONE);
        else holder.weekDay.setText(day.weekDay);
        if (day.date.isEmpty()) holder.date.setVisibility(View.GONE);
        else holder.date.setText(day.date);
        if (day.even.isEmpty()) holder.even.setVisibility(View.GONE);
        else holder.even.setText(day.even);
        holder.lessons_container.setAdapter(new AdapterRecyclerViewLesson(context,day.lessons,spLessons));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView weekDay,date,even;
        RecyclerView lessons_container;
        ViewHolder(View view){
            super(view);
            weekDay = view.findViewById(R.id.weekday);
            date = view.findViewById(R.id.date);
            even = view.findViewById(R.id.even);
            lessons_container = view.findViewById(R.id.recyclerViewLessons);
        }
    }
}
