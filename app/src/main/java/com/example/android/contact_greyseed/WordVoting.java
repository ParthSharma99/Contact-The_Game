package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class WordVoting extends AppCompatActivity {
    DatabaseReference players,reference;
    private ArrayList<String> playerNames;
    RadioGroup listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_voting);
        reference = FirebaseDatabase.getInstance().getReference("ContactWord").child(new playerName().getGameCode());
        players = FirebaseDatabase.getInstance().getReference("Players").child(new playerName().getGameCode());
        listView = findViewById(R.id.radioChoice);
        playerNames = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        players.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,String> data = (HashMap<String, String>) dataSnapshot.getValue();
                for(int i =0;i<data.size();i++){
                    playerNames.add(String.valueOf(i));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        for(int i=0;i<playerNames.size();i++){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(playerNames.get(i));
            radioButton.setId(View.generateViewId());
            listView.addView(radioButton);
        }
    }
    public void select(View view){
        reference.child("Choose").child(new playerName().getName()).setValue(playerNames.get(listView.getCheckedRadioButtonId()));
        Intent intent = new Intent(this,WordEnter.class);
        startActivity(intent);
    }
}
