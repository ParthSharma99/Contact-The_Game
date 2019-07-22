package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Lobby_other extends AppCompatActivity {
    static DatabaseReference players,ref;
    static ArrayList<String> playerNames;
    ListView playerList;
    ArrayAdapter<String> adapter;
    String gameCode;
    TextView codeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_other);

        codeView = findViewById(R.id.gameCode_other);
        players = FirebaseDatabase.getInstance().getReference("Players");
        ref = FirebaseDatabase.getInstance().getReference("Games");
        playerList = findViewById(R.id.list_players);

        playerNames = new ArrayList<>();
//        playerNames.add(new playerName().getName());

        adapter = new ArrayAdapter<>(Lobby_other.this,android.R.layout.simple_list_item_1,playerNames);
        playerList.setAdapter(adapter);

        gameCode = Objects.requireNonNull(getIntent().getExtras()).getString("gameCode");
        codeView.setText(gameCode);

        while(gameCode == null){gameCode = Objects.requireNonNull(getIntent().getExtras()).getString("gameCode");}
        new playerName().setGameCode(gameCode);
        playerList.setClickable(false);

        players.child(gameCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){return;}
                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
                if(data == null || data.keySet().isEmpty())return;
                for(String s : data.keySet()){
                    if(!playerNames.contains(s)){
                        playerNames.add(s);
                    }
                }
                adapter = new ArrayAdapter<>(Lobby_other.this,android.R.layout.simple_list_item_1,playerNames);
                playerList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while(gameCode == null || dataSnapshot.child(gameCode).getValue()==null){}

                if(dataSnapshot.child(gameCode).getValue(String.class).equals("Begun")){
                    Intent intent = new Intent(Lobby_other.this,player_game_screen.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void leave(View view){
        players.child(gameCode).child(new playerName().getName()).removeValue();
        finish();
    }
}
