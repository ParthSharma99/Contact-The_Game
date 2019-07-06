package com.example.android.contact_greyseed;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Set;

public class leader_game_screen extends AppCompatActivity implements HintTrackAdapter.OnHintClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord;
    private static RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapter hintTrackAdapter;
    private ArrayList<Message> messages,messagesAdapter;
    private static ArrayList<HintTrack> hintTracks,hintAdapter;
    private String gameCode,word,progress;
    private int idx = 1,i=0,temp,hintTrackSelectIdx = 0,guessTime = 0,gameWordIdx = 0;
    private ImageButton cancelButton,cancelGuess,sendGuess;
    private ArrayList<HashMap<String,String>> database;
    private Set<String> contactCommonWords;
    ArrayList<ArrayList<String>> hintTrackContactData;
    TextView wordArea,makeContactMsg;
    Button sendButton,challengeBtn;
    EditText editText;
    String msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_game_screen);

        recyclerView = findViewById(R.id.message_box_leader);
        hintTrackView = findViewById(R.id.hintTrackView_leader);
        wordArea = findViewById(R.id.wordArea_leader);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hintTrackContactData = new ArrayList<>();

        sendButton = findViewById(R.id.button9_leader);
        challengeBtn = findViewById(R.id.challengeContact);
        makeContactMsg = findViewById(R.id.makeContactMsg_leader);
        editText = findViewById(R.id.hint_message_leader);
        cancelButton = findViewById(R.id.cancelHintSelect);
        cancelGuess = findViewById(R.id.cancelGuess);
        sendGuess = findViewById(R.id.sendGuess);

        hintTrackView.setHasFixedSize(true);
        hintTrackView.setLayoutManager(new LinearLayoutManager(this,LinearLayout.HORIZONTAL,false));

        messages = new ArrayList<>();
        messagesAdapter = new ArrayList<>();
        hintTracks = new ArrayList<>();
        hintAdapter = new ArrayList<>();

        database = new ArrayList<>();
        gameCode = new playerName().getGameCode();
        if(gameCode == ""){
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
            finish();
        }

        reference = FirebaseDatabase.getInstance().getReference("GameChat").child(gameCode);
        hints = FirebaseDatabase.getInstance().getReference("Hints").child(gameCode);
        gameWord = FirebaseDatabase.getInstance().getReference("GameWord").child(gameCode);
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(gameCode);


        adapter = new MessageAdapter(messagesAdapter,leader_game_screen.this);
        recyclerView.setAdapter(adapter);

        hintTrackAdapter = new HintTrackAdapter(hintTracks,this);
        hintTrackView.setAdapter(hintTrackAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while(dataSnapshot.getValue() == null){}
                database = (ArrayList<HashMap<String,String>>)dataSnapshot.getValue();
//                Toast.makeText(player_game_screen.this, database.toString(), Toast.LENGTH_LONG).show();
                messages.clear();
                messagesAdapter.clear();
                idx = database.size();
                if(database.isEmpty())return;
                for(i = 0;i<database.size();i++){
                    messages.add(new Message(database.get(i).get("msg"),database.get(i).get("sender")));
                }
                for(i = 1;i<messages.size();i++){
                    messagesAdapter.add(messages.get(i));
                }
                adapter = new MessageAdapter(messagesAdapter,leader_game_screen.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        hints.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null){}
                ArrayList<String> temp ;

                hintTracks.clear();
                hintAdapter.clear();
                for(int i=1;i<dataSnapshot.getChildrenCount();i++){
                    ArrayList<Message> msg_db = new ArrayList<>();
                    ArrayList<Integer> idx_db = new ArrayList<>();
                    temp = (ArrayList<String>) Objects.requireNonNull(dataSnapshot).child(String.valueOf(i)).getValue();
                    assert temp != null;
                    if(temp.size()>0){
                        for(int j=1;j<temp.size();j++){
                            int t = Integer.valueOf(temp.get(j));
                            if(t >=messages.size())continue;
                            msg_db.add(messages.get(t));
                            idx_db.add(t);
                        }

//                        Toast.makeText(player_game_screen.this, idx_db.toString(), Toast.LENGTH_SHORT).show();
                        hintTracks.add(new HintTrack(idx_db,msg_db));
                        hintTrackAdapter.notifyDataSetChanged();
//                        if(i>0){
//                            hintAdapter.add(hintTracks.get(i));
//                            hintTrackAdapter.notifyDataSetChanged();
//                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        gameWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,String> dt = (HashMap<String, String>)dataSnapshot.getValue();
                if(dt!=null) {
                    word = dt.get("Word");
                    progress = dt.get("Progress");
                    word = word.toUpperCase();

                    wordArea.setText(word.substring(0,Integer.parseInt(progress)+1));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        contactWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(int j =0;j<dataSnapshot.getChildrenCount();j++) {
                    ArrayList<String> getWords = new ArrayList<>();
                    long size = dataSnapshot.child(String.valueOf(j)).getChildrenCount();
                    for (int i = 0; i < size; i++) {
                        HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.child(String.valueOf(j)).child(String.valueOf(i)).getValue();
                        for (String t : data.keySet()) {
                            getWords.add(data.get(t.toLowerCase()));
                        }
                    }
                    HashMap<String, Integer> commonWords = new HashMap<>();
                    for (int i = 0; i < getWords.size(); i++) {
                        if (commonWords.containsKey(getWords.get(i))) {
                            int s = commonWords.get(getWords.get(i));
                            commonWords.remove(getWords.get(i));
                            commonWords.put(getWords.get(i), s + 1);
                        } else {
                            commonWords.put(getWords.get(i), 1);
                        }
                    }

                    hintTrackContactData.add(j,new ArrayList<>(commonWords.keySet()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
                Toast.makeText(this, "Not more than 3 words", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        if(reenter == 0){
            messages.add(new Message(msg,new playerName().getName()));
            editText.setText(" ");
            reference.setValue(messages);
//            idx++;
        }

        editText.setHint("Type a hint..");

    }

    @Override
    public void onClick(int pos) {
        hintTrackSelectIdx = pos;
        HintTrack ht = hintTracks.get(pos);
        temp = ht.getNext();
        if(temp >= messagesAdapter.size()){
            return;
        }
        MessageAdapter tempHintAdapter = new MessageAdapter(ht.track,leader_game_screen.this);
        recyclerView.setAdapter(tempHintAdapter);
//        hintTrackView.setVisibility(View.GONE);
        challengeBtn.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        makeContactMsg.setVisibility(View.VISIBLE);
//        recyclerView.findViewHolderForAdapterPosition(temp).itemView.animate().alpha(0.3f).setDuration(400);
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                recyclerView.findViewHolderForAdapterPosition(temp).itemView.animate().alpha(1).setDuration(400);
//            }
//        }, 500);

    }

    public void cancel(View view){
        recyclerView.setAdapter(adapter);
//        hintTrackView.setVisibility(View.VISIBLE);
        challengeBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        editText.setHint("Type a hint..");
    }

    public void guess(View view){

        Toast.makeText(this, " JHKJ ", Toast.LENGTH_SHORT).show();
        editText.setText("");
        editText.setHint("Enter guess word..");
        sendButton.setVisibility(View.INVISIBLE);
        cancelGuess.setVisibility(View.VISIBLE);
        sendGuess.setVisibility(View.VISIBLE);

    }

    public void setSendGuess(View view){
        if(guessTime == 2){
            guessTime = 0;
            int temp = Integer.valueOf(progress);
            progress = String.valueOf(temp+1);
            gameWord.child("Progress").setValue(progress);
            for(int i = messagesAdapter.size();i>0;i--){
                reference.child(String.valueOf(i)).removeValue();
            }
            for(int i = hintTracks.size();i>0;i--){
                hints.child(String.valueOf(i)).removeValue();
            }
//            for(int i = hintTrackContactData.size()-1;i>0;i--){
//                contactWord.child(gameCode).child(String.valueOf(i)).removeValue();
//            }

        }
            String temp = editText.getText().toString().trim().toLowerCase();
            if(temp.contains(" ")){
                return;
            }
            if(!checkMessage(temp)){
                Toast.makeText(this, "Check the Game word.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(hintTrackContactData.get(hintTrackSelectIdx).contains(temp)){
                Toast.makeText(this, "You Guessed it!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Sorry", Toast.LENGTH_SHORT).show();
                guessTime++;
            }

    }

    private boolean checkMessage(String s){
        int temp = Integer.valueOf(progress);
        String t = word.toLowerCase();
        s = s.toLowerCase();
        if(s.length() < temp)return false;
        for (int i = 0; i < temp; i++) {
            if(s.charAt(i) != t.charAt(i)){
                return false;
            }
        }
        return true;
    }


}
