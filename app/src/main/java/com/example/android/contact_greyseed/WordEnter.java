package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class WordEnter extends AppCompatActivity {
    DatabaseReference reference,player_numbers;
    Collection<String> list;
    int player_num;
    boolean run;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_enter);
        reference = FirebaseDatabase.getInstance().getReference("ContactWord").child(new playerName().getGameCode());
        player_numbers = FirebaseDatabase.getInstance().getReference("Players").child(new playerName().getGameCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        player_num = 0;
        player_numbers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    HashMap<String,String> map = (HashMap<String,String>)dataSnapshot.getValue();
                    player_num = map.size();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        run = true;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!run){return;}
                HashMap<String,String> map = new HashMap<>();
                map = (HashMap<String, String>)dataSnapshot.getValue();
                list = Objects.requireNonNull(map).values();
                if(list.size() == player_num){
                    task();
                    run = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void task(){
        HashMap<String,Integer> win = new HashMap<>();
        String temp = "",winner = "";
        int temp_val ,max = 0;
        while(list.iterator().hasNext()){
            temp = list.iterator().next();
            temp_val = 0;
            if(win.containsKey(temp)){
                temp_val = win.get(temp);
                win.remove(temp);
            }
            win.put(temp,temp_val+1);
            if(temp_val+1 > max){
                max = temp_val+1;
                winner = temp;
            }
        }
        if(winner.equals(new playerName().getName())){
            findViewById(R.id.wordEnter).setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView8).setVisibility(View.INVISIBLE);
            findViewById(R.id.button11).setVisibility(View.VISIBLE);
        }
    }

    public void done(View view){
        TextView textView = findViewById(R.id.wordEnter);
        String word = textView.getText().toString();
        reference.removeValue();
        reference.setValue(word);
        Intent intent = new Intent(this,player_game_screen.class);
        startActivity(intent);
    }

}
