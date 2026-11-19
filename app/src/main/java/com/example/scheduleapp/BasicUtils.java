package com.example.scheduleapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.scheduleapp.Classes.Lesson;

public class BasicUtils {

    /// Пример выхода: `Понедельник`
    public String getSystemWeekday(){
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        int dayValue = dayOfWeek.getValue();
        String[] weekdays = new String[]{"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};
        return weekdays[dayValue-1];
    }

    /// Текущее время между переданными или нет
    /// Пример входа: "09:30 — 10:10"; выход: true/false
    public boolean isMiddleTime(String time){
        if (time == null) return false;
        String[] times = time.split(" — ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (times.length == 2){
            LocalTime first = LocalTime.parse(times[0], formatter);
            LocalTime second = LocalTime.parse(times[1], formatter);
            LocalTime now = LocalTime.now();
            return now.isAfter(first) && now.isBefore(second);
        }

        return false;
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
        (lessonView).setOnClickListener(ignored-> mainActivity.basicUtils.openChangeNote(
                mainActivity,
                noteName,
                lesson.subject,
                lesson.teacher+" * Заметка",
                sp.getString(noteName,""),
                updateRender
        ));
        parent.addView(lessonView);
    }

    /// alertInclude
    public void animateAlertInclude(View view, boolean isVisible){
        ConstraintLayout alertBody;
        ImageView alertBackground;
        EditText editText;
        if (view.findViewById(R.id.alert_change_body) == null){
            if(view.findViewById(R.id.alert_add_body) == null){
                return;
            }else{
                alertBody = view.findViewById(R.id.alert_add_body);
                alertBackground = view.findViewById(R.id.alert_add_background);
                editText = view.findViewById(R.id.alert_add_valueEditText);
            }
        }else{
            alertBackground = view.findViewById(R.id.alert_change_background);
            alertBody = view.findViewById(R.id.alert_change_body);
            editText = view.findViewById(R.id.alert_change_editText);
        }
        if(isVisible){
            view.setVisibility(View.VISIBLE);
            alertBody.animate().setDuration(0)
                    .translationY(2500)
                    .withEndAction(()-> alertBody.animate().setDuration(500)
                            .translationY(0)
                            .start())
                    .start();
            alertBackground.animate().setDuration(0)
                    .translationY(-2500)
                    .withEndAction(()-> alertBackground.animate().setDuration(500)
                            .translationY(0)
                            .start())
                    .start();
        }else{
            // Закрываем
            hideKeyboard(editText);
            alertBackground.animate().setDuration(500)
                    .translationY(-2500)
                    .start();
            alertBody.animate().setDuration(500)
                    .translationY(2500)
                    .withEndAction(()-> view.setVisibility(View.GONE))
                    .start();
        }
    }
    public void hideKeyboard(EditText editText){
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(
                    editText
                .getWindowToken(), 0);
    }


    /// Метод для быстрого открытия и работы с окном заметки
    public void openChangeNote(MainActivity mainActivity_, String fullNoteName, String noteName, String noteExtra, String noteValue, Runnable onCloseRunnable){
        LinearLayout alertInclude = mainActivity_.findViewById(R.id.alert_change_note);
        View alertBackground = alertInclude.findViewById(R.id.alert_change_background);
        TextView mainText = alertInclude.findViewById(R.id.alert_change_mainText);
        TextView extraText = alertInclude.findViewById(R.id.alert_change_extraText);
        EditText editText = alertInclude.findViewById(R.id.alert_change_editText);
        View deleteButton = alertInclude.findViewById(R.id.alert_change_deleteButton);
        View saveButton = alertInclude.findViewById(R.id.alert_change_saveButton);

        mainText.setText(noteName);
        extraText.setText(noteExtra);
        editText.setText(noteValue);

        alertBackground.setOnClickListener(view -> {
            mainActivity_.basicUtils.animateAlertInclude(alertInclude,false);
        });
        deleteButton.setOnClickListener(view -> {
            mainActivity_.spNotes.edit().remove(fullNoteName).apply();
            mainActivity_.basicUtils.animateAlertInclude(alertInclude,false);
            onCloseRunnable.run();
        });
        saveButton.setOnClickListener(view -> {
            if (!String.valueOf(editText.getText()).isEmpty()) {
                mainActivity_.spNotes.edit().putString(fullNoteName, "" + editText.getText()).apply();
            }
            mainActivity_.basicUtils.animateAlertInclude(alertInclude,false);
            onCloseRunnable.run();
        });
        mainActivity_.basicUtils.animateAlertInclude(alertInclude,true);
    }


    public void openAddNote(MainActivity mainActivity_, Runnable onCloseRunnable){
        LinearLayout alertInclude = mainActivity_.findViewById(R.id.alert_add_note);
        View alertBackground = alertInclude.findViewById(R.id.alert_add_background);
        EditText mainTextEditText = alertInclude.findViewById(R.id.alert_add_mainEditText);
        EditText valueEditText = alertInclude.findViewById(R.id.alert_add_valueEditText);
        TextView exitTextView = alertInclude.findViewById(R.id.alert_add_exitButton);
        TextView saveTextView = alertInclude.findViewById(R.id.alert_add_saveButton);

        mainTextEditText.setText("");
        valueEditText.setText("");

        Runnable justClose = () -> {
            mainActivity_.basicUtils.animateAlertInclude(alertInclude,false);
        };

        alertBackground.setOnClickListener(view -> justClose.run());
        exitTextView.setOnClickListener(view -> justClose.run());
        saveTextView.setOnClickListener(view -> {
            if (!String.valueOf(mainTextEditText.getText()).isEmpty() && !String.valueOf(valueEditText.getText()).isEmpty()) {
                // (sp.getString("current_group","")+"_"+noteName+"_"+noteExtra)
                mainActivity_.spNotes.edit().putString(
                        (mainActivity_.sp.getString("current_group","")+"_"+mainTextEditText.getText()+"_ "),
                        ""+valueEditText.getText()).apply();
            }
            mainActivity_.basicUtils.animateAlertInclude(alertInclude,false);
            onCloseRunnable.run();
        });

        mainActivity_.basicUtils.animateAlertInclude(alertInclude,true);
    }
}
