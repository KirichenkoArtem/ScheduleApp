package com.example.scheduleapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.scheduleapp.MainActivity;
import com.example.scheduleapp.R;
import com.example.scheduleapp.ScheduleUtils;

public class GroupFragment extends Fragment {

    MainActivity mainActivity;
    TextView select_button;
    AutoCompleteTextView input_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) requireActivity();
        select_button = view.findViewById(R.id.select_button);
        input_view = view.findViewById(R.id.input_view);

        select_button.setVisibility(View.GONE);
        select_button.setOnClickListener(ignored -> {
            mainActivity.goTo("fragment_day");
        });
        updateInputAdapter();
        input_view.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if(!input_view.isPerformingCompletion())
                    select_button.setVisibility(View.GONE);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });
        input_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String groupName = adapterView.getItemAtPosition(i).toString();
                mainActivity.sp.edit().putString("current_group",groupName).apply();
                select_button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateInputAdapter(){
        mainActivity.setVisibleOfLoadingIndicator(true);
        if(input_view != null){
            new Thread(() -> {
                String[] array = mainActivity.scheduleUtils.getGroups();
                mainActivity.runOnUiThread(() -> {
                    input_view.setAdapter(new ArrayAdapter<>(
                            mainActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            array
                    ));
                    mainActivity.setVisibleOfLoadingIndicator(false);
                });
            }).start();
        }
    }
}