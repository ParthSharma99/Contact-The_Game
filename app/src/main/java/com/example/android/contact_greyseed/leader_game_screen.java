package com.example.android.contact_greyseed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class leader_game_screen extends AppCompatActivity implements HintTrackAdapter.OnHintClickListener,MessageAdapter.OnMessageClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord,players,games;
    private ValueEventListener listener1,listener2,listener3;
    private ChildEventListener childEventListener1,childEventListener2,childEventListener3;
    private RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapter hintTrackAdapter;
    private ArrayList<Message> messagesAdapter;
    private static ArrayList<HintTrack> hintTracks;
    private String gameCode,word,progress;
    private int idx = 1,i=0,temp,hintTrackSelectIdx = 0,guessTime = 0,messagesSelected = 0, hintTracksSoFar = 0, shortAnimationDuration = 100;
    private ImageButton cancelButton,cancelGuess,sendGuess,sendButton;
    private RecyclerView playerListView;
    private PlayersListViewAdapter playerListAdapter;
    HashMap<String,HashMap<String,String>> hintTrackContactData;
    ArrayList<String> msgTimeStamp, hintTimeStamp,playerList;
    HashMap<String,String> guessesHintTrack;
    TextView wordArea,playerListGameCode;
    Button guessBtn,challengeBtn;
    EditText editText;
    LiveData<DataSnapshot> liveDataMessages,liveDataPlayers,liveDataGameWord;
    String msg,hintTrackSelectTimestamp="",datePattern;
    int currentScore = 0;
    boolean playersShown = false,playerInTheGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_game_screen);


        recyclerView = findViewById(R.id.message_box_leader);
        hintTrackView = findViewById(R.id.hintTrackView_leader);
        wordArea = findViewById(R.id.wordArea_leader);
        playerListView = findViewById(R.id.players);
        playerListView.setHasFixedSize(true);
        playerListView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));


//        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hintTrackView.setMinimumHeight(200);
        hintTrackContactData = new HashMap<>();

        sendButton = findViewById(R.id.button9_leader);
        guessBtn = findViewById(R.id.guessContact);
        challengeBtn = findViewById(R.id.challengeBtn);
        editText = findViewById(R.id.hint_message_leader);
        cancelButton = findViewById(R.id.cancelHintSelect);
        cancelGuess = findViewById(R.id.cancelGuess);
        sendGuess = findViewById(R.id.sendGuess);
        playerListGameCode = findViewById(R.id.players_game_code);

        datePattern = "ddMMyyyyhhmmssSSS";

        hintTrackView.setHasFixedSize(true);
        hintTrackView.setLayoutManager(new LinearLayoutManager(this,LinearLayout.HORIZONTAL,false));
        hintTrackView.setMinimumHeight(10);

        messagesAdapter = new ArrayList<>();
        hintTracks = new ArrayList<>();
        msgTimeStamp = new ArrayList<>();
        hintTimeStamp = new ArrayList<>();
        guessesHintTrack = new HashMap<>();
        playerList = new ArrayList<>();
//        playerList.add(new playerName().getName() + "\t" + "(HOST) (Leader)");
        playerListAdapter = new PlayersListViewAdapter(playerList);
        playerListView.setAdapter(playerListAdapter);



        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        gameCode = new playerName().getGameCode();
        if(gameCode == ""){
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
            finish();
        }

        reference = FirebaseDatabase.getInstance().getReference("GameChat").child(gameCode);
        hints = FirebaseDatabase.getInstance().getReference("Hints").child(gameCode);
        gameWord = FirebaseDatabase.getInstance().getReference("GameWord").child(gameCode);
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(gameCode);
        players = FirebaseDatabase.getInstance().getReference("Players").child(gameCode);
        games = FirebaseDatabase.getInstance().getReference("Games").child(gameCode);

        playerListGameCode.setText(gameCode);

        adapter = new MessageAdapter(messagesAdapter,leader_game_screen.this);
        recyclerView.setAdapter(adapter);

        hintTrackAdapter = new HintTrackAdapter(hintTracks,this);
        hintTrackView.setAdapter(hintTrackAdapter);
        games.setValue("Begun");


        childEventListener1 = reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getValue()==null)return;
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("0"))return;

                Object ob = dataSnapshot.getValue(Object.class);
                String data = new Gson().toJson(ob);
                if(data == null)return;
                Message temp = new Gson().fromJson(data,Message.class);
                if(temp == null || temp.getSender() == null)return;
                if(temp.getSender().equals(new playerName().getName())){
                    temp.side = 1;
                }else if(temp.getSender().equals("UniversalMessageCop")){
                    temp.side = 3;
                }else{
                    temp.side = 2;
                }

                int i = messagesAdapter.size()-1;
                while(i>0 && messagesAdapter.get(i).timeStamp.compareTo(temp.timeStamp) > 0){
                    i--;
                }
//                if(i<0){i=0;}
                messagesAdapter.add(i+1,temp);
                adapter.notifyItemInserted(i+1);
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
                msgTimeStamp.add(dataSnapshot.getKey());
//                    messagesAdapter.sort(new Comparator<Message>() {
//                        @Override
//                        public int compare(Message message, Message t1) {
//                            return message.timeStamp.compareTo(t1.timeStamp);
//                        }
//                    });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue()==null)return;
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("0"))return;
                Object ob = dataSnapshot.getValue(Object.class);
                String data = new Gson().toJson(ob);
                if(data == null)return;
                Message temp = new Gson().fromJson(data,Message.class);
                if(temp == null || temp.getSender() == null)return;
                if(temp.getSender().equals(new playerName().getName())){
                    temp.side = 1;
                }else if(temp.getSender().equals("UniversalMessageCop")){
                    temp.side = 3;
                }
                else{
                    temp.side = 2;
                }
                for(int i=0;i<messagesAdapter.size();i++){
                    if(messagesAdapter.get(i).timeStamp.equals(temp.timeStamp)){
                        messagesAdapter.set(i,temp);
                        adapter.notifyItemChanged(i);
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)return;
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("a"))return;
                int num = messagesAdapter.size();
                messagesAdapter.clear();
                msgTimeStamp.clear();
                adapter.notifyItemRangeRemoved(0,num);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        childEventListener2 = hints.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("NotThis"))return;
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("Status"))return;
//                ArrayList<Message> msg_db = new ArrayList<>();
//                ArrayList<Integer> idx_db = new ArrayList<>();
                Object temp = dataSnapshot.getValue(Object.class);
                String json = new Gson().toJson(temp);
                HintTrack ht = new Gson().fromJson(json,HintTrack.class);
                if(hintTracks.contains(ht))return;
                hintTracks.add(ht);
                hintTimeStamp.add(dataSnapshot.getKey());
                hintTrackAdapter.notifyItemInserted(hintTracks.size()-1);
//                hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
                hintTrackView.requestFocus();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("NotThis"))return;
                int hint_idx = -1;
                boolean found = false;
                for(int i=0;i<hintTracks.size();i++){
                    if(dataSnapshot.getKey().equals(hintTracks.get(i).timeStamp)){
                        hint_idx = i;
                        found = true;
                    }else if(found && hintTracks.get(i).timeStamp.equals(hintTrackSelectTimestamp)){
                        hintTrackSelectIdx = i-1;
                        hintTracks.remove(hint_idx);
                        hintTrackAdapter.notifyItemRemoved(hint_idx);
//                        guessesHintTrack.remove(dataSnapshot.getKey());
                        hintTrackContactData.remove(hintTrackSelectTimestamp);
                        contactWord.child(dataSnapshot.getKey()).removeValue();
                        onClick(hintTrackSelectIdx);
                        return;
                    }
                }
                if(hint_idx==-1)return;
                hintTracks.remove(hint_idx);
                hintTrackAdapter.notifyItemRemoved(hint_idx);
//                guessesHintTrack.remove(dataSnapshot.getKey());
                hintTrackContactData.remove(hintTrackSelectTimestamp);
                contactWord.child(dataSnapshot.getKey()).removeValue();
                if(hintTrackSelectTimestamp.equals(dataSnapshot.getKey())){
                    reset();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        childEventListener3 = contactWord.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey() == null)return;
                if(dataSnapshot.getKey().equals("NotThis"))return;
                if(dataSnapshot.getKey().equals("Status"))return;
//                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
                Object temp = dataSnapshot.getValue(Object.class);
                String json = new Gson().toJson(temp);
                Type type = new TypeToken<HashMap<String,String>>(){}.getType();
                HashMap<String,String> data = new Gson().fromJson(json,type);
                if(data == null)return;
//                if(data.containsKey("Guesses")){
//                    guessesHintTrack.remove(dataSnapshot.getKey());
//                    guessesHintTrack.put(dataSnapshot.getKey(),data.get("Guesses"));
//                    data.remove("Guesses");
//                }
                int i=0;
                for(i=0;i<hintTracks.size();i++){
                    if(hintTracks.get(i).timeStamp.equals(dataSnapshot.getKey())){
                        hintTracks.get(i).count = data.size()-1;
                        break;
                    }
                }
                hintTrackContactData.put(dataSnapshot.getKey(),data);
                hintTrackAdapter.notifyItemChanged(i);
                guessCross();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey() == null)return;
                if(dataSnapshot.getKey().equals("NotThis"))return;
                if(dataSnapshot.getKey().equals("Status"))return;
//                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
                Object temp = dataSnapshot.getValue(Object.class);
                String json = new Gson().toJson(temp);
                Type type = new TypeToken<HashMap<String,String>>(){}.getType();
                HashMap<String,String> data = new Gson().fromJson(json,type);
                if(data == null)return;
//                if(data.containsKey("Guesses")){
//                    guessesHintTrack.remove(dataSnapshot.getKey());
//                    guessesHintTrack.put(dataSnapshot.getKey(),data.get("Guesses"));
//                    data.remove("Guesses");
//                }
                int i=0;
                for(i=0;i<hintTracks.size();i++){
                    if(hintTracks.get(i).timeStamp.equals(dataSnapshot.getKey())){
                        hintTracks.get(i).count = data.size()-1;
                        break;
                    }
                }
                hintTrackContactData.put(dataSnapshot.getKey(),data);
                hintTrackAdapter.notifyItemChanged(i);
                guessCross();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                guessesHintTrack.remove(dataSnapshot.getKey());
                int i=0;
                for(i=0;i<hintTracks.size();i++){
                    if(hintTracks.get(i).timeStamp.equals(dataSnapshot.getKey())){
                        break;
                    }
                }
                hintTrackContactData.remove(dataSnapshot.getKey());
                hintTrackAdapter.notifyItemRemoved(i);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ViewModelPlayers viewModelPlayers = ViewModelProviders.of(this).get(ViewModelPlayers.class);
        liveDataPlayers = viewModelPlayers.getDataSnapshotLiveData(gameCode);
        liveDataPlayers.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if(dataSnapshot == null || dataSnapshot.getValue()==null)return;
                Object object = dataSnapshot.getValue(Object.class);
                String json = new Gson().toJson(object);
                Type type = new TypeToken<HashMap<String,String>>(){}.getType();
                HashMap<String,String> data = new Gson().fromJson(json,type);
                if(data==null || data.keySet().isEmpty())return;
                playerList.clear();
                playerInTheGame = false;
                for(String s: data.keySet()){
                    if(s.equals("NotThis") && data.get(s).equals("NotThis"))continue;
                    if(s.equals(new playerName().getName())){playerInTheGame = true;}
                    if(data.get(s).equals("HOST")){
                        playerListAdapter.host = s;
                    }
                    if(!playerList.contains(s)){
                        playerList.add(s);
                    }
                    if(s.equals(new playerName().getName())){
                        playerInTheGame = true;
                    }
                }
                playerListAdapter.notifyDataSetChanged();
                if(!playerInTheGame){
                    finish();
                }
            }
        });

        ViewModelWord viewModelWord = ViewModelProviders.of(this).get(ViewModelWord.class);
        liveDataGameWord = viewModelWord.getDataSnapshotLiveData(gameCode);
        liveDataGameWord.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if(dataSnapshot == null || dataSnapshot.getValue()==null)return;
                Object object = dataSnapshot.getValue(Object.class);
                String json = new Gson().toJson(object);
                Type type = new TypeToken<HashMap<String,String>>(){}.getType();
                HashMap<String,String> data = new Gson().fromJson(json,type);
                word = data.get("Word");
                progress = String.valueOf(data.get("Progress"));
                word = word.toUpperCase();
                if (Integer.parseInt(progress)+1 <= word.length()){
                        String source = "<b>" + word.substring(0, Integer.parseInt(progress) + 1) + "</b>" + word.substring(Integer.parseInt(progress) + 1);
                        wordArea.setText(Html.fromHtml(source, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));
                    }
                String score = data.get("Leader");
                if(score==null)return;
                currentScore = Integer.valueOf(score);
                setTrophy(currentScore);
                if(currentScore == 5){
                    String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                    Message m1 = new Message("Leader has Won the Game." , "UniversalMessageCop",date);
                    gameWord.child("Progress").setValue(String.valueOf(word.length()));
                    reference.child(date).setValue(m1);
                }
            }
        });


//
//        players.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if(dataSnapshot.getValue()==null)return;
//                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("NotThis") && Objects.requireNonNull(dataSnapshot.getValue()).equals("NotThis"))return;
//                String player = dataSnapshot.getKey();
//                if(Objects.requireNonNull(dataSnapshot.getValue()).equals("HOST")){
//                    playerListAdapter.host = player;
//                }
//                if(!playerList.contains(player)){
//                    playerList.add(player);
//                }
//                playerListAdapter.notifyItemInserted(playerList.size()-1);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.getValue()==null)return;
//                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
//                if(data==null || data.keySet().isEmpty())return;
//                String playerRem = dataSnapshot.getKey();
//                playerList.remove(playerRem);
//                if(Objects.requireNonNull(playerRem).equals(playerName.name)){
//                    finish();
//                }
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


    }

    @Override
    protected void onStart() {
        super.onStart();

//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                while(dataSnapshot.getValue() == null){}
//                messagesAdapter.clear();
//                msgTimeStamp.clear();
//                for (DataSnapshot it : dataSnapshot.getChildren()) {
//                    Object ob = it.getValue(Object.class);
//                    String data = new Gson().toJson(ob);
//                    if(data == null)return;
//                    Message temp = new Gson().fromJson(data,Message.class);
//                    if(temp == null || temp.getSender() == null)continue;
//                    if(temp.getSender().equals(new playerName().getName())){
//                        temp.side = 1;
//                    }else if(temp.getSender().equals("UniversalMessageCop")){
//                        temp.side = 3;
//                    }else{
//                        temp.side = 2;
//                    }
//                    if(it.getKey().equals("0")){
//                        continue;
//                    }
//
//                    messagesAdapter.add(temp);
//                    messagesAdapter.sort(new Comparator<Message>() {
//                        @Override
//                        public int compare(Message message, Message t1) {
//                            return message.timeStamp.compareTo(t1.timeStamp);
//                        }
//                    });
//                    msgTimeStamp.add(it.getKey());
//                    adapter.notifyDataSetChanged();
////                    adapter = new MessageAdapter(messagesAdapter,leader_game_screen.this);
////                    recyclerView.setAdapter(adapter);
//                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

//        hints.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                if(dataSnapshot.getValue() != null){}
//                ArrayList<String> temp ;
//                hintTracks.clear();
//                hintTimeStamp.clear();
//                for(DataSnapshot it : dataSnapshot.getChildren()){
//                    ArrayList<Message> msg_db = new ArrayList<>();
//                    ArrayList<Integer> idx_db = new ArrayList<>();
//                    if( !dataSnapshot.hasChild(it.getKey())) return;
//
//                    if(it.getKey().equals("NotThis")){
////                        hintTracksSoFar = Objects.requireNonNull(dataSnapshot).child(it.getKey()).getValue(Integer.class);
//                        continue;
//                    }
//                    if(it.getKey().equals("Status")){
//                        hints.child("Status").setValue("No change");
//                        hintTrackAdapter.notifyDataSetChanged();
//                        continue;
//                    }
//
//                    temp = (ArrayList<String>) it.getValue();
//                    if(temp.isEmpty())return;
//                    if(temp.size()>0){
//                        for(int j=3;j<temp.size();j++){
//                            int t = Integer.valueOf(temp.get(j));
//                            if(t >=messagesAdapter.size())continue;
//                            msg_db.add(messagesAdapter.get(t));
//                            idx_db.add(t);
//                        }
//
////                        Toast.makeText(player_game_screen.this, idx_db.toString(), Toast.LENGTH_SHORT).show();
//                        int num = Integer.valueOf(temp.get(2));
//                        hintTracks.add(new HintTrack(idx_db,msg_db,temp.get(0),num,temp.get(1)));
//                        hintTimeStamp.add(it.getKey());
//                        hintTrackAdapter.notifyDataSetChanged();
//                        hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
////                        if(i>0){
////                            hintAdapter.add(hintTracks.get(i));
////                            hintTrackAdapter.notifyDataSetChanged();
////                            hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
////                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

//        listener1 = gameWord.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.getValue()==null)return;
//                HashMap<String,String> dt = (HashMap<String, String>)dataSnapshot.getValue();
//                if(dt!=null) {
//                    word = dt.get("Word");
//                    progress = String.valueOf(dt.get("Progress"));
//                    word = word.toUpperCase();
//                    if (Integer.parseInt(progress)+1 <= word.length()){
//                        String source = "<b>" + word.substring(0, Integer.parseInt(progress) + 1) + "</b>" + word.substring(Integer.parseInt(progress) + 1);
//                        wordArea.setText(Html.fromHtml(source, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));
//                    }
//                    String score = dt.get("Leader");
//                    currentScore = Integer.valueOf(score);
//                    setTrophy(currentScore);
//                    if(currentScore == 5){
//                        String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
//                        Message m1 = new Message("Leader has Won the Game." , "UniversalMessageCop",date);
//                        gameWord.child("Progress").setValue(String.valueOf(word.length()));
//                        reference.child(date).setValue(m1);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

//        contactWord.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                hintTrackContactData.clear();
//                guessesHintTrack.clear();
//                for(DataSnapshot it : dataSnapshot.getChildren()) {
//                    if(it.getKey().equals("NotThis") || it.getKey().equals("Status")){continue;}
//                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.child(it.getKey()).getValue();
//                    if(data == null || data.isEmpty())return;
//                    if(data.containsKey("Guesses")){
//                        guessesHintTrack.put(it.getKey(),data.get("Guesses"));
//                        data.remove("Guesses");
//                    }
//                    String t = it.getKey();
//                    for(int i=0;i<hintTracks.size();i++){
//                        if(hintTracks.get(i).timeStamp.equals(t)){
//                            hintTracks.get(i).count = data.size();
//                            break;
//                        }
//                    }
//                    hintTrackContactData.add(data);
//                    hintTrackAdapter.notifyDataSetChanged();
////                    hintTrackAdapter = new HintTrackAdapter(hintTracks, leader_game_screen.this);
////                    hintTrackView.setAdapter(hintTrackAdapter);
//                }
//                guessCross();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });

//        listener3 = players.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.getValue()==null)return;
//                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
//                if(data==null || data.keySet().isEmpty())return;
//                playerList.clear();
//                playerInTheGame = false;
//                for(String s: data.keySet()){
//                    if(s.equals("NotThis") && data.get(s).equals("NotThis"))continue;
//                    if(s.equals(new playerName().getName())){playerInTheGame = true;}
//                    if(data.get(s).equals("HOST")){
//                        playerListAdapter.host = s;
//                    }
//                    if(!playerList.contains(s)){
//                        playerList.add(s);
//                    }
//                    if(s.equals(new playerName().getName())){
//                        playerInTheGame = true;
//                    }
//                }
//                playerListAdapter.notifyDataSetChanged();
//                if(!playerInTheGame){
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

    public void setTrophy(int n){
        if(n>0){
            ((ImageView)findViewById(R.id.trophy1)).setImageResource(R.drawable.ic_trophy);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.trophy2)).setImageResource(R.drawable.ic_trophy);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.trophy3)).setImageResource(R.drawable.ic_trophy);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.trophy4)).setImageResource(R.drawable.ic_trophy);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.trophy5)).setImageResource(R.drawable.ic_trophy);
            n--;
        }
    }

    public void send_msg(View view){
        msg = editText.getText().toString();
        if(msg.trim().equals("")){return;}
        int count_words = 0,reenter = 0;
        for(int i=0;i<msg.length();i++){
            if(msg.charAt(i) == ' ' && i!=msg.length()-1 && i!=0){
                count_words++;
            }
            if(count_words >= 3){
                editText.setText(" ");
                reenter = 1;
                editText.setHint("Not more than 3 words");
                break;
            }
        }
        if(reenter == 0){
            String date= new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
            Message  temp = new Message(msg,new playerName().getName(),date);
            editText.setText("");
            reference.child(date).setValue(temp);
        }

        editText.setHint("Type a hint..");

    }

    @Override
    public void onClick(int pos) {
        reset();
        if(pos<0 || pos >= hintTracks.size())return;
        hintTrackSelectIdx = pos;
        HintTrack ht = hintTracks.get(pos);
        hintTrackSelectTimestamp = ht.timeStamp;
        if(ht.getSelected())return;
        ht.toggleSelected();
        hintTrackAdapter.notifyItemChanged(pos);
        String s = Objects.requireNonNull(hintTrackContactData.get(hintTrackSelectTimestamp)).get("Guesses");
        if(s==null)return;
        guessTime = Integer.parseInt(s);
        ArrayList<Message> msg = new ArrayList<>();
        for(int i : ht.list){
            msg.add(messagesAdapter.get(i));
        }
        MessageAdapter tempHintAdapter = new MessageAdapter(msg,leader_game_screen.this);
        findViewById(R.id.guess_cross_leader).setVisibility(View.VISIBLE);
        guessCross();
        recyclerView.setAdapter(tempHintAdapter);

        editText.setVisibility(View.INVISIBLE);
        sendButton.setVisibility(View.INVISIBLE);
        guessBtn.setVisibility(View.VISIBLE);

        if(hintTracks.get(hintTrackSelectIdx).count >=2){
            challengeBtn.setVisibility(View.VISIBLE);
        }
        cancelButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessageClick(int pos) {
        if(pos>=0 && pos < messagesAdapter.size()){
            messagesAdapter.get(pos).toggleSelect();
            if(messagesAdapter.get(pos).isSelected()){
                messagesSelected++;
            }else{
                messagesSelected--;
            }
            if(messagesSelected > 0){
                adapter.state = 1;
                sendButton.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.INVISIBLE);
                hintTrackView.setVisibility(View.INVISIBLE);
            }else{
                adapter.state = 0;
                sendButton.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                hintTrackView.setVisibility(View.VISIBLE);
            }
        }
        boolean ch = false;
        for(int i=0;i<messagesAdapter.size();i++){
            if(messagesAdapter.get(i).isSelected()){
                ch = true;
            }
        }
        if(!ch){
            adapter.state = 0;
            sendButton.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
            hintTrackView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void guessCross(){
        if(hintTrackSelectTimestamp == "" ){
            ((ImageView)findViewById(R.id.cross1_leader)).setImageResource(R.drawable.ic_unclicked_cross);
            ((ImageView)findViewById(R.id.cross2_leader)).setImageResource(R.drawable.ic_unclicked_cross);
            ((ImageView)findViewById(R.id.cross3_leader)).setImageResource(R.drawable.ic_unclicked_cross);
            return;
        }
        String s = Objects.requireNonNull(hintTrackContactData.get(hintTrackSelectTimestamp)).get("Guesses");
        if(s==null)return;
        int g = Integer.parseInt(s);
        guessTime = g;
        if(g>0){
            ((ImageView)findViewById(R.id.cross1_leader)).setImageResource(R.drawable.ic_guess_cross);
            g--;
        }
        if(g>0){
            ((ImageView)findViewById(R.id.cross2_leader)).setImageResource(R.drawable.ic_guess_cross);
            g--;
        }
        if(g>0){
            ((ImageView)findViewById(R.id.cross3_leader)).setImageResource(R.drawable.ic_guess_cross);
            g--;
        }
    }

    public void cancel(View view){

        reset();

        hintTrackSelectTimestamp="";
        for(int i=0;i<messagesAdapter.size();i++){
            if(messagesAdapter.get(i).isSelected()){
                messagesAdapter.get(i).toggleSelect();
            }
        }
    }

    public void guess(View view){
        guessBtn.setVisibility(View.GONE);
        sendButton.setVisibility(View.INVISIBLE);
        challengeBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Enter guess word..");
        cancelGuess.setVisibility(View.VISIBLE);
        sendGuess.setVisibility(View.VISIBLE);
        editText.requestFocus();
        showKeyboard();
    }

    public void reset(){
        UIUtil.hideKeyboard(this);
        guessBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.INVISIBLE);
        challengeBtn.setVisibility(View.GONE);
        cancelGuess.setVisibility(View.INVISIBLE);
        sendGuess.setVisibility(View.INVISIBLE);
        findViewById(R.id.guess_cross_leader).setVisibility(View.INVISIBLE);

        sendButton.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Type a hint..");
        hintTrackView.setVisibility(View.VISIBLE);
        hintTrackSelectTimestamp="";

        for(int i=0;i<messagesAdapter.size();i++){
            if(messagesAdapter.get(i).isSelected()){
                messagesAdapter.get(i).toggleSelect();
            }
        }
        adapter.state = 0;
        adapter.notifyDataSetChanged();

        for(int i=0;i<hintTracks.size();i++){
            if(hintTracks.get(i).getSelected()){
                hintTracks.get(i).toggleSelected();
            }
        }
        hintTrackAdapter.notifyDataSetChanged();

        if(playersShown) {
            playersShown = false;
            playerListGameCode.animate().alpha(0f).setDuration(shortAnimationDuration);
            playerListView.animate().alpha(0f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    playerListView.setVisibility(View.GONE);
                    playerListGameCode.setVisibility(View.GONE);
                }
            });
            findViewById(R.id.guessCrossContainerLeader).animate().alpha(0f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.guessCrossContainerLeader).setAlpha(1f);
                    findViewById(R.id.guessCrossContainerLeader).setVisibility(View.VISIBLE);
                    findViewById(R.id.message_box_leader).setVisibility(View.VISIBLE);
                    if(!hintTrackSelectTimestamp.equals("")){
                        findViewById(R.id.guess_cross_leader).setVisibility(View.VISIBLE);
                    }
                    findViewById(R.id.layout_message_send_leader).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_hint_track_leader).setVisibility(View.VISIBLE);
                }
            });
        }

        recyclerView.setAdapter(adapter);
        guessCross();
    }

    public void setSendGuess(View view){
        if(hintTrackSelectTimestamp=="")return;
        if(hintTracks.get(hintTrackSelectIdx).count == 1 ){
            if(guessTime == 2){
                String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader could not guess the word for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
                contactWord.child(hintTrackSelectTimestamp).child("Guesses").setValue("0");
                hintTrackSelectTimestamp = "";
                reset();
                return;
            }
            String temp = editText.getText().toString().trim().toLowerCase();
            String author = hintTracks.get(hintTrackSelectIdx).author;
            String authorWord = "";
            if(!checkMessage(temp)){
                editText.setText("");
                editText.setHint("Check the Game word.");
                return;
            }
            if(hintTrackContactData.get(hintTrackSelectTimestamp).containsKey(author)){
                authorWord = hintTrackContactData.get(hintTrackSelectTimestamp).get(author);
            }else{
                return;
            }
            if(temp.equals(authorWord.toLowerCase())){
                String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader guessed correctly for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
//                gameWord.child("Leader").setValue(String.valueOf(currentScore+1));
                deleteHintTrack();
            }else{
                String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader guessed incorrectly for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
                contactWord.child(hintTrackSelectTimestamp).child("Guesses").setValue(String.valueOf(++guessTime));
            }
            return;
        }


        if(guessTime == 2){
            guessTime = -1;
            setChallengeBtn(view);

        }else {

            if(hintTracks.get(hintTrackSelectIdx).count <2){
                editText.setText("");
                editText.setHint("Hint track has no contact.");
                return;
            }

            String temp = editText.getText().toString().trim().toLowerCase();
            String author = hintTracks.get(hintTrackSelectIdx).author;

            String authorWord = "";

            if(hintTrackContactData.get(hintTrackSelectTimestamp).containsKey(author)){
                authorWord = hintTrackContactData.get(hintTrackSelectTimestamp).get(author);
            }else{
                return;
            }

            if (temp.contains(" ") || temp.equals("")){
                return;
            }
            if (!checkMessage(temp)) {
                editText.setText("");
                editText.setHint("Check the Game word.");
                return;
            }
            authorWord = authorWord.toLowerCase();
            if(temp.equals(authorWord)){
                String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader guessed correctly for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
                hintTrackSelectTimestamp = "";
                gameWord.child("Leader").setValue(String.valueOf(currentScore+1));
                deleteHintTrack();

            }else{
                editText.setText("");
                editText.setHint("Sorry");
                String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader guessed incorrectly for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
                contactWord.child(hintTrackSelectTimestamp).child("Guesses").setValue(String.valueOf(++guessTime));
            }
        }
    }

    public void setChallengeBtn(View view){

        if(hintTrackSelectTimestamp=="")return;
        if(hintTracks.get(hintTrackSelectIdx).count <2){
            editText.setText("");
            editText.setHint("Hint track has no contact.");
            return;
        }
//        editText.setText("");
//        editText.setHint("Are you sure ?");
        String author = hintTracks.get(hintTrackSelectIdx).author;

        String authorWord = "";

        if(hintTrackContactData.get(hintTrackSelectTimestamp).containsKey(author)){
            authorWord = hintTrackContactData.get(hintTrackSelectTimestamp).get(author);
        }else{
            return;
        }
        authorWord = authorWord.toLowerCase();
        boolean found = false;
        for(String s : hintTrackContactData.get(hintTrackSelectTimestamp).keySet()){
            if(s.equals(author))continue;
            String t = hintTrackContactData.get(hintTrackSelectTimestamp).get(s);
            if(t.toLowerCase().equals(authorWord)){
                guessTime = 0;
                int temp = Integer.valueOf(progress);
                progress = String.valueOf(temp+1);
                gameWord.child("Progress").setValue(progress);
                for(String msgDel : msgTimeStamp){
                    reference.child(msgDel).removeValue();
                }

                for(String toDel : hintTimeStamp){
                    hints.child(toDel).removeValue();
                    contactWord.child(toDel).removeValue();
                }
                hints.child("NotThis").setValue(0);
                contactWord.child("Status").setValue("Change");
                hints.child("Status").setValue("Change");
                reset();
                hintTrackSelectTimestamp = "";
                guessTime = -1;

                found =true;

                String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m1 = new Message("Leader challenged hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber ,"UniversalMessageCop",date);
                reference.child(date).setValue(m1);

                date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                Message m2 = new Message("Contact successful for keyword : " + authorWord.toUpperCase(),"UniversalMessageCop",date);
                reference.child(date).setValue(m2);

                if(t.equals(word.toLowerCase())){
                    date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
                    Message m3 = new Message("Players have won the game.","UniversalMessageCop",date);
                    reference.child(date).setValue(m3);
                    progress = String.valueOf(word.length()-1);
                    gameWord.child("Progress").setValue(progress);
                }


                deleteHintTrack();
            }
        }
        if(!found) {
            String date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
            Message m1 = new Message("Leader challenged hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
            reference.child(date).setValue(m1);

            date = new SimpleDateFormat(datePattern,Locale.ENGLISH).format(new Date());
            Message m = new Message("Challenge successful, Contact failed.", "UniversalMessageCop",date);
            reference.child(date).setValue(m);

            gameWord.child("Leader").setValue(String.valueOf(currentScore+1));
            deleteHintTrack();
        }
    }

    public void playerList(View view){
        if(!playersShown) {
            findViewById(R.id.layout_message_send_leader).setVisibility(View.GONE);
            findViewById(R.id.layout_hint_track_leader).setVisibility(View.GONE);
            findViewById(R.id.guessCrossContainerLeader).setVisibility(View.INVISIBLE);
            findViewById(R.id.message_box_leader).setVisibility(View.INVISIBLE);
            findViewById(R.id.guess_cross_leader).setVisibility(View.INVISIBLE);

            playersShown = true;
            playerListView.setVisibility(View.VISIBLE);
            playerListGameCode.setVisibility(View.VISIBLE);

        }else{
            playersShown = false;
            playerListGameCode.animate().alpha(0f).setDuration(shortAnimationDuration);
            playerListView.animate().alpha(0f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    playerListView.setVisibility(View.GONE);
                    playerListGameCode.setVisibility(View.GONE);
                }
            });
            findViewById(R.id.guessCrossContainerLeader).animate().alpha(0f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.guessCrossContainerLeader).setAlpha(1f);
                    findViewById(R.id.guessCrossContainerLeader).setVisibility(View.VISIBLE);
                    findViewById(R.id.message_box_leader).setVisibility(View.VISIBLE);
                    if(!hintTrackSelectTimestamp.equals("")){
                        findViewById(R.id.guess_cross_leader).setVisibility(View.VISIBLE);
                    }
                    findViewById(R.id.layout_message_send_leader).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_hint_track_leader).setVisibility(View.VISIBLE);
                }
            });


        }

    }

    private void deleteHintTrack(){
//        if(!hintTracks.get(hintTrackSelectIdx).timeStamp.equals(hintTrackSelectTimestamp)){return;}
        hints.child(hintTrackSelectTimestamp).removeValue();
        contactWord.child(hintTrackSelectTimestamp).removeValue();
        contactWord.child("Status").setValue("Change");
        hints.child("Status").setValue("Change");
        reset();
        hintTrackSelectTimestamp = "";
    }

    private boolean checkMessage(String s){
        int temp = Integer.valueOf(progress);
        String t = word.toLowerCase();
        s = s.toLowerCase();
        if(s.length() < temp){
            return false;
        }
        return t.substring(0, temp + 1).equals(s.substring(0, temp + 1));
    }

    public void leave(View view){
        exit();
    }

    private void exit(){
        games.setValue("End");
        UIUtil.hideKeyboard(this);
        for(String msgDel : msgTimeStamp){
            reference.child(msgDel).removeValue();
        }

        for(String toDel : hintTimeStamp){
            hints.child(toDel).removeValue();
            contactWord.child(toDel).removeValue();
        }

        for(String player : playerList){
            players.child(player).removeValue();
        }
        gameWord.child("Leader").setValue("0");
        gameWord.child("Progress").setValue("0");
        gameWord.child("Word").setValue("WORD");
        hints.child("NotThis").setValue(0);
        contactWord.child("Status").setValue("Change");
        hints.child("Status").setValue("Change");
        finish();
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        reset();
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(childEventListener1);
        hints.removeEventListener(childEventListener2);
        contactWord.removeEventListener(childEventListener3);
        UIUtil.hideKeyboard(this);
//        gameWord.removeEventListener(listener1);
//        players.removeEventListener(listener3);
    }
//
    @Override
    protected void onStop() {
        super.onStop();
//        players.child(new playerName().getName()).removeValue();
        UIUtil.hideKeyboard(this);
    }
}
