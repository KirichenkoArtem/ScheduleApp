package com.example.scheduleapp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;

public class BasicUtils {
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
}
