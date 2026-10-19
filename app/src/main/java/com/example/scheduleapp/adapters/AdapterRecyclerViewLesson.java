package com.example.scheduleapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.scheduleapp.Classes.Lesson;
import com.example.scheduleapp.R;

import java.util.List;

public class AdapterRecyclerViewLesson extends RecyclerView.Adapter<AdapterRecyclerViewLesson.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Lesson> lessons;
    private final SharedPreferences sp;

    public AdapterRecyclerViewLesson(Context context, List<Lesson> lessons, SharedPreferences sp) {
        this.inflater = LayoutInflater.from(context);
        this.lessons = lessons;
        this.sp = sp;
    }

    @Override
    public AdapterRecyclerViewLesson.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.include_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterRecyclerViewLesson.ViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.lesson_count.setText(lesson.id+"");
        holder.lesson_name.setText(lesson.subject);
        holder.lesson_teacher.setText(lesson.teacher);
        holder.lesson_time.setText(lesson.time);
        holder.lesson_subgroup.setText(lesson.subgroup == 1 ? "" : "."+lesson.subgroup);
        holder.lesson_room.setText(lesson.auditory);
        String note = sp.getString(lesson.subject+"_"+lesson.teacher,"");
        if(!note.isEmpty()){
            holder.line_note.setVisibility(View.VISIBLE);
            holder.note_text.setText(note);
        }
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lesson_count,lesson_subgroup,lesson_name,lesson_teacher,lesson_time,lesson_type,lesson_room;
        ImageView line_note;
        TextView note_text,note_star;
        ViewHolder(View view){
            super(view);
            lesson_count = view.findViewById(R.id.lesson_count);
            lesson_subgroup = view.findViewById(R.id.lesson_subgroup);
            lesson_name = view.findViewById(R.id.lesson_name);
            lesson_teacher = view.findViewById(R.id.lesson_teacher);
            lesson_time = view.findViewById(R.id.lesson_time);
            lesson_type = view.findViewById(R.id.lesson_type);
            lesson_room = view.findViewById(R.id.lesson_room);
            line_note = view.findViewById(R.id.line_note);
            note_text = view.findViewById(R.id.note_text);
            note_star = view.findViewById(R.id.note_star);
        }
    }
}
