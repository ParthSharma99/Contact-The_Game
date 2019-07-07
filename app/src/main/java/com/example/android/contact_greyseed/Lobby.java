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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Lobby extends AppCompatActivity {
    static DatabaseReference ref,players,chat,contactWord,hints;
    static ArrayList<String> gameCodeName,playerNames;
    ArrayList<Message> messages;
    int max = 3;
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
        messages.add(new Message("0",new playerName().getName()));

        gameCodeName = new ArrayList<String>();
        playerNames = new ArrayList<String>();
        gameCodeName.add("HELLO");
        gameCodeName.add("CATCH");
        gameCodeName.add("MONTE");
        gameCodeName.add("CHASE");

        name = gameCodeName.get(new Random().nextInt(max)) + " " + gameCodeName.get(new Random().nextInt(max));
        adapter = new ArrayAdapter<>(Lobby.this,android.R.layout.simple_list_item_1,playerNames);
        playerList.setAdapter(adapter);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(name)){
                    while(dataSnapshot.hasChild(name) && dataSnapshot.child(name).getValue(String.class).equals("Begun"))
                    name = gameCodeName.get(new Random().nextInt(max)) + " " + gameCodeName.get(new Random().nextInt(max)) ;
                }else{
                    gameCode.setText(name);
                    new playerName().setGameCode(name);
                    ref.child(name).setValue("Active");
                    chat.child(name).setValue(messages);
//                    contactWord.child(name).setValue(mp);
                    ArrayList<String> hintMsgIndex = new ArrayList<>();
                    hintMsgIndex.add("YO");
                    hintMsgIndex.add("0");
                    hintMsgIndex.add("0");
                    hintMsgIndex.add("0");
                    hints.child(name).child("0").setValue(hintMsgIndex);
                    playerNames.add(new playerName().getName());
                    adapter.notifyDataSetChanged();
                    players.child(name).setValue(playerNames);
                    check();
                }
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
