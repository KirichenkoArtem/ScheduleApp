package com.example.scheduleapp;

import java.util.List;

public class Classes {
    public static class Schedule{
        public String groupName;
        public List<Week> weeks;
        public Schedule(String groupName, List<Week> weeks) {
            this.groupName = groupName;
            this.weeks = weeks;
        }
    }
    public static class Week{
        public List<Day> days;
        public Week(List<Day> days) {
            this.days = days;
        }
    }
    public static class Day{
        public String weekDay;
        public String date;
        public String even;
        public List<Lesson> lessons;
        public Day(String weekDay, String date, String even, List<Lesson> lessons) {
            this.weekDay = weekDay;
            this.date = date;
            this.even = even;
            this.lessons = lessons;
        }
    }
    public static class Lesson{
        public int id;
        public int subgroup;
        public String subject;
        public String time;
        public String teacher;
        public String auditory;
        public String type;
        public Lesson(int id, int subgroup, String subject, String time, String teacher, String auditory, String type) {
            this.id = id;
            this.subgroup = subgroup;
            this.subject = subject;
            this.time = time;
            this.teacher = teacher;
            this.auditory = auditory;
            this.type = type;
        }
    }
}
