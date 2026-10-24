package com.example.scheduleapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import com.example.scheduleapp.Classes.Lesson;

public class BasicUtils {
    private float dragOffsetX;
    private float dragOffsetY;
    public void simpleNewAndUIThreads(MainActivity mainActivity, Runnable thread, Runnable runnableUI){
        new Thread(()->{
            thread.run();
            mainActivity.runOnUiThread(runnableUI);
        }).start();
    }

    public String getSystemWeekday(){
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        int dayValue = dayOfWeek.getValue();
        String[] weekdays = new String[]{"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};
        return weekdays[dayValue-1];
    }
    @SuppressLint("ClickableViewAccessibility")
    public void createLessonView(MainActivity mainActivity, Lesson lesson, ViewGroup parent, Runnable updateRender){
        String currentGroup = mainActivity.sp.getString("current_group","");
        if (currentGroup.isEmpty()) return;
        String noteName = currentGroup+"_"+lesson.subject+"_"+lesson.teacher;
        SharedPreferences sp = mainActivity.spNotes;
        LinearLayout lessonView = (LinearLayout) mainActivity.getLayoutInflater().inflate(R.layout.include_lesson, null);
        ((TextView) lessonView.findViewById(R.id.lesson_count)).setText(""+lesson.id);
        ((TextView) lessonView.findViewById(R.id.lesson_subgroup)).setText(lesson.subgroup==1? "" : "."+lesson.subgroup);
        ((TextView) lessonView.findViewById(R.id.lesson_name)).setText(lesson.subject);
        ((TextView) lessonView.findViewById(R.id.lesson_teacher)).setText(lesson.teacher);
        ((TextView) lessonView.findViewById(R.id.lesson_time)).setText(lesson.time);
        ((TextView) lessonView.findViewById(R.id.lesson_room)).setText(lesson.auditory);
        ImageView noteLine = lessonView.findViewById(R.id.line_note);
        TextView noteText = lessonView.findViewById(R.id.note_text);
        TextView noteStar = lessonView.findViewById(R.id.note_star);
        noteText.setText(sp.getString(noteName,""));
        if (!String.valueOf(noteText.getText()).isEmpty()){
            noteLine.setVisibility(View.VISIBLE);
            noteText.setVisibility(View.VISIBLE);
            noteStar.setVisibility(View.VISIBLE);
        }
        (lessonView).setOnClickListener(ignored->{
            View alertInclude = mainActivity.findViewById(R.id.alert_include);
            View alertBackground = alertInclude.findViewById(R.id.alert_background);
            TextView mainText = alertInclude.findViewById(R.id.alert_mainText);
            TextView extraText = alertInclude.findViewById(R.id.alert_extraText);
            EditText editText = alertInclude.findViewById(R.id.alert_editText);
            View deleteButton = alertInclude.findViewById(R.id.alert_deleteButton);
            View saveButton = alertInclude.findViewById(R.id.alert_saveButton);

            mainText.setText(lesson.subject);
            extraText.setText(lesson.teacher+" * Заметка");
            editText.setText(sp.getString(noteName,""));

            alertBackground.setOnClickListener(view -> {
                alertInclude.setVisibility(View.GONE);
            });
            deleteButton.setOnClickListener(view -> {
                sp.edit().remove(noteName).apply();
                alertInclude.setVisibility(View.GONE);
                updateRender.run();
            });
            saveButton.setOnClickListener(view -> {
                if (!String.valueOf(editText.getText()).isEmpty()) {
                    Log.d("LOG", "IF: " + String.valueOf(editText.getText()).equals("") + "\nedittext: " + editText.getText());
                    sp.edit().putString(noteName, "" + editText.getText()).apply();
                }
                alertInclude.setVisibility(View.GONE);
                updateRender.run();
            });

            alertInclude.setVisibility(View.VISIBLE);
        });
        parent.addView(lessonView);
    }
}
