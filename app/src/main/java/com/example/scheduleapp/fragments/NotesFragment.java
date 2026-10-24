package com.example.scheduleapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.scheduleapp.MainActivity;
import com.example.scheduleapp.R;

public class NotesFragment extends Fragment {

    private SharedPreferences spNotes, sp;
    private MainActivity mainActivity;
    private LayoutInflater inflater;
    private LinearLayout notesContainer;

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
        render();
    }

    private void render(){
        notesContainer.removeAllViews();
        SharedPreferences.Editor spNotesEdit = spNotes.edit();
        spNotes.getAll().forEach((key, value)->{
            if(key.split("_")[0].equals(sp.getString("current_group", "Ошибка"))){
                LinearLayout noteView = (LinearLayout) inflater.inflate(R.layout.include_note, notesContainer, false);
                ((TextView) noteView.findViewById(R.id.note_extra)).setText(key.split("_")[2]);
                ((TextView) noteView.findViewById(R.id.note_name)).setText(key.split("_")[1]);
                ((TextView) noteView.findViewById(R.id.note_text)).setText(String.valueOf(value));
                noteView.setOnClickListener(ignored -> {
                    View alertInclude = mainActivity.findViewById(R.id.alert_include);
                    View alertBackground = alertInclude.findViewById(R.id.alert_background);
                    TextView mainText = alertInclude.findViewById(R.id.alert_mainText);
                    TextView extraText = alertInclude.findViewById(R.id.alert_extraText);
                    EditText editText = alertInclude.findViewById(R.id.alert_editText);
                    View deleteButton = alertInclude.findViewById(R.id.alert_deleteButton);
                    View saveButton = alertInclude.findViewById(R.id.alert_saveButton);

                    mainText.setText(key.split("_")[1]);
                    extraText.setText(key.split("_")[2]+" * Заметка");
                    editText.setText(String.valueOf(value));

                    alertBackground.setOnClickListener(view -> {
                        alertInclude.setVisibility(View.GONE);
                    });
                    deleteButton.setOnClickListener(view -> {
                        spNotesEdit.remove(key);
                        alertInclude.setVisibility(View.GONE);
                        render();
                    });
                    saveButton.setOnClickListener(view -> {
                        if (!String.valueOf(editText.getText()).isEmpty())
                            spNotesEdit.putString(key, "" + editText.getText());
                        alertInclude.setVisibility(View.GONE);
                        render();
                    });
                    alertInclude.setVisibility(View.VISIBLE);
                });
                notesContainer.addView(noteView);
            }
        });
        spNotesEdit.apply();
    }
}