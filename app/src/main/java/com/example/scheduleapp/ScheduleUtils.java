package com.example.scheduleapp;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.poi.ss.usermodel.CellType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.scheduleapp.Classes.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ScheduleUtils {

    String baseUrl = "https://www.магпк.рф";
    String groupsUrl = baseUrl + "/students/schedule/group_list/group_rest.php";
    String teachersUrl = baseUrl + "/teachers/teacher_rest.php";
    String scheduleUrl = baseUrl + "/students/schedule/ajax_schedule/open_json.php";
    String teacherScheduleUrl = baseUrl + "/students/schedule/teachers_schedule/open_json.php";

    public JSONArray getSchedule(String groupName) {
        try {
            JSONArray weeks = new JSONArray();
            String response = post(scheduleUrl,"type=2&group="+groupName);
            if (response == null) return null;
            JSONObject JOResponse = new JSONObject(response);
            JSONObject JODays = JOResponse.getJSONObject("rs");
            JSONArray JADays = new JSONArray();
            Iterator<String> daysKeys = JODays.keys();
            String daysKey;
            while(daysKeys.hasNext()){
                daysKey = daysKeys.next();
                JSONObject _JODay = JODays.getJSONObject(daysKey);
                JSONObject JODay = new JSONObject();
                JODay.put("weekDay", daysKey.charAt(0) + daysKey.substring(1).toLowerCase());
                JODay.put("date","");

                JSONArray JALessons = new JSONArray();
                Iterator<String> dayKeys = _JODay.keys(); String dayKey;
                int id = 0;
                while(dayKeys.hasNext()){
                    dayKey = dayKeys.next();
                    id++;
//                    Log.e("LOG",dayKey.replace("\n"," — "));
                    JSONObject day = _JODay.getJSONObject(dayKey);
                    dayKey = dayKey.replace("\n"," — ");
                    JSONObject _firstSubgroupLesson = day.get("1") instanceof JSONObject ? day.getJSONObject("1") : new JSONObject();
                    JSONObject _secondSubgroupLesson = day.get("2") instanceof JSONObject ? day.getJSONObject("2") : new JSONObject();

                    String[] firstSplit = _firstSubgroupLesson.has("lesson") ? _firstSubgroupLesson.getString("lesson").split("\n") : null;
                    String firstClass = _firstSubgroupLesson.has("class") ?  _firstSubgroupLesson.getString("class") : null;
                    String[] secondSplit = _secondSubgroupLesson.has("lesson") ? _secondSubgroupLesson.getString("lesson").split("\n") : null;
                    String secondClass = _secondSubgroupLesson.has("class") ? _secondSubgroupLesson.getString("class") : null;

                    if(firstSplit != null && firstClass != null && firstSplit.length>1){
                        JSONObject firstSubgroupLesson = new JSONObject();
                        firstSubgroupLesson.put("id",id);
                        firstSubgroupLesson.put("subgroup",1);
                        firstSubgroupLesson.put("subject",firstSplit[firstSplit.length-2]);
                        firstSubgroupLesson.put("time",dayKey);
                        firstSubgroupLesson.put("teacher",firstSplit[firstSplit.length-1]);
                        firstSubgroupLesson.put("auditory",firstClass);
                        JALessons.put(firstSubgroupLesson);
                    }
                    if(secondSplit != null && secondClass != null && secondSplit.length>1){
                        JSONObject secondSubgroupLesson = new JSONObject();
                        secondSubgroupLesson.put("id",id);
                        secondSubgroupLesson.put("subgroup",2);
                        secondSubgroupLesson.put("subject",secondSplit[secondSplit.length-2]);
                        secondSubgroupLesson.put("time",dayKey);
                        secondSubgroupLesson.put("teacher",secondSplit[secondSplit.length-1]);
                        secondSubgroupLesson.put("auditory",secondClass);
                        if(firstSplit == null || !(firstSplit[firstSplit.length - 2].equals(secondSplit[firstSplit.length - 2]) && Objects.equals(firstClass, secondClass) && firstSplit[firstSplit.length - 1].equals(secondSplit[firstSplit.length - 1]))){
                            JALessons.put(secondSubgroupLesson);
                        }
                    }

                }
                JODay.put("lessons",JALessons);
                JADays.put(JODay);
            }
            JSONObject week = new JSONObject();
            week.put("weekIsEven","");
            week.put("days",JADays);
            weeks.put(week);
            return weeks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getGroups() {
        try {
            String response = get(groupsUrl);
            JSONArray JAGroups = new JSONArray(response);
            String[] returnArray = new String[JAGroups.length()];
            for (int i = 0; i<JAGroups.length(); i++){
                returnArray[i] = JAGroups.getJSONObject(i).getString("group_name");
            }
            return returnArray;
        } catch (Exception e) {
            Log.e("LOG_getGroups",""+e);
        }
        return new String[]{};
    }

//    /// Записывает расписание в `scheduleWeeks`, вызывать только если есть `currentGroup`
//    public void getScheduleToSP(SharedPreferences sp){
//        SharedPreferences.Editor editor = sp.edit();
//        String currentGroup = sp.getString("current_group","");
//        Log.e("LOG",currentGroup);
//        editor.putString("scheduleWeeks",getSchedule(currentGroup).toString());
//        editor.apply();
//    }

    /// Записывает расписание в Schedule из `scheduleWeeks`, вызывать только если есть `currentGroup,scheduleWeeks`
    public List<Day> getScheduleInClasses(SharedPreferences sp){
        String currentGroup = sp.getString("current_group","");
        if(currentGroup.isEmpty()){
            Log.i("LOG","current_group = "+currentGroup);
            return null;
        }
        try{
            List<Day> return_days = new ArrayList<>();
            JSONArray JASchedule = getSchedule(currentGroup);
//            List<Week> weeks = new ArrayList<>();
            for(int weekI = 0; weekI<JASchedule.length(); weekI++){
                JSONObject JOWeek = JASchedule.getJSONObject(weekI);
                String weekIsEven = JOWeek.get("weekIsEven") instanceof String ? ""
                        : JOWeek.getBoolean("weekIsEven") ? "Чётная" : "Нечётная";
                JSONArray JADays = JOWeek.getJSONArray("days");
//                List<Day> days = new ArrayList<>();
                for (int dayI = 0; dayI<JADays.length(); dayI++){
                    JSONObject JODay = JADays.getJSONObject(dayI);
                    String weekDay = JODay.getString("weekDay");
                    String date = JODay.getString("date");
                    JSONArray JALessons = JODay.getJSONArray("lessons");
                    List<Lesson> lessons = new ArrayList<>();
                    for (int lessonI = 0; lessonI<JALessons.length(); lessonI++){
                        JSONObject JOLesson = JALessons.getJSONObject(lessonI);
                        int id = JOLesson.getInt("id");
                        int subgroup = JOLesson.getInt("subgroup");
                        String subject = JOLesson.getString("subject");
                        String time = JOLesson.getString("time");
                        String teacher = JOLesson.getString("teacher");
                        String auditory = JOLesson.getString("auditory");
                        lessons.add(new Lesson(id,subgroup,subject,time,teacher,auditory,""));
                    }
                    Day day = new Day(weekDay,date,weekIsEven,lessons);
//                    days.add(day);
                    return_days.add(day);
                }
//                Week week = new Week(days);
//                weeks.add(week);
            }
//            return new Schedule(currentGroup, weeks);
            return return_days;
        }catch (Exception e){
            Log.e("LOG", "parseSPToClasses "+e);
            return null;
        }
    }

    private String get(String url){
        String html = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(15000);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String ln;
                while ((ln = br.readLine()) != null) sb.append(ln);
                html = sb.toString();
            } catch (Exception e){
                Log.e("LOG","HTML bufferedReader error:\n"+e.toString());
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            Log.e("LOG","HTML connection error:\n"+e.toString());
        }
        return html;
    }
    private String post(String url, String body){
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] output = body.getBytes("UTF-8");
                outputStream.write(output);
            }

            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode >= 200 && responseCode < 300) ?
                    connection.getInputStream() : connection.getErrorStream();

            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"))) {
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("LOG","HTML connection error:\n"+e.toString());
        }
        return null;
    }
}
