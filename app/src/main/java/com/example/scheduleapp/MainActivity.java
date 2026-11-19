package com.example.scheduleapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.scheduleapp.fragments.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout include_header;
    private View loading_indicator;
    private BottomNavigationView bottom_navigator;
    private TextView headerGroupName, headerExtraText;
    public ImageView headerGroupSetButton, headerUpdateButton, headerAddButton;
    public SharedPreferences sp, spNotes;
    public ScheduleUtils scheduleUtils = new ScheduleUtils();
    public BasicUtils basicUtils = new BasicUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Устанавливаем всё что надо
        scheduleUtils.mainActivity = this;
        include_header = findViewById(R.id.include_header);
        bottom_navigator = findViewById(R.id.bottom_navigator);
        sp = getSharedPreferences("hgfdzxgguhijoihugyfdt",MODE_PRIVATE);
        spNotes = getSharedPreferences("fcgvjhbkljgjvgchfgkjb",MODE_PRIVATE);
        loading_indicator = findViewById(R.id.loading_indicator);
        headerGroupSetButton = include_header.findViewById(R.id.headerGroupSetButton);
        headerUpdateButton = include_header.findViewById(R.id.headerUpdateButton);
        headerGroupName = include_header.findViewById(R.id.headerGroupName);
        headerAddButton = include_header.findViewById(R.id.headerAddButton);
        headerExtraText = include_header.findViewById(R.id.headerExtraText);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.alert_change_note), (v, insets) -> {
            Insets ime        = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int extraBottom = Math.max(0, ime.bottom - systemBars.bottom);
            v.setPadding(0, 0, 0, extraBottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.alert_add_note), (v, insets) -> {
            Insets ime        = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int extraBottom = Math.max(0, ime.bottom - systemBars.bottom);
            v.setPadding(0, 0, 0, extraBottom);
            return insets;
        });

        // Работаем с установленным
        bottom_navigator.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.menu_day){
                goTo("fragment_day");
                return true;
            } else if(id == R.id.menu_week){
                goTo("fragment_week");
                return true;
            } else if(id == R.id.menu_notes){
                goTo("fragment_notes");
                return true;
            }
            return false;
        });
        headerGroupSetButton.setOnClickListener(view -> {
            goTo("select_group");
        });
        if(sp.getString("current_group", "").isEmpty()) {
            goTo("select_group");
        } else {
            new Thread(()->scheduleUtils.getScheduleInClasses(sp)).start();
            goTo("fragment_day");
        }
    }

    /// Доступные фрагменты:
    /// select_group, fragment_day, fragment_week, fragment_notes
    public void goTo(String goToName){
        Fragment fragment = null;
        headerGroupName.setText(sp.getString("current_group", "Ошибка"));
        headerAddButton.setVisibility(View.GONE);
        headerUpdateButton.setVisibility(View.GONE);
        setVisibleOfHeaders(true);

        switch (goToName){
            case "select_group":
                fragment = new GroupFragment();
                setVisibleOfHeaders(false);
                break;
            case "fragment_day":
                fragment = new DayFragment();
                headerExtraText.setText("Расписание на день");
                headerUpdateButton.setVisibility(View.VISIBLE);
                break;
            case "fragment_week":
                fragment = new WeekFragment();
                headerExtraText.setText("Расписание на неделю");
                headerUpdateButton.setVisibility(View.VISIBLE);
                break;
            case "fragment_notes":
                fragment = new NotesFragment();
                headerExtraText.setText("Заметки на уроки");
                headerAddButton.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment).commit();
        }
    }

    /// Скрывает или показывает include_header|bottom_navigator
    private void setVisibleOfHeaders(boolean isVisible){
        if (isVisible) {
            include_header.setVisibility(View.VISIBLE);
            bottom_navigator.setVisibility(View.VISIBLE);
        } else {
            include_header.setVisibility(View.GONE);
            bottom_navigator.setVisibility(View.GONE);
        }
    }

    /// Включаем или выключаем загрузку
    public void setVisibleOfLoadingIndicator(boolean isVisible){
        loading_indicator.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}