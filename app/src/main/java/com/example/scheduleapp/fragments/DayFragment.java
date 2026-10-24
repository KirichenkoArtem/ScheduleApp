package com.example.scheduleapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.scheduleapp.BasicUtils;
import com.example.scheduleapp.Classes.Day;
import com.example.scheduleapp.Classes.Lesson;
import com.example.scheduleapp.MainActivity;
import com.example.scheduleapp.R;
import com.example.scheduleapp.ScheduleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DayFragment extends Fragment {

    MainActivity mainActivity;
    ScheduleUtils scheduleUtils;
    BasicUtils basicUtils;
    LinearLayout linearLayout;
    ImageView headerUpdateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = ((MainActivity) requireActivity());
        scheduleUtils = mainActivity.scheduleUtils;
        basicUtils = mainActivity.basicUtils;
        linearLayout = view.findViewById(R.id.days_container);
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
        linearLayout.removeAllViews();
        AtomicReference<List<Day>> days = new AtomicReference<>(new ArrayList<>());
        basicUtils.simpleNewAndUIThreads(
                mainActivity,
                ()->{
                    days.set(scheduleUtils.getScheduleInClasses(mainActivity.sp));
                },
                ()->{
                    for (Day day : days.get()){
                        if(day.weekDay.equals(basicUtils.getSystemWeekday())){

                            LinearLayout includeDay = (LinearLayout) View.inflate(mainActivity,R.layout.include_day,linearLayout);
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

                            for(Lesson lesson : day.lessons){
                                basicUtils.createLessonView(mainActivity,lesson,lessonsContainer,this::render);
                            }
                        }
                    }
                    mainActivity.setVisibleOfLoadingIndicator(false);
                }
        );
    }
}