package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class leader_game_screen extends AppCompatActivity implements HintTrackAdapter.OnHintClickListener,MessageAdapter.OnMessageClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord,players,games;
    private static RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapter hintTrackAdapter;
    private ArrayList<Message> messages,messagesAdapter;
    private static ArrayList<HintTrack> hintTracks,hintAdapter;
    private String gameCode,word,progress;
    private int idx = 1,i=0,temp,hintTrackSelectIdx = 0,guessTime = 0,messagesSelected = 0, hintTracksSoFar = 0;
    private ImageButton cancelButton,cancelGuess,sendGuess,sendButton;
    private ArrayList<HashMap<String,String>> database;
    private ListView playerListView;
    private ArrayAdapter<String> playerListAdapter;
    ArrayList<HashMap<String,String>> hintTrackContactData;
    ArrayList<String> msgTimeStamp, hintTimeStamp,playerList;
    HashMap<String,String> guessesHintTrack;
    TextView wordArea;
    Button guessBtn,challengeBtn;
    EditText editText;
    String msg,hintTrackSelectTimestamp="";
    int currentScore = 0;
    float prevYWordArea = -1;
    boolean playersShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_game_screen);

        recyclerView = findViewById(R.id.message_box_leader);
        hintTrackView = findViewById(R.id.hintTrackView_leader);
        wordArea = findViewById(R.id.wordArea_leader);
        playerListView = findViewById(R.id.players);


        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hintTrackContactData = new ArrayList<>();

        sendButton = findViewById(R.id.button9_leader);
        guessBtn = findViewById(R.id.guessContact);
        challengeBtn = findViewById(R.id.challengeBtn);
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
        msgTimeStamp = new ArrayList<>();
        hintTimeStamp = new ArrayList<>();
        guessesHintTrack = new HashMap<>();
        playerList = new ArrayList<>();
        playerList.add(new playerName().getName() + "\t" + "(HOST) (Leader)");
        playerListAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,playerList);
        playerListView.setAdapter(playerListAdapter);

        prevYWordArea = wordArea.getY();

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
                messagesAdapter.clear();
                msgTimeStamp.clear();
                for (DataSnapshot it : dataSnapshot.getChildren()) {

                    HashMap<String,String> data = (HashMap<String, String>) it.getValue();
                    if(data == null)return;

                    Message temp = new Message(data.get("msg"),data.get("sender"),data.get("timeStamp"));
                    if(temp.getSender().equals(new playerName().getName())){
                        temp.side = 1;
                    }else if(temp.getSender().equals("UniversalMessageCop")){
                        temp.side = 3;
                    }else{
                        temp.side = 2;
                    }
                    if(it.getKey().equals("0")){
                        continue;
                    }

                    messagesAdapter.add(temp);
                    messagesAdapter.sort(new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            return message.timeStamp.compareTo(t1.timeStamp);
                        }
                    });
                    msgTimeStamp.add(it.getKey());
                    adapter = new MessageAdapter(messagesAdapter,leader_game_screen.this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                }
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
                hintTimeStamp.clear();
                for(DataSnapshot it : dataSnapshot.getChildren()){
                    ArrayList<Message> msg_db = new ArrayList<>();
                    ArrayList<Integer> idx_db = new ArrayList<>();
                    if( !dataSnapshot.hasChild(it.getKey())) return;

                    if(it.getKey().equals("NotThis")){
//                        hintTracksSoFar = Objects.requireNonNull(dataSnapshot).child(it.getKey()).getValue(Integer.class);
                        continue;
                    }
                    if(it.getKey().equals("Status")){
                        hints.child("Status").setValue("No change");
                        hintTrackAdapter.notifyDataSetChanged();
                        continue;
                    }

                    temp = (ArrayList<String>) it.getValue();
                    if(temp.isEmpty())return;
                    if(temp.size()>0){
                        for(int j=3;j<temp.size();j++){
                            int t = Integer.valueOf(temp.get(j));
                            if(t >=messagesAdapter.size())continue;
                            msg_db.add(messagesAdapter.get(t));
                            idx_db.add(t);
                        }

//                        Toast.makeText(player_game_screen.this, idx_db.toString(), Toast.LENGTH_SHORT).show();
                        int num = Integer.valueOf(temp.get(2));
                        hintTracks.add(new HintTrack(idx_db,msg_db,temp.get(0),num,temp.get(1)));
                        hintTimeStamp.add(it.getKey());
                        hintTrackAdapter.notifyDataSetChanged();
                        hintTrackView.smoothScrollToPosition(hintTrackAdapter.getItemCount());
//                        if(i>0){
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
                    if (Integer.parseInt(progress) <= word.length()){
                        String source = "<b>" + word.substring(0, Integer.parseInt(progress) + 1) + "</b>" + word.substring(Integer.parseInt(progress) + 1);
                        wordArea.setText(Html.fromHtml(source, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));
                    }
                    String score = dt.get("Leader");
                    currentScore = Integer.valueOf(score);
                    setTrophy(currentScore);
                    if(currentScore == 5){
                        String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
                        Message m1 = new Message("Leader has Won the Game." , "UniversalMessageCop",date);
                        gameWord.child("Progress").setValue(String.valueOf(word.length()));
                        reference.child(date).setValue(m1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        contactWord.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hintTrackContactData.clear();
                guessesHintTrack.clear();
                for(DataSnapshot it : dataSnapshot.getChildren()) {
                    if(it.getKey().equals("NotThis") || it.getKey().equals("Status")){continue;}
                    HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.child(it.getKey()).getValue();
                    if(data == null || data.isEmpty())return;
                    if(data.containsKey("Guesses")){
                        guessesHintTrack.put(it.getKey(),data.get("Guesses"));
                        data.remove("Guesses");
                    }
                    String t = it.getKey();
                    for(int i=0;i<hintTracks.size();i++){
                        if(hintTracks.get(i).timeStamp.equals(t)){
                            hintTracks.get(i).count = data.size();
                            break;
                        }
                    }
                    hintTrackContactData.add(data);
                    hintTrackAdapter = new HintTrackAdapter(hintTracks, leader_game_screen.this);
                    hintTrackView.setAdapter(hintTrackAdapter);
                }
                guessCross();

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

        players.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)return;
                HashMap<String,String> data = (HashMap<String, String>) Objects.requireNonNull(dataSnapshot).getValue();
                if(data==null || data.keySet().isEmpty())return;

                for(String s: data.keySet()){
                    if(data.get(s).equals("Player")){
                        if(s.equals(new playerName().getName())){
                            playerList.add(s + "         " + "(You)");
                        }else{
                            if(!playerList.contains(s))
                                playerList.add(s);
                        }
                    }
                }
                playerListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void setTrophy(int n){
        if(n>0){
            findViewById(R.id.trophy1).setBackgroundResource(R.drawable.trophy);
            n--;
        }
        if(n>0){
            findViewById(R.id.trophy2).setBackgroundResource(R.drawable.trophy);
            n--;
        }
        if(n>0){
            findViewById(R.id.trophy3).setBackgroundResource(R.drawable.trophy);
            n--;
        }
        if(n>0){
            findViewById(R.id.trophy4).setBackgroundResource(R.drawable.trophy);
            n--;
        }
        if(n>0){
            findViewById(R.id.trophy5).setBackgroundResource(R.drawable.trophy);
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
            String date= new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
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
        guessTime = Integer.parseInt(guessesHintTrack.get(hintTrackSelectTimestamp));

        MessageAdapter tempHintAdapter = new MessageAdapter(ht.track,leader_game_screen.this);
        guessCross();
        recyclerView.setAdapter(tempHintAdapter);
        editText.setVisibility(View.GONE);
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
            findViewById(R.id.cross1_leader).setBackgroundResource(R.drawable.uncross);
            findViewById(R.id.cross2_leader).setBackgroundResource(R.drawable.uncross);
            findViewById(R.id.cross3_leader).setBackgroundResource(R.drawable.uncross);
            return;
        }
        int g = Integer.parseInt(guessesHintTrack.get(hintTrackSelectTimestamp));
        guessTime = g;
        if(g>0){
            findViewById(R.id.cross1_leader).setBackgroundResource(R.drawable.cross);
            g--;
        }
        if(g>0){
            findViewById(R.id.cross2_leader).setBackgroundResource(R.drawable.cross);

            g--;
        }
        if(g>0){
            findViewById(R.id.cross3_leader).setBackgroundResource(R.drawable.cross);
            g--;
        }
    }

    public void cancel(View view){
        hintTrackSelectTimestamp = "";
        sendButton.setVisibility(View.VISIBLE);
        cancelGuess.setVisibility(View.GONE);
        sendGuess.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        guessBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        challengeBtn.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Type a hint..");
        guessCross();
    }

    public void guess(View view){
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Enter guess word..");
        sendButton.setVisibility(View.INVISIBLE);
        cancelGuess.setVisibility(View.VISIBLE);
        sendGuess.setVisibility(View.VISIBLE);
        guessBtn.setVisibility(View.GONE);
        challengeBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

    }

    public void reset(){
        editText.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Type a hint..");
        sendButton.setVisibility(View.VISIBLE);
        cancelGuess.setVisibility(View.GONE);
        sendGuess.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        guessBtn.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        challengeBtn.setVisibility(View.GONE);
        guessCross();
    }

    public void setSendGuess(View view){
        if(hintTrackSelectTimestamp=="")return;
        if(hintTracks.get(hintTrackSelectIdx).count == 1 ){
            if(guessTime == 2){
                String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
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
            if(hintTrackContactData.get(hintTrackSelectIdx).containsKey(author)){
                authorWord = hintTrackContactData.get(hintTrackSelectIdx).get(author);
            }else{
                return;
            }
            if(temp.equals(authorWord.toLowerCase())){
                String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader guessed correctly for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
                gameWord.child("Leader").setValue(String.valueOf(currentScore+1));
                deleteHintTrack();
            }else{
                String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
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

            if(hintTrackContactData.get(hintTrackSelectIdx).containsKey(author)){
                authorWord = hintTrackContactData.get(hintTrackSelectIdx).get(author);
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
                String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
                Message m = new Message("Leader guessed correctly for hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
                reference.child(date).setValue(m);
                reset();

                hintTrackSelectTimestamp = "";
                gameWord.child("Leader").setValue(String.valueOf(currentScore+1));
                deleteHintTrack();

            }else{
                editText.setText("");
                editText.setHint("Sorry");
                String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
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

                String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
                Message m1 = new Message("Leader challenged hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber ,"UniversalMessageCop",date);
                reference.child(date).setValue(m1);

                date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
                Message m2 = new Message("Contact successful for keyword : " + authorWord.toUpperCase(),"UniversalMessageCop",date);
                reference.child(date).setValue(m2);

                if(t.equals(word.toLowerCase())){
                    date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
                    Message m3 = new Message("Players have won the game.","UniversalMessageCop",date);
                    reference.child(date).setValue(m3);
                    progress = String.valueOf(word.length());
                    gameWord.child("Progress").setValue(progress);
                }


                deleteHintTrack();
            }
        }
        if(!found) {
            String date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
            Message m1 = new Message("Leader challenged hint-track#" + hintTracks.get(hintTrackSelectIdx).hintTrackNumber , "UniversalMessageCop",date);
            reference.child(date).setValue(m1);

            date = new SimpleDateFormat("ddMMyyyyhhmmss",Locale.ENGLISH).format(new Date());
            Message m = new Message("Challenge successful, Contact failed.", "UniversalMessageCop",date);
            reference.child(date).setValue(m);

            gameWord.child("Leader").setValue(String.valueOf(currentScore+1));
            deleteHintTrack();
        }
    }

    public void playerList(View view){
        if(!playersShown) {
            playersShown = true;
            int playerListHt = findViewById(R.id.main).getHeight();
            playerListView.setVisibility(View.VISIBLE);
            playerListView.setMinimumHeight(playerListHt);

            findViewById(R.id.layout_message_send_leader).setVisibility(View.GONE);
            findViewById(R.id.layout_hint_track_leader).setVisibility(View.GONE);
            findViewById(R.id.imageView2).setVisibility(View.GONE);
            findViewById(R.id.message_box_leader).setVisibility(View.GONE);
            findViewById(R.id.guess_cross_leader).setVisibility(View.GONE);
        }else{
            playerListView.setVisibility(View.GONE);
            playersShown = false;
            findViewById(R.id.layout_message_send_leader).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_hint_track_leader).setVisibility(View.VISIBLE);
            findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
            findViewById(R.id.message_box_leader).setVisibility(View.VISIBLE);
            findViewById(R.id.guess_cross_leader).setVisibility(View.VISIBLE);
        }
    }


    private void deleteHintTrack(){
        if(hintTrackSelectIdx > hintTracks.size() ||  !hintTracks.get(hintTrackSelectIdx).timeStamp.equals(hintTrackSelectTimestamp)){return;}
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
        if(t.substring(0,temp+1).equals(s.substring(0,temp+1))){
            return true;
        }
        return false;
    }

    public void leave(View view){
        players.child(new playerName().getName()).removeValue();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }


}
