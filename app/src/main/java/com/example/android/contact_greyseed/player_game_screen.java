package com.example.android.contact_greyseed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class player_game_screen extends AppCompatActivity implements HintTrackAdapter.OnHintClickListener,MessageAdapter.OnMessageClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord,players,games;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapter hintTrackAdapter;
    private ArrayList<Message> messages,messagesAdapter;
    private static ArrayList<HintTrack> hintTracks,hintAdapter;
    private String gameCode,word,progress,ccw = "";
    private int idx = 1,i=0,temp,hintTrackSelectIdx = 0, messagesSelected = 0, hintTracksSoFar = 0,shortAnimationDuration = 100;
    private ImageButton cancelButton,contactButton,contactWordSend,cancelContactBtn,contactWordSendBtn,sendButton;
    private RecyclerView playerListView;
    private ArrayList<String> playerList;
    private PlayersListViewAdapter playerListAdapter;
    HashMap<String,HashMap<String, String>> hintTrackContactData;
    HashMap<String,String> guessesHintTrack;
    TextView wordArea,playerScore,contactedWord,playerListGameCode;
    Button addButton,deleteTrackBtn,breakContactBtn;
    EditText editText;
    String msg,hintTrackSelectTimestamp = "",leader = "";
    Boolean playersShown = false,change = false,playerInTheGame = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_game_screen);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        recyclerView = findViewById(R.id.message_box);
        hintTrackView = findViewById(R.id.hintTrackView);
        wordArea = findViewById(R.id.wordArea);
        playerListView = findViewById(R.id.players_list);
        playerListView.setHasFixedSize(true);
        playerListView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));


        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cancelContactBtn = findViewById(R.id.cancelContact);
        contactWordSendBtn = findViewById(R.id.contactBtn);
        deleteTrackBtn = findViewById(R.id.deleteTrack);
        breakContactBtn = findViewById(R.id.breakContact);
        cancelButton = findViewById(R.id.imageCancel);
        sendButton = findViewById(R.id.button9);
        addButton = findViewById(R.id.addHints);
        contactButton = findViewById(R.id.imageContact);
        contactWordSend = findViewById(R.id.contactBtn);
        editText = findViewById(R.id.hint_message);
        playerScore = findViewById(R.id.player_score);
        contactedWord = findViewById(R.id.contacted_word);
        playerListGameCode = findViewById(R.id.player_list_game_code);

        hintTrackView.setHasFixedSize(true);
        hintTrackView.setLayoutManager(new LinearLayoutManager(this,LinearLayout.HORIZONTAL,false));

        messages = new ArrayList<>();
        messagesAdapter = new ArrayList<>();
        hintTracks = new ArrayList<>();
        hintAdapter = new ArrayList<>();
        hintTrackContactData = new HashMap<>();
        guessesHintTrack = new HashMap<>();
        playerList = new ArrayList<>();
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

        adapter = new MessageAdapter(messagesAdapter,player_game_screen.this);
        recyclerView.setAdapter(adapter);

        hintTrackAdapter = new HintTrackAdapter(hintTracks, this);
        hintTrackView.setAdapter(hintTrackAdapter);

        reference.addChildEventListener(new ChildEventListener() {
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
                }else if(temp.getSender().equals(leader)){
                    temp.side = 4;
                }
                else{
                    temp.side = 2;
                }
                messagesAdapter.add(temp);
                adapter.notifyItemInserted(messagesAdapter.size()-1);
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
                messagesAdapter.sort(new Comparator<Message>() {
                    @Override
                    public int compare(Message message, Message t1) {
                        return message.timeStamp.compareTo(t1.timeStamp);
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)return;
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("a"))return;
                messagesAdapter.clear();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        hints.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("NotThis")){
                    hintTracksSoFar = Objects.requireNonNull(dataSnapshot).getValue(Integer.class);
                    return;
                }
                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("Status"))return;
//                ArrayList<Message> msg_db = new ArrayList<>();
//                ArrayList<Integer> idx_db = new ArrayList<>();
//                ArrayList<String> temp = (ArrayList<String>) Objects.requireNonNull(dataSnapshot).getValue();
                Object temp = dataSnapshot.getValue(Object.class);
                String json = new Gson().toJson(temp);
                HintTrack ht = new Gson().fromJson(json,HintTrack.class);

//                if(Objects.requireNonNull(temp).isEmpty())return;
//                if(temp.size()>0){
//                    for(int j=2;j<temp.size();j++){
//                        int t = Integer.valueOf(temp.get(j));
//                        if(t >=messagesAdapter.size())continue;
//                        msg_db.add(messagesAdapter.get(t));
//                        idx_db.add(t);
//                    }
//                    int num = Integer.valueOf(temp.get(2));
                hintTracks.add(ht);
                hintTrackAdapter.notifyDataSetChanged();
                hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
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
                        hintTrackContactData.remove(hint_idx);
                        contactWord.child(dataSnapshot.getKey()).removeValue();
                        onClick(hintTrackSelectIdx);
                        return;
                    }
                }
                if(hint_idx==-1)return;
                hintTracks.remove(hint_idx);
                hintTrackAdapter.notifyItemRemoved(hint_idx);
//                guessesHintTrack.remove(dataSnapshot.getKey());
                hintTrackContactData.remove(hint_idx);
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
//
        contactWord.addChildEventListener(new ChildEventListener() {
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

//        players.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if(dataSnapshot.getValue()==null)return;
//                if(Objects.requireNonNull(dataSnapshot.getKey()).equals("NotThis") && Objects.requireNonNull(dataSnapshot.getValue()).equals("NotThis"))return;
//                String player = dataSnapshot.getKey();
//                if(Objects.requireNonNull(dataSnapshot.getValue()).equals("HOST")){
//                    playerListAdapter.host = player;
//                    leader = player;
//
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

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while(dataSnapshot.getValue() == null){}
                messagesAdapter.clear();
                for (DataSnapshot it : dataSnapshot.getChildren()) {

                    HashMap<String,String> data = (HashMap<String, String>) it.getValue();
                    if(data == null)return;

                    Message temp = new Message(data.get("msg"),data.get("sender"),data.get("timeStamp"));
                    if(temp.getSender().equals(new playerName().getName())){
                        temp.side = 1;
                    }else if(temp.getSender().equals("UniversalMessageCop")){
                        temp.side = 3;
                    }else if(temp.getSender().equals(leader)){
                        temp.side = 4;
                    }
                    else{
                        temp.side = 2;
                    }
                    if(it.getKey().equals("0")){
                        leader = temp.getSender();
                        continue;
                    }

                    messagesAdapter.add(temp);
                    messagesAdapter.sort(new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            return message.timeStamp.compareTo(t1.timeStamp);
                        }
                    });
                    adapter = new MessageAdapter(messagesAdapter,player_game_screen.this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        hints.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                if(dataSnapshot.getValue() != null){}
//                ArrayList<String> temp ;
//
//                hintTracks.clear();
//                for(DataSnapshot it : dataSnapshot.getChildren()){
//                    ArrayList<Message> msg_db = new ArrayList<>();
//                    ArrayList<Integer> idx_db = new ArrayList<>();
//                    if( !dataSnapshot.hasChild(it.getKey())) return;
//
//                    if(it.getKey().equals("NotThis")){
//                        hintTracksSoFar = Objects.requireNonNull(dataSnapshot).child(it.getKey()).getValue(Integer.class);
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
//                        int num = Integer.valueOf(temp.get(2));
//                        hintTracks.add(new HintTrack(idx_db,msg_db,temp.get(0),num,temp.get(1)));
//                        hintTrackAdapter.notifyDataSetChanged();
//                        hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        gameWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,String> dt = (HashMap<String, String>)dataSnapshot.getValue();
                if(dt!=null) {
                    word = dt.get("Word");
                    progress = dt.get("Progress");
                    word = word.toUpperCase();
                    String score = dt.get("Leader");
                    if(score == null)return;
                    int s = Integer.valueOf(score);
                    setLife(s);
                    if(Integer.parseInt(progress) + 1 <= word.length())
                        wordArea.setText(word.substring(0,Integer.parseInt(progress)+1));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
////                    hintTrackAdapter = new HintTrackAdapter(hintTracks, player_game_screen.this);
////                    hintTrackView.setAdapter(hintTrackAdapter);
//                }
//                guessCross();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        games.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class).equals("End")){
                    Intent intent = new Intent(player_game_screen.this,MainActivity.class);
                    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        players.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)return;
                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
                if(data==null || data.keySet().isEmpty())return;
                playerList.clear();
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
                    playerName.gameCode = "";
                    finishActivity(1);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void setLife(int n){
        if(n>0){
            ((ImageView)findViewById(R.id.heart5)).setImageResource(R.drawable.ic_un_heart);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.heart4)).setImageResource(R.drawable.ic_un_heart);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.heart3)).setImageResource(R.drawable.ic_un_heart);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.heart2)).setImageResource(R.drawable.ic_un_heart);
            n--;
        }
        if(n>0){
            ((ImageView)findViewById(R.id.heart1)).setImageResource(R.drawable.ic_un_heart);
            n--;
        }
    }

    public void send_msg(View view){
        if(view.getVisibility() == View.INVISIBLE) return;
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
                Toast.makeText(this, "Not more than 3 words", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        if(reenter == 0){
            String date= new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
            Message  temp = new Message(msg,new playerName().getName(),date);
            editText.setText("");
            reference.child(date).setValue(temp);
        }
        editText.setHint("Type a hint..");

    }

    public void getHintContactWord(View view){
        final String temp = editText.getText().toString().trim().toLowerCase();
        if(!checkMessage(temp)){
            editText.setText("");
            editText.setHint("Check the game word.");return;
        }
        if(temp.contains(" ") || temp.equals("")){return;}
            if(adapter.getSelectedCount() > 0) {

//                final ArrayList<String> hintMsgIndex = new ArrayList<>();
//                hintMsgIndex.add(new playerName().getName());
                final String date= new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
//                hintMsgIndex.add(date);
                ArrayList<Integer> idxMsg = new ArrayList<>();
                ++hintTracksSoFar;
//                hintMsgIndex.add(String.valueOf(hintTracksSoFar));
                hints.child("NotThis").setValue(hintTracksSoFar);

                for (int i = 0; i < messagesAdapter.size(); i++) {
                    if (messagesAdapter.get(i).isSelected()) {
//                        hintMsgIndex.add(String.valueOf(i));
                        idxMsg.add(i);
                        messagesAdapter.get(i).toggleSelect();
                    }
                }
                HintTrack val = new HintTrack(idxMsg,new playerName().getName(),hintTracksSoFar,date);
                hints.child(date).setValue(val);
                contactWord.child(date).child(new playerName().getName()).setValue(temp.toLowerCase());
                contactWord.child(date).child("Guesses").setValue("0");
            }
            sendButton.setVisibility(View.VISIBLE);
            contactWordSendBtn.setVisibility(View.GONE);
            cancelContactBtn.setVisibility(View.GONE);
            editText.setText("");
            editText.setHint("Type a hint..");



    }

    public void addHints(View view){
        addButton.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.INVISIBLE);
        hintTrackView.setVisibility(View.VISIBLE);
        contactWordSendBtn.setVisibility(View.VISIBLE);
        cancelContactBtn.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Enter your contact word..");
        adapter.state =0;
        adapter.notifyDataSetChanged();
    }

    public void reset(){
        addButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        contactButton.setVisibility(View.GONE);
        cancelContactBtn.setVisibility(View.GONE);
        deleteTrackBtn.setVisibility(View.GONE);
        breakContactBtn.setVisibility(View.GONE);
        contactButton.setVisibility(View.GONE);
        contactWordSendBtn.setVisibility(View.GONE);
        cancelContactBtn.setVisibility(View.GONE);
        contactedWord.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        hintTrackView.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Type a hint..");
        hintTrackSelectTimestamp = "";
        change = false;
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
            findViewById(R.id.imageView).animate().alpha(0).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                    findViewById(R.id.imageView).animate().alpha(1);
                    findViewById(R.id.layout_message_send).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_hint_track).setVisibility(View.VISIBLE);
                    findViewById(R.id.message_box).setVisibility(View.VISIBLE);
                    findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                }
            });
        }
        guessCross();
    }

    public void resetAdapter(View view){
        reset();
        hintTrackSelectTimestamp = "";
        for(int i=0;i<messagesAdapter.size();i++){
            if(messagesAdapter.get(i).isSelected()){
                messagesAdapter.get(i).toggleSelect();
            }
        }
    }

    private void guessCross(){
        if(hintTrackSelectTimestamp == "" || hintTrackSelectIdx == -1){
            ((ImageView)findViewById(R.id.cross1)).setImageResource(R.drawable.ic_unclicked_cross);
            ((ImageView)findViewById(R.id.cross2)).setImageResource(R.drawable.ic_unclicked_cross);
            ((ImageView)findViewById(R.id.cross3)).setImageResource(R.drawable.ic_unclicked_cross);
            return;
        }
        if(hintTrackContactData.get(hintTrackSelectTimestamp)== null ||  hintTrackContactData.get(hintTrackSelectTimestamp).get("Guesses")==null ){return;}
        int g = Integer.parseInt(hintTrackContactData.get(hintTrackSelectTimestamp).get("Guesses"));
        if(g>0){
            ((ImageView)findViewById(R.id.cross1)).setImageResource(R.drawable.ic_guess_cross);
            g--;
        }
        if(g>0){
            ((ImageView)findViewById(R.id.cross2)).setImageResource(R.drawable.ic_guess_cross);
            g--;
        }
        if(g>0){
            ((ImageView)findViewById(R.id.cross3)).setImageResource(R.drawable.ic_guess_cross);
            g--;
        }
    }

    @Override
    public void onClick(int pos) {
        reset();
        hintTrackSelectTimestamp="";
        guessCross();
        HintTrack ht = hintTracks.get(pos);
        hintTrackSelectIdx = pos;
        ht.toggleSelected();
        hintTrackSelectTimestamp = ht.timeStamp;
        hintTrackAdapter.notifyItemChanged(pos);

        ArrayList<Message> msg = new ArrayList<>();
        for(int i : ht.list){
            msg.add(messagesAdapter.get(i));
        }
        MessageAdapter tempHintAdapter = new MessageAdapter(msg,player_game_screen.this);
        guessCross();
        recyclerView.setAdapter(tempHintAdapter);

        addButton.setVisibility(View.GONE);


        if(ht.author.equals(new playerName().getName())){
            String presentWord = Objects.requireNonNull(hintTrackContactData.get(hintTrackSelectTimestamp)).get(new playerName().getName());
            if(presentWord == null || presentWord.isEmpty())return;
            presentWord = presentWord.toUpperCase();
            contactedWord.setVisibility(View.VISIBLE);
            contactedWord.setText(Html.fromHtml("<b>CONTACT : </b><br>"+presentWord, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));
            sendButton.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.INVISIBLE);
            deleteTrackBtn.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
        else if(Objects.requireNonNull(hintTrackContactData.get(hintTrackSelectTimestamp)).containsKey(new playerName().getName())){
            String presentWord = hintTrackContactData.get(hintTrackSelectTimestamp).get(new playerName().getName());
            if(presentWord.isEmpty())return;
            presentWord = presentWord.toUpperCase();
            contactedWord.setVisibility(View.VISIBLE);
            contactedWord.setText(Html.fromHtml("<b>CONTACT : </b><br>"+presentWord, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));
            sendButton.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.INVISIBLE);
            breakContactBtn.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
        else {

            editText.setVisibility(View.VISIBLE);
            editText.setText("");
            editText.setHint("Enter your Contact word...");
            sendButton.setVisibility(View.INVISIBLE);
            deleteTrackBtn.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
            contactButton.setVisibility(View.VISIBLE);
        }

//        hintTrackView.setVisibility(View.GONE);
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
                addButton.setVisibility(View.VISIBLE);
            }else{
                adapter.state = 0;
                addButton.setVisibility(View.GONE);
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
            addButton.setVisibility(View.GONE);
            sendButton.setVisibility(View.VISIBLE);
            editText.setVisibility(View.VISIBLE);
            hintTrackView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    public void changeContactWord(View view){
        contactedWord.setVisibility(View.GONE);
        breakContactBtn.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("New Contact word...");
        sendButton.setVisibility(View.INVISIBLE);
        deleteTrackBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.VISIBLE);
        contactButton.setVisibility(View.VISIBLE);
        change =true;
    }

    public void makeContact(View view){

        final String temp = editText.getText().toString().trim().toLowerCase();
        if(!checkMessage(temp)){
            editText.setText("");
            editText.setHint("Check the game word");
            return;
        }
        if(temp.equals("") || temp.contains(" "))return;
        contactWord.child(hintTrackSelectTimestamp).child(new playerName().getName()).setValue(temp);
        if(!change){
            Message m = new Message("A contact has been initiated on hint-track #" +hintTracks.get(hintTrackSelectIdx).hintTrackNumber, "UniversalMessageCop",new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date()));
            reference.child(m.timeStamp).setValue(m);
        }
        editText.setText("");
        resetAdapter(view);
    }

    public void breakContact(View view){
        contactWord.child(hintTrackSelectTimestamp).child(new playerName().getName()).removeValue();
        resetAdapter(view);
    }

    public void deleteHintTrack(View view){
        if(hintTrackSelectIdx > hintTracks.size() ||  !hintTracks.get(hintTrackSelectIdx).timeStamp.equals(hintTrackSelectTimestamp)){resetAdapter(view);return;}
//        HintTrack ht = hintAdapter.get(hintTrackSelectIdx);
        hints.child(hintTrackSelectTimestamp).removeValue();
        contactWord.child(hintTrackSelectTimestamp).removeValue();
        contactWord.child("Status").setValue("Change");
        hints.child("Status").setValue("Change");
        resetAdapter(view);
    }

    public void playerList(View view){
        if(!playersShown) {
            findViewById(R.id.layout_message_send).setVisibility(View.GONE);
            findViewById(R.id.layout_hint_track).setVisibility(View.GONE);
            findViewById(R.id.imageView).setVisibility(View.GONE);
            findViewById(R.id.message_box).setVisibility(View.GONE);
            findViewById(R.id.linearLayout).setVisibility(View.GONE);
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
            findViewById(R.id.imageView).animate().alpha(0).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_message_send).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_hint_track).setVisibility(View.VISIBLE);
                    findViewById(R.id.message_box).setVisibility(View.VISIBLE);
                    findViewById(R.id.linearLayout).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private boolean checkMessage(String s){
        int temp = Integer.valueOf(progress);
        String t = word.toLowerCase();
        s = s.toLowerCase();
        if(s.length() < temp){
            return false;
        }
        if(t.substring(0,temp+1).equals(s.substring(0,temp+1))){
            return true;
        }
        return false;
    }

    public void leave(View view){
        players.child(new playerName().getName()).removeValue();
        new playerName().setGameCode("");
        finish();
//        finishActivity(1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        reset();
        return false;
    }
}
