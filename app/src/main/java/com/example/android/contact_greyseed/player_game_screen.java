package com.example.android.contact_greyseed;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Adapter;
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

    private DatabaseReference reference,hints,contactWord;
    private static RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapter hintTrackAdapter;
    private ArrayList<Message> messages;
    private static ArrayList<HintTrack> hintTracks;
    private String gameCode,word,progress;
    private int idx = 1,i=0,temp;
    private ImageButton cancelButton;
    private ArrayList<HashMap<String,String>> database;
    TextView wordArea;
    String msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_game_screen);

        recyclerView = findViewById(R.id.message_box);
        hintTrackView = findViewById(R.id.hintTrackView);
        wordArea = findViewById(R.id.wordArea);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cancelButton = findViewById(R.id.imageCancel);

        hintTrackView.setHasFixedSize(true);
        hintTrackView.setLayoutManager(new LinearLayoutManager(this,LinearLayout.HORIZONTAL,false));

        messages = new ArrayList<>();
        hintTracks = new ArrayList<>();

        database = new ArrayList<>();
        gameCode = new playerName().getGameCode();
        if(gameCode == ""){
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
            finish();
        }

        reference = FirebaseDatabase.getInstance().getReference("GameChat").child(gameCode);
        hints = FirebaseDatabase.getInstance().getReference("Hints").child(gameCode);
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(gameCode);

        adapter = new MessageAdapter(messages,player_game_screen.this);
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
                idx = database.size();
                for(i = 0;i<database.size();i++){
                    messages.add(new Message(database.get(i).get("msg"),database.get(i).get("sender")));
                }

                adapter = new MessageAdapter(messages,player_game_screen.this);
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
                for(int i=0;i<dataSnapshot.getChildrenCount();i++){
                    ArrayList<Message> msg_db = new ArrayList<>();
                    ArrayList<Integer> idx_db = new ArrayList<>();
                    temp = (ArrayList<String>) Objects.requireNonNull(dataSnapshot).child(String.valueOf(i)).getValue();
                    assert temp != null;
                    if(temp.size()>0){
                        for(int j=1;j<temp.size();j++){
                            msg_db.add(messages.get(Integer.valueOf(temp.get(j))));
                            idx_db.add(Integer.valueOf(temp.get(j)));
                        }

//                        Toast.makeText(player_game_screen.this, idx_db.toString(), Toast.LENGTH_SHORT).show();
                        hintTracks.add(new HintTrack(idx_db,msg_db));
//                        Toast.makeText(player_game_screen.this, hintTracks.get(hintTracks.size()-1).list.toString(), Toast.LENGTH_SHORT).show();
                        hintTrackAdapter.notifyDataSetChanged();
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
    }

    public void send_msg(View view){
        EditText editText = findViewById(R.id.message);
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

    }

    public void addHints(View view){
        final ArrayList<String> hintMsgIndex = new ArrayList<>();
        hintMsgIndex.add(new playerName().getName());
        if(adapter.getSelectedCount() > 0){
             for(int i=0;i<messages.size();i++){
                 if(messages.get(i).isSelected()){
                     hintMsgIndex.add(String.valueOf(i));
                     messages.get(i).toggleSelect();
                     recyclerView.findViewHolderForAdapterPosition(i).itemView.setBackgroundColor(Color.WHITE);
                 }
             }

            final long[] s = {0};
             hints.addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                     s[0] = dataSnapshot.getChildrenCount();
//                     Toast.makeText(player_game_screen.this, String.valueOf(s[0]), Toast.LENGTH_SHORT).show();
                     hints.child(String.valueOf(s[0])).setValue(hintMsgIndex);
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });
        }
    }

    public void resetAdapter(View view){
        recyclerView.setAdapter(adapter);
        cancelButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(int pos) {

        HintTrack ht = hintTracks.get(pos);
        temp = ht.getNext();
        if(temp >= messages.size()){
            return;
        }

        MessageAdapter tempHintAdapter = new MessageAdapter(ht.track,player_game_screen.this);
        recyclerView.setAdapter(tempHintAdapter);
        cancelButton.setVisibility(View.VISIBLE);

//        recyclerView.findViewHolderForAdapterPosition(temp).itemView.animate().alpha(0.3f).setDuration(400);
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                recyclerView.findViewHolderForAdapterPosition(temp).itemView.animate().alpha(1).setDuration(400);
//            }
//        }, 500);

    }
}
