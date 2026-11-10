package com.example.scheduleapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scheduleapp.BasicUtils;
import com.example.scheduleapp.MainActivity;
import com.example.scheduleapp.R;

public class NotesFragment extends Fragment {

    private SharedPreferences spNotes, sp;
    private MainActivity mainActivity;
    private LayoutInflater inflater;
    private LinearLayout notesContainer;
    private ImageView addNoteButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();
        spNotes = mainActivity.spNotes;
        sp = mainActivity.sp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notesContainer = view.findViewById(R.id.notes_container);
        addNoteButton = mainActivity.findViewById(R.id.headerAddButton);
        render();
        addNoteButton.setOnClickListener((v)->{
            mainActivity.basicUtils.openAddNote(mainActivity,this::render);
        });
    }

    private void render(){
        notesContainer.removeAllViews();
        SharedPreferences.Editor spNotesEdit = spNotes.edit();
        spNotes.getAll().forEach((key, value)->{
            if(key.split("_")[0].equals(sp.getString("current_group", "Ошибка"))){
                LinearLayout noteView = (LinearLayout) inflater.inflate(R.layout.include_note, notesContainer, false);
                String noteName = key.split("_")[1];
                if(key.split("_").length>1) {
                    ((TextView) noteView.findViewById(R.id.note_extra)).setText(key.split("_")[2]);
                }else{
                    ((TextView) noteView.findViewById(R.id.note_extra)).setText("");
                }
                String noteExtra = ""+((TextView) noteView.findViewById(R.id.note_extra)).getText();
                String noteValue = ""+value;
                ((TextView) noteView.findViewById(R.id.note_name)).setText(noteName);
                ((TextView) noteView.findViewById(R.id.note_text)).setText(noteValue);
                noteView.setOnClickListener(ignored -> mainActivity.basicUtils.openChangeNote(
                        mainActivity,(sp.getString("current_group","")+"_"+noteName+"_"+noteExtra),
                        noteName,
                        noteExtra+" * Заметка",
                        noteValue,
                        this::render
                ));
                notesContainer.addView(noteView);
            }
        });
        spNotesEdit.apply();
    }
}