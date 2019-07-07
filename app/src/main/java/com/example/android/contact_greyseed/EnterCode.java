package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Array;
import java.util.ArrayList;

public class EnterCode extends AppCompatActivity {
    private DatabaseReference ref,players;
    private boolean enter = false;
    private ArrayList<String> playerNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);
        ref = FirebaseDatabase.getInstance().getReference("Games");
        players = FirebaseDatabase.getInstance().getReference("Players");
    }

    public void enter(View view) {
        final EditText text = findViewById(R.id.codeEntered);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Toast.makeText(EnterCode.this, dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                if (dataSnapshot.hasChild(text.getText().toString().toUpperCase())) {
                    enter = true;
                }
                if(enter){
                    Toast.makeText(EnterCode.this, "Here", Toast.LENGTH_SHORT).show();
                    players.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            Toast.makeText(EnterCode.this, dataSnapshot.child(text.getText().toString()).getValue().toString(), Toast.LENGTH_SHORT).show();
                            while(dataSnapshot.child(text.getText().toString().toUpperCase()).getValue() == null){

                            }
                            playerNames =(ArrayList<String>) dataSnapshot.child(text.getText().toString()).getValue();
                            if(!playerNames.contains(new playerName().getName()))
                                playerNames.add(new playerName().getName());
//                            Toast.makeText(EnterCode.this, playerNames.toString(), Toast.LENGTH_SHORT).show();
                            players.child(text.getText().toString().toUpperCase()).setValue(playerNames);
                            Intent intent = new Intent(EnterCode.this,Lobby_other.class);
                            intent.putExtra("gameCode",text.getText().toString());
                            startActivity(intent);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
