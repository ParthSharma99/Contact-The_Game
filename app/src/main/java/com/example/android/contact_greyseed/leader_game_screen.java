package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Set;

public class leader_game_screen extends AppCompatActivity implements HintTrackAdapter.OnHintClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord,players,games;
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
    ArrayList<HashMap<String,String>> hintTrackContactData;
    TextView wordArea,makeContactMsg;
    Button sendButton,guessBtn,challengeBtn;
    EditText editText;
    String msg;
    boolean challenge = false;

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
        guessBtn = findViewById(R.id.guessContact);
        challengeBtn = findViewById(R.id.challengeBtn);
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
        players = FirebaseDatabase.getInstance().getReference("Players").child(gameCode);
        games = FirebaseDatabase.getInstance().getReference("Games").child(gameCode);


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
                    if(messages.get(i).getSender().equals(new playerName().getName())){
                        messages.get(i).side = 1;
                    }else if(messages.get(i).getSender().equals("UniversalMessageCop")){
                        messages.get(i).side = 3;
                    }else{
                        messages.get(i).side = 2;
                    }
                }
                for(i = messagesAdapter.size()+1;i<messages.size();i++){
                    messagesAdapter.add(messages.get(i));
                }
//                adapter = new MessageAdapter(messagesAdapter,leader_game_screen.this);
//                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
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
                        hintTracks.add(new HintTrack(idx_db,msg_db,temp.get(0)));
                        hintTrackAdapter.notifyDataSetChanged();
                        hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
//                        if(i>0 && i<hintTracks.size()){
//                            hintAdapter.add(hintTracks.get(i));
//                            hintTrackAdapter.notifyDataSetChanged();
//                            hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
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
                    String source = "<b>" + word.substring(0,Integer.parseInt(progress)+1) + "</b>" + word.substring(Integer.parseInt(progress)+1);
                    wordArea.setText(Html.fromHtml(source,Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        contactWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(int j =1;j<dataSnapshot.getChildrenCount();j++) {
                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.child(String.valueOf(j)).getValue();
                    if(data == null || data.isEmpty())return;
                    if(j-1 > hintTracks.size())return;
                    hintTracks.get(j-1).count = dataSnapshot.child(String.valueOf(j)).getChildrenCount();
                    hintTrackContactData.add(data);
                    hintTrackAdapter = new HintTrackAdapter(hintTracks, leader_game_screen.this);
                    hintTrackView.setAdapter(hintTrackAdapter);
//                    for (int i = 0; i < size; i++) {
//                        int cnt = 0;
////                        HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.child(String.valueOf(j)).child(String.valueOf(i)).getValue();
//                        for (String t : data.keySet()) {
//                            getWords.add(data.get(t));
//                            getPlayer.add(t);
//                            cnt++;
//                        }
//
//                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        games.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class).equals("End")){
                    Intent intent = new Intent(leader_game_screen.this,MainActivity.class);
                    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
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
        guessBtn.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        makeContactMsg.setVisibility(View.VISIBLE);
    }

    public void cancel(View view){
        sendButton.setVisibility(View.VISIBLE);
        cancelGuess.setVisibility(View.GONE);
        sendGuess.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        guessBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        challengeBtn.setVisibility(View.GONE);
        editText.setText("");
        editText.setHint("Type a hint..");
    }

    public void guess(View view){

        editText.setText("");
        editText.setHint("Enter guess word..");
        sendButton.setVisibility(View.INVISIBLE);
        cancelGuess.setVisibility(View.VISIBLE);
        sendGuess.setVisibility(View.VISIBLE);
        challengeBtn.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.GONE);

    }

    public void setCancelGuess(View view){
        editText.setText("");
        editText.setHint("Type a hint..");
        sendButton.setVisibility(View.VISIBLE);
        cancelGuess.setVisibility(View.GONE);
        sendGuess.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        guessBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        challengeBtn.setVisibility(View.GONE);
    }

    public void setSendGuess(View view){

        if(challenge){

            String author = hintTracks.get(hintTrackSelectIdx).author;

            String authorWord = "";

            if(hintTrackContactData.get(hintTrackSelectIdx).containsKey(author)){
                authorWord = hintTrackContactData.get(hintTrackSelectIdx).get(author);
            }else{
                return;
            }
            authorWord = authorWord.toLowerCase();
            boolean found = false;
            for(String s :hintTrackContactData.get(hintTrackSelectIdx).keySet()){
                if(s.equals(author))continue;
                String t = hintTrackContactData.get(hintTrackSelectIdx).get(s);
                if(t.toLowerCase().equals(authorWord)){
                    guessTime = 0;
                    found =true;
                    int temp = Integer.valueOf(progress);
                    progress = String.valueOf(temp+1);
                    gameWord.child("Progress").setValue(progress);
                    for(int i = messagesAdapter.size();i>0;i--){
                        reference.child(String.valueOf(i)).removeValue();
                    }
                    for(int i = hintTracks.size();i>0;i--){
                        hints.child(String.valueOf(i)).removeValue();
                        contactWord.child(String.valueOf(i)).removeValue();
                    }
                    setCancelGuess(view);
                    Message m1 = new Message("Leader challenged hint-track#" + String.valueOf(hintTrackSelectIdx+1),"UniversalMessageCop");
                    Message m2 = new Message("Contact successful for keyword : " + authorWord.toUpperCase(),"UniversalMessageCop");
                    reference.child("1").setValue(m1);
                    reference.child("2").setValue(m2);
                }
            }
            if(!found) {
                Message m = new Message("No valid Contact.", "UniversalMessageCop");
                reference.child(String.valueOf(messages.size())).setValue(m);
            }
            challenge = false;
            return;
        }

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
                contactWord.child(String.valueOf(i)).removeValue();
            }
            setCancelGuess(view);
            Message m = new Message(new playerName().getName() + " " + "could not guess the word.","UniversalMessageCop");
            reference.child("1").setValue(m);
            return;


        }else {
            if(hintTracks.get(hintTrackSelectIdx).count <2){
                Toast.makeText(this, "Hint track has no contact.", Toast.LENGTH_SHORT).show();
                return;
            }

            String temp = editText.getText().toString().trim().toLowerCase();
            String author = hintTracks.get(hintTrackSelectIdx).author;

            String authorWord = "";

            if(hintTrackContactData.get(hintTrackSelectIdx).containsKey(author)){
                authorWord = hintTrackContactData.get(hintTrackSelectIdx).get(author);
            }else{
                return;
            }

            if (temp.contains(" ")) {
                return;
            }
            if (!checkMessage(temp)) {
                Toast.makeText(this, "Check the Game word.", Toast.LENGTH_SHORT).show();
                return;
            }
            authorWord = authorWord.toLowerCase();
            Toast.makeText(this, author, Toast.LENGTH_SHORT).show();
            if(temp.equals(authorWord)){
                Toast.makeText(this, "You Guessed it!", Toast.LENGTH_SHORT).show();
                Message m = new Message("Leader guessed correctly for hint-track#" + String.valueOf(hintTrackSelectIdx+1) , "UniversalMessageCop");
                reference.child(String.valueOf(messages.size())).setValue(m);
                setCancelGuess(view);
            }else{
                Toast.makeText(this, "Sorry", Toast.LENGTH_SHORT).show();
                Message m = new Message("Leader guessed correctly for hint-track#" + String.valueOf(hintTrackSelectIdx+1) , "UniversalMessageCop");
                reference.child(String.valueOf(messages.size())).setValue(m);
                guessTime++;
            }
        }
    }

    public void setChallengeBtn(View view){
        if(hintTracks.get(hintTrackSelectIdx).count <2){
            Toast.makeText(this, "Hint track has no contact.", Toast.LENGTH_SHORT).show();
            return;
        }
        editText.setText("");
        editText.setHint("Are you sure ?");
        challenge = true;
        Message m = new Message("Leader challenged hint-track#" + String.valueOf(hintTrackSelectIdx+1) , "UniversalMessageCop");
        reference.child(String.valueOf(messages.size())).setValue(m);

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
        games.setValue("End");
    }

}
