package com.example.scheduleapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scheduleapp.BasicUtils;
import com.example.scheduleapp.Classes.Day;
import com.example.scheduleapp.MainActivity;
import com.example.scheduleapp.R;
import com.example.scheduleapp.ScheduleUtils;
import com.example.scheduleapp.adapters.AdapterRecyclerViewSchedule;

import java.util.List;

public class WeekFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdapterRecyclerViewSchedule adapter;
    private ScheduleUtils scheduleUtils;
    private BasicUtils basicUtils;
    private MainActivity mainActivity;
    private ImageView headerGroupUpdate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = ((MainActivity) requireActivity());
        scheduleUtils = mainActivity.scheduleUtils;
        basicUtils = mainActivity.basicUtils;
        recyclerView = view.findViewById(R.id.recyclerView);
        headerGroupUpdate = mainActivity.headerGroupUpdate;
        headerGroupUpdate.setOnClickListener(ignored -> {
            render();
            headerGroupUpdate.setRotation(0);
            headerGroupUpdate.animate().setDuration(350).rotationBy(360).start();
        });
        render();
    }


    /// Сильно оптимизированный рендер благодаря recyclerView
    private void render(){
        mainActivity.setVisibleOfLoadingIndicator(true);
        basicUtils.simpleNewAndUIThreads(
                mainActivity,
                ()->{
                    List<Day> days = scheduleUtils.getScheduleInClasses(mainActivity.sp);
                    adapter = new AdapterRecyclerViewSchedule(mainActivity, days, mainActivity.spLessons);
                },
                ()->{
                    mainActivity.setVisibleOfLoadingIndicator(false);
                    recyclerView.setAdapter(adapter);
                }
        );
    }
}