package com.example.scheduleapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.scheduleapp.BasicUtils;
import com.example.scheduleapp.Classes;
import com.example.scheduleapp.Classes.Day;
import com.example.scheduleapp.MainActivity;
import com.example.scheduleapp.R;
import com.example.scheduleapp.ScheduleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Inflater;

public class WeekFragment extends Fragment {
    private LinearLayout daysContainer;
//    private AdapterRecyclerViewSchedule adapter;
    private ScheduleUtils scheduleUtils;
    private BasicUtils basicUtils;
    private MainActivity mainActivity;
    private ImageView headerUpdateButton;
    private LayoutInflater inflater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = ((MainActivity) requireActivity());
        scheduleUtils = mainActivity.scheduleUtils;
        basicUtils = mainActivity.basicUtils;
        daysContainer = view.findViewById(R.id.days_container);
        headerUpdateButton = mainActivity.headerUpdateButton;
        headerUpdateButton.setOnClickListener(ignored -> {
            render();
            headerUpdateButton.setRotation(0);
            headerUpdateButton.animate().setDuration(350).rotationBy(360).start();
        });
        render();
    }


    /// Сильно оптимизированный рендер благодаря recyclerView
    private void render(){
        mainActivity.setVisibleOfLoadingIndicator(true);
        daysContainer.removeAllViews();
        AtomicReference<List<Day>> days = new AtomicReference<>(new ArrayList<>());

        new Thread(()-> {
            days.set(scheduleUtils.getScheduleInClasses(mainActivity.sp));
            mainActivity.runOnUiThread(()->{
                for (Day day : days.get()){
                    LinearLayout includeDay = (LinearLayout) inflater.inflate(
                            R.layout.include_day,
                            daysContainer,
                            false);
                    LinearLayout lessonsContainer = includeDay.findViewById(R.id.lesson_container);
                    TextView weekDay = includeDay.findViewById(R.id.weekday);
                    TextView date = includeDay.findViewById(R.id.date);
                    TextView even = includeDay.findViewById(R.id.even);
                    if (day.weekDay.isEmpty()) weekDay.setVisibility(View.GONE);
                    else weekDay.setText(day.weekDay);
                    if (day.date.isEmpty()) date.setVisibility(View.GONE);
                    else date.setText(day.date);
                    if (day.even.isEmpty()) even.setVisibility(View.GONE);
                    else even.setText(day.even);

                    for(Classes.Lesson lesson : day.lessons){
                        basicUtils.createLessonView(mainActivity,lesson,lessonsContainer,this::render);
                    }
                    daysContainer.addView(includeDay);
                }
                mainActivity.setVisibleOfLoadingIndicator(false);
            });
        });
    }
}