package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    RecyclerView playerList;
    PlayersListViewAdapter adapter;
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
        playerList.setHasFixedSize(true);
        playerList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));



        playerNames = new ArrayList<>();
//        playerNames.add(new playerName().getName());

        adapter = new PlayersListViewAdapter(playerNames);
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
                playerNames.clear();
                if(data == null || data.keySet().isEmpty())return;
                for(String s : data.keySet()){
                    if(s.equals("NotThis") && data.get(s).equals("NotThis"))continue;
                    if(data.get(s).equals("HOST")){
                        adapter.host = s;
                    }
                    if(!playerNames.contains(s)){
                        playerNames.add(s);
                    }
                }
                adapter.notifyDataSetChanged();
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
                    startActivityForResult(intent,1);
                    finish();
                }else if(dataSnapshot.child(gameCode).getValue(String.class).equals("End") || new playerName().getGameCode().equals("")){
                    finishActivity(1);
                    finish();
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
