package com.example.android.contact_greyseed;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

public class leader_game_screen extends AppCompatActivity implements HintTrackAdapterLeader.OnHintClickListener{

    private DatabaseReference reference,hints,gameWord,contactWord;
    private static RecyclerView recyclerView;
    private RecyclerView hintTrackView;
    static public MessageAdapter adapter;
    private HintTrackAdapterLeader hintTrackAdapter;
    private ArrayList<Message> messages;
    private static ArrayList<HintTrack> hintTracks;
    private String gameCode,word,progress;
    private int idx = 1,i=0,temp;
    private ImageButton cancelButton,addButton,contactButton;
    private ArrayList<HashMap<String,String>> database;
    TextView wordArea,makeContactMsg;
    Button sendButton;
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

        sendButton = findViewById(R.id.button9_leader);
        makeContactMsg = findViewById(R.id.makeContactMsg_leader);
        editText = findViewById(R.id.hint_message_leader);

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
        gameWord = FirebaseDatabase.getInstance().getReference("GameWord").child(gameCode);
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(gameCode);


        adapter = new MessageAdapter(messages,leader_game_screen.this);
        recyclerView.setAdapter(adapter);

        hintTrackAdapter = new HintTrackAdapterLeader(hintTracks,this);
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

                adapter = new MessageAdapter(messages,leader_game_screen.this);
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
                if(!data.get("Status").equals("No Contact")){
                    if(editText == null || sendButton == null || recyclerView == null ||
                            addButton==null|| contactButton==null||makeContactMsg==null||cancelButton==null)
                        return;
                    editText.setVisibility(View.INVISIBLE);
                    sendButton.setVisibility(View.INVISIBLE);
//                    recyclerView.setVisibility(View.GONE);
                    addButton.setVisibility(View.GONE);
                    contactButton.setVisibility(View.VISIBLE);
                    makeContactMsg.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                }else{
                    if(editText == null || sendButton == null || recyclerView == null ||
                            addButton==null|| contactButton==null||makeContactMsg==null||cancelButton==null)
                        return;
                    editText.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
//                    recyclerView.setVisibility(View.VISIBLE);
                    addButton.setVisibility(View.VISIBLE);
                    contactButton.setVisibility(View.GONE);
                    makeContactMsg.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
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

    }

    @Override
    public void onClick(int pos) {
        editText.setText("");
        editText.setHint("Enter your Contact word...");
        HintTrack ht = hintTracks.get(pos);
        temp = ht.getNext();
        if(temp >= messages.size()){
            return;
        }
        MessageAdapter tempHintAdapter = new MessageAdapter(ht.track,leader_game_screen.this);
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



}
