package com.example.android.contact_greyseed;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class Lobby extends AppCompatActivity {
    static DatabaseReference ref,players,chat,contactWord,hints;
    static ArrayList<String> gameCodeName,playerNames;
    ArrayList<Message> messages;
    int mx = 4,n1=0,n2=0;
    private TextView gameCode;
    static String name = "";
     ListView playerList;
    static ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        chat = FirebaseDatabase.getInstance().getReference("GameChat");
        ref = FirebaseDatabase.getInstance().getReference("Games");
        players = FirebaseDatabase.getInstance().getReference("Players");
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord");
        hints = FirebaseDatabase.getInstance().getReference("Hints");


        playerList = findViewById(R.id.playerList);
        gameCode = findViewById(R.id.gameCode);
        messages = new ArrayList<>();
        messages.add(new Message("0",new playerName().getName(),new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date())));

        gameCodeName = new ArrayList<String>();
        playerNames = new ArrayList<String>();
        gameCodeName.add("HELLO");
        gameCodeName.add("CATCH");
        gameCodeName.add("MONTE");
        gameCodeName.add("CHASE");

//        name = gameCodeName.get(n1) + " " + gameCodeName.get(n2);
        adapter = new ArrayAdapter<>(Lobby.this,android.R.layout.simple_list_item_1,playerNames);
        playerList.setAdapter(adapter);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean found = false;
                    for(n1=0;n1<mx;n1++){
                        for(n2=0;n2<mx;n2++){
                            name = gameCodeName.get(n1) + " " + gameCodeName.get(n2) ;
                            if(!dataSnapshot.hasChild(name)){
                                found = true;
                                break;
                            }
                        }
                        if(found)
                            {break;}
                    }
                    if(!found)return;
                    gameCode.setText(name);
                    new playerName().setGameCode(name);
                    ref.child(name).setValue("Active");
                    chat.child(name).setValue(messages);
                    contactWord.child(name).child("NotThis").child("YO").setValue("yo");
                    hints.child(name).child("NotThis").setValue(0);
                    playerNames.add(new playerName().getName());
                    adapter.notifyDataSetChanged();
                    players.child(name).setValue(playerNames);
                    check();
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(Lobby.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void check(){

        players.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while(dataSnapshot.child(name).getValue() == null){}
                HashMap<String,ArrayList<String>> temp = (HashMap<String,ArrayList<String>>) dataSnapshot.getValue();
                playerNames =temp.get(name);
                adapter = new ArrayAdapter<>(Lobby.this,android.R.layout.simple_list_item_1,playerNames);
                playerList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void cancel(View view){
        ref.child(name).setValue("End");
        players.child(name).removeValue();
        finish();
    }

    public void start(View view){
        if(name == ""){return;}

//        ref.child(name).setValue("Begun");

        Intent intent = new Intent(Lobby.this,EnterContactWord.class);
        startActivity(intent);
    }
}
