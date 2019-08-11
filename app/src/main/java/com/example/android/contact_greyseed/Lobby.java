package com.example.android.contact_greyseed;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class Lobby extends AppCompatActivity {
    static DatabaseReference ref,players,chat,contactWord,hints,gameWord;
    private ValueEventListener listener1,listener2,listener3;
    static ArrayList<String> gameCodeName,playerNames;
    ArrayList<Message> messages;
    int mx = 4,n1=0,n2=0;
    private TextView gameCode;
    static String name = "";
    RecyclerView playerList;
    boolean found = false,enterWord = false;
    static PlayersListViewAdapter adapter;
    LiveData<DataSnapshot> liveDataPlayers,liveDataGameStatus;


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
        playerList.setHasFixedSize(true);
        playerList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        gameCode = findViewById(R.id.gameCode);
        messages = new ArrayList<>();
        messages.add(new Message("0",new playerName().getName(),new SimpleDateFormat("ddMMyyyyhhmmssSSS",Locale.ENGLISH).format(new Date())));

        gameCodeName = new ArrayList<>();
        playerNames = new ArrayList<>();
        gameCodeName.add("HELLO");
        gameCodeName.add("CATCH");
        gameCodeName.add("MONTE");
        gameCodeName.add("CHASE");

        playerList.setClickable(false);
//        name = gameCodeName.get(n1) + " " + gameCodeName.get(n2);
        adapter = new PlayersListViewAdapter(playerNames);
        playerList.setAdapter(adapter);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    found = false;
                    for(n1=0;n1<mx;n1++){
                        for(n2=0;n2<mx;n2++){
                            name = gameCodeName.get(n1) + " " + gameCodeName.get(n2) ;
                            if(!dataSnapshot.hasChild(name) || Objects.requireNonNull(dataSnapshot.child(name).getValue(String.class)).equals("End")){
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
                    players.child(name).child(new playerName().getName()).setValue("HOST");
                    players.child(name).child("NotThis").setValue("NotThis");
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


//        ViewModelPlayers viewModelPlayers = ViewModelProviders.of(this).get(ViewModelPlayers.class);
//        liveDataPlayers = viewModelPlayers.getDataSnapshotLiveData(name);
//        liveDataPlayers.observe(this, new Observer<DataSnapshot>() {
//            @Override
//            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
//                if(dataSnapshot==null)return;
////                Toast.makeText(Lobby.this, dataSnapshot.toString(), Toast.LENGTH_LONG).show();
//                Object object = dataSnapshot.getValue(Object.class);
//                String json = new Gson().toJson(object);
//                Type type = new TypeToken<HashMap<String,String>>(){}.getType();
//                HashMap<String,String> temp = new Gson().fromJson(json,type);
//                playerNames.clear();
//                if(temp == null || temp.keySet().isEmpty())return;
//                for(String s:   temp.keySet()){
//                    if(s.equals("NotThis") && temp.get(s).equals("NotThis"))continue;
//                    if(temp.get(s).equals("HOST")){
//                        adapter.host = s;
//                    }
//                    if(!playerNames.contains(s)){
//                        playerNames.add(s);
//                    }
//                }
//                adapter.notifyDataSetChanged();
//            }
//        });

        listener1 = players.child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while(dataSnapshot.getValue() == null){}
                HashMap<String,String> temp = (HashMap<String,String>) dataSnapshot.getValue();
                playerNames.clear();
                if(temp == null || temp.keySet().isEmpty())return;
                for(String s:   temp.keySet()){
                    if(s.equals("NotThis") && temp.get(s).equals("NotThis"))continue;
                    if(temp.get(s).equals("HOST")){
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

//        ViewModelGameStatus viewModelGameStatus = ViewModelProviders.of(this).get(ViewModelGameStatus.class);
//        liveDataGameStatus = viewModelGameStatus.getDataSnapshotLiveData(name);
//        liveDataGameStatus.observe(this, new Observer<DataSnapshot>() {
//            @Override
//            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
//                if(dataSnapshot==null)return;
//                if(gameCode == null || dataSnapshot.getValue()==null)return;
//                if(Objects.requireNonNull(dataSnapshot.getValue(String.class)).equals("End")){
//                    finishActivity(2);
//                    finish();
//                }
//            }
//        });
        listener2 = ref.child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(gameCode == null || dataSnapshot.getValue()==null)return;
                if(dataSnapshot.getValue(String.class).equals("End")){
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    public void enterWord(View view){
        EditText text = findViewById(R.id.contactWordEnter);
        gameWord = FirebaseDatabase.getInstance().getReference("GameWord");
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(new playerName().getGameCode());
        contactWord.child("Status").setValue("No Contact");

        String s = text.getText().toString();
        s = s.toUpperCase().trim();
        if(s.contains(" ") || s.equals("")){
            text.setText("");
            text.setHint("ENTER WORD");
            return;
        }
        gameWord.child(name).child("Word").setValue(s);
        gameWord.child(name).child("Progress").setValue("0");
        gameWord.child(name).child("Leader").setValue("0");
        ref.child(name).setValue("Begun");
        Intent intent = new Intent(Lobby.this,leader_game_screen.class);
        startActivityForResult(intent,2);
//        listener3 = ref.child(name).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(Objects.requireNonNull(dataSnapshot.getValue(String.class)).equals("Begun")){
//                    Intent intent = new Intent(Lobby.this,leader_game_screen.class);
//                    startActivityForResult(intent,2);
//                }else if(Objects.requireNonNull(dataSnapshot.getValue(String.class)).equals("End")){
//                    finishActivity(2);
//                    finish();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    public void start(View view){
        if(name.equals("")){return;}
        if(enterWord){
            enterWord(view);
            return;
        }
        findViewById(R.id.playerList).setVisibility(View.GONE);
        EditText text = findViewById(R.id.contactWordEnter);
        text.setVisibility(View.VISIBLE);
        ((Button)view).setText("DONE");
        enterWord = true;
    }

    public void cancel(View view){
        players.child(name).child(new playerName().getName()).removeValue();
//        for(String player : playerNames){
//            players.child(name).child(player).removeValue();
//        }
        ref.child(name).setValue("End");
        finish();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(listener1!=null)
//            players.child(name).removeEventListener(listener1);
//        if(listener2!=null)
//            ref.child(name).removeEventListener(listener2);
//        if(listener3!=null)
//            ref.child(name).removeEventListener(listener3);
//    }
}
