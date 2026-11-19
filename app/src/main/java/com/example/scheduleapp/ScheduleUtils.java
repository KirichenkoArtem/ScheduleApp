package com.example.scheduleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import org.apache.poi.ss.usermodel.CellType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.scheduleapp.Classes.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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

    MainActivity mainActivity;
    String baseUrl = "https://www.магпк.рф";
    String groupsUrl = baseUrl + "/students/schedule/group_list/group_rest.php";
//    String teachersUrl = baseUrl + "/teachers/teacher_rest.php";
    String scheduleUrl = baseUrl + "/students/schedule/ajax_schedule/open_json.php";
//    String teacherScheduleUrl = baseUrl + "/students/schedule/teachers_schedule/open_json.php";

    private JSONArray getSchedule(String groupName) {
        JSONArray weeks = new JSONArray();
        try {
            String response = post(scheduleUrl,"type=2&group="+groupName);
            if (response == null || response.equals("timeout") || response.equals("no-network")) {
                return null;
            }
            JSONArray JADays = new JSONArray();
            JSONObject JOResponse = new JSONObject(response);
            JSONObject JODays = JOResponse.getJSONObject("rs");
            Iterator<String> daysKeys = JODays.keys();
            String daysKey;
            while(daysKeys.hasNext()){
                daysKey = daysKeys.next();
                JSONObject _JODay = JODays.getJSONObject(daysKey);
                JSONObject JODay = new JSONObject();
                JSONArray JALessons = new JSONArray();
                JODay.put("weekDay", daysKey.charAt(0) + daysKey.substring(1).toLowerCase());
                JODay.put("date","");
                Iterator<String> dayKeys = _JODay.keys();
                String dayKey;
                int id = 0;
                while(dayKeys.hasNext()){
                    id++;
                    dayKey = dayKeys.next();
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
        } catch (Exception ignored) {}
        return null;
    }


    /// Выводит список групп (в массиве для адаптера)
    public String[] getGroups() {
        try {
            String response = get(groupsUrl);
            if (response == null || response.equals("timeout") || response.equals("no-network")) {
                return new String[]{};
            }
            JSONArray JAGroups = new JSONArray(response);
            String[] returnArray = new String[JAGroups.length()];
            for (int i = 0; i<JAGroups.length(); i++){
                returnArray[i] = JAGroups.getJSONObject(i).getString("group_name");
            }
            return returnArray;
        } catch (Exception ignored) {}
        return new String[]{};
    }

    public List<Day> getScheduleInClasses(SharedPreferences sp){
        String currentGroup = sp.getString("current_group","");
        if(currentGroup.isEmpty()) return null;
        List<Day> returnDays = new ArrayList<>();
        try{
            JSONArray JASchedule = getSchedule(currentGroup);
            if (JASchedule == null){
                JASchedule = new JSONArray(sp.getString(currentGroup+"_schedule", String.valueOf(new JSONArray())));
            } else {
                sp.edit().putString(currentGroup+"_schedule", JASchedule.toString()).apply();
            }
            for(int weekI = 0; weekI<JASchedule.length(); weekI++){
                JSONObject JOWeek = JASchedule.getJSONObject(weekI);
                String weekIsEven = JOWeek.get("weekIsEven") instanceof String ? "" : JOWeek.getBoolean("weekIsEven") ? "Чётная" : "Нечётная";
                JSONArray JADays = JOWeek.getJSONArray("days");
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
                        if (subject.trim().equals("Обществознание")
                                && currentGroup.trim().equals("ПД-12")
                                && weekDay.trim().equals("Пятница")
                        ){
                            subject = "Индивидуальный проект";
                        }
                        lessons.add(new Lesson(id,subgroup,subject,time,teacher,auditory,""));
                    }
                    Day day = new Day(weekDay,date,weekIsEven,lessons);
                    returnDays.add(day);
                }
            }
        } catch (Exception ignored){}
        return returnDays;
    }

    /// Проверка на подключение к интернету
    private boolean isHaveNetwork(){
        if (mainActivity == null){
            return false;
        }
        ConnectivityManager connManager =
                (ConnectivityManager) mainActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities =
                connManager.getNetworkCapabilities(connManager.getActiveNetwork());
        if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))){
            return true;
        }
        return false;
    }

    private String get(String url){
        if(!isHaveNetwork()){
            return "no-network";
        }
        String html = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setReadTimeout(2500);
            connection.setConnectTimeout(2500);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String ln;
                while ((ln = br.readLine()) != null) sb.append(ln);
                html = sb.toString();
            } catch (Exception e) {
                Log.e("LOG", "HTML bufferedReader error:\n" + e);
            } finally {
                connection.disconnect();
            }
        } catch (SocketTimeoutException | UnknownHostException timeoutException) {
            return "timeout";
        } catch (Exception e) {
            Log.e("LOG","HTML connection error:\n"+e);
        }
        return html;
    }
    private String post(String url, String body){
        if(!isHaveNetwork()){
            return "no-network";
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] output = body.getBytes("UTF-8");
                outputStream.write(output);
            }
            int responseCode = connection.getResponseCode();
            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }
            }
            return stringBuilder.toString();
        } catch (SocketTimeoutException | UnknownHostException timeoutException) {
            return "timeout";
        } catch (Exception e) {
            Log.e("LOG","HTML connection error:\n"+e);
            return null;
        }
    }
}
