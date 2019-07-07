package com.example.android.contact_greyseed;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class player_game_screen extends AppCompatActivity implements HintTrackAdapter.OnHintClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord,players,games;
    private static RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapter hintTrackAdapter;
    private ArrayList<Message> messages,messagesAdapter;
    private static ArrayList<HintTrack> hintTracks,hintAdapter;
    private String gameCode,word,progress,ccw = "";
    private int idx = 1,i=0,temp,hintTrackSelectIdx = 0;
    private ImageButton cancelButton,addButton,contactButton,contactWordSend,cancelContactBtn,contactWordSendBtn;
    private ArrayList<HashMap<String,String>> database;
    private ArrayList<String> playerNames;
    TextView wordArea,makeContactMsg;
    Button sendButton;
    EditText editText;
    String msg,contactHintTrack = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_game_screen);

        recyclerView = findViewById(R.id.message_box);
        hintTrackView = findViewById(R.id.hintTrackView);
        wordArea = findViewById(R.id.wordArea);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cancelContactBtn = findViewById(R.id.cancelContact);
        contactWordSendBtn = findViewById(R.id.contactBtn);
        cancelButton = findViewById(R.id.imageCancel);
        sendButton = findViewById(R.id.button9);
        addButton = findViewById(R.id.imageAdd);
        contactButton = findViewById(R.id.imageContact);
        contactWordSend = findViewById(R.id.contactBtn);
        makeContactMsg = findViewById(R.id.makeContactMsg);
        editText = findViewById(R.id.hint_message);

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


        adapter = new MessageAdapter(messagesAdapter,player_game_screen.this);
        recyclerView.setAdapter(adapter);

        hintTrackAdapter = new HintTrackAdapter(hintTracks, this);
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
                adapter = new MessageAdapter(messagesAdapter,player_game_screen.this);
                recyclerView.setAdapter(adapter);
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
                for(int i=1;i<dataSnapshot.getChildrenCount();i++){
                    ArrayList<Message> msg_db = new ArrayList<>();
                    ArrayList<Integer> idx_db = new ArrayList<>();
                    temp = (ArrayList<String>) Objects.requireNonNull(dataSnapshot).child(String.valueOf(i)).getValue();
                    if(temp.isEmpty())return;
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
                HashMap<String,String> data = (HashMap<String, String>) dataSnapshot.getValue();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            messages.add(new Message(msg,new playerName().getName()));
            editText.setText(" ");
            reference.setValue(messages);
        }

    }

    public void getHintContactWord(View view){
        String temp = editText.getText().toString().trim();
        if(!checkMessage(temp)){
            Toast.makeText(this, "Check the Game word.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!temp.contains(" ") && !temp.equals("")){
//            Toast.makeText(this, contactHintTrack, Toast.LENGTH_SHORT).show();
            if(adapter.getSelectedCount() > 0) {

                final ArrayList<String> hintMsgIndex = new ArrayList<>();
                hintMsgIndex.add(new playerName().getName());


                for (int i = 0; i < messagesAdapter.size(); i++) {
                    if (messagesAdapter.get(i).isSelected()) {
                        hintMsgIndex.add(String.valueOf(i+1));
                        messagesAdapter.get(i).toggleSelect();
                        recyclerView.findViewHolderForAdapterPosition(i).itemView.setBackgroundColor(Color.WHITE);
                    }
                }
//                hintMsgIndex.add("1");
                final long[] s = {0};
                final String word = temp;
                hints.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        s[0] = dataSnapshot.getChildrenCount();
//                     Toast.makeText(player_game_screen.this, String.valueOf(s[0]), Toast.LENGTH_SHORT).show();
                        hints.child(String.valueOf(s[0])).setValue(hintMsgIndex);
                        contactWord.child(String.valueOf(s[0])).child("0").child(new playerName().getName()).setValue(word);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            sendButton.setVisibility(View.VISIBLE);
            contactWordSendBtn.setVisibility(View.GONE);
            cancelContactBtn.setVisibility(View.GONE);
            editText.setText("");
            editText.setHint("Type a hint..");
        }



    }

    public void addHints(View view){

        sendButton.setVisibility(View.INVISIBLE);
        contactWordSendBtn.setVisibility(View.VISIBLE);
        cancelContactBtn.setVisibility(View.VISIBLE);
        editText.setText("");
        editText.setHint("Enter your contact word..");

//        if(adapter.getSelectedCount() > 0) {
//
//            final ArrayList<String> hintMsgIndex = new ArrayList<>();
//            hintMsgIndex.add(new playerName().getName());
//
//            if(contactHintTrack.equals("")){
////                sendButton.setVisibility(View.VISIBLE);
////                contactWordSendBtn.setVisibility(View.GONE);
////                cancelContactBtn.setVisibility(View.GONE);
////                editText.setText("");
////                editText.setHint("Type a hint..");
//                return;
//            }
//            for (int i = 0; i < messages.size(); i++) {
//                if (messages.get(i).isSelected()) {
//                    hintMsgIndex.add(String.valueOf(i));
//                    messages.get(i).toggleSelect();
//                    recyclerView.findViewHolderForAdapterPosition(i).itemView.setBackgroundColor(Color.WHITE);
//                }
//            }
//            hintMsgIndex.add("1");
//            final long[] s = {0};
//            hints.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                    s[0] = dataSnapshot.getChildrenCount();
////                     Toast.makeText(player_game_screen.this, String.valueOf(s[0]), Toast.LENGTH_SHORT).show();
//                    hints.child(String.valueOf(s[0])).setValue(hintMsgIndex);
//                    contactWord.child(String.valueOf(s[0])).child("0").child(new playerName().getName()).setValue(contactHintTrack);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }
//        contactHintTrack = "";


    }

    public void resetAdapter(View view){
        recyclerView.setAdapter(adapter);
        hintTrackView.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.GONE);
        contactButton.setVisibility(View.GONE);
        makeContactMsg.setVisibility(View.GONE);
        editText.setText("");
        editText.setHint("Type a hint..");

    }

    @Override
    public void onClick(int pos) {
        editText.setText("");
        editText.setHint("Enter your Contact word...");
        HintTrack ht = hintTracks.get(pos);
        hintTrackSelectIdx = pos;
        MessageAdapter tempHintAdapter = new MessageAdapter(ht.track,player_game_screen.this);
        recyclerView.setAdapter(tempHintAdapter);
        addButton.setVisibility(View.GONE);
        hintTrackView.setVisibility(View.GONE);

        cancelButton.setVisibility(View.VISIBLE);
        contactButton.setVisibility(View.VISIBLE);
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

    public void makeContact(View view){

        final String temp = editText.getText().toString().trim();
        if(!checkMessage(temp)){
            Toast.makeText(this, "Check the Game word.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(temp.equals("") || temp.contains(" "))return;
        contactWord.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long cnt = dataSnapshot.child(String.valueOf(hintTrackSelectIdx)).getChildrenCount();
                if(!dataSnapshot.child(String.valueOf(hintTrackSelectIdx)).child("0")
                        .getValue().toString().equals(new playerName().getName()))
                {
                    contactWord.child(String.valueOf(hintTrackSelectIdx)).child(String.valueOf(cnt)).child(new playerName().getName()).setValue(temp);
//                    hintTrackAdapter.incrementContactCount(,);
                    hintTracks.get(hintTrackSelectIdx).count+=1;
                    hintTrackAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        editText.setText("");
        resetAdapter(view);
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

    public void leave(View view){
        players.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                while(dataSnapshot.child(gameCode).getValue() == null){}
                playerNames =(ArrayList<String>) dataSnapshot.child(gameCode).getValue();
                playerNames.remove(new playerName().getName());
                ArrayAdapter<String> adp= new ArrayAdapter<>(player_game_screen.this,android.R.layout.simple_list_item_1,playerNames);
                players.child(gameCode).setValue(playerNames);
                Intent intent = new Intent(player_game_screen.this,MainActivity.class);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
