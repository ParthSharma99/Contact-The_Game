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

public class EnterContactWord extends AppCompatActivity {
    private DatabaseReference gameWord,contactWord,ref;
    EditText word;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_contact_word);


        String gameCode = new playerName().getGameCode();
        if(gameCode == ""){
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
            finish();
        }

        word = findViewById(R.id.editText);
        gameWord = FirebaseDatabase.getInstance().getReference("GameWord");
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(new playerName().getGameCode());
        contactWord.child("Status").setValue("No Contact");
        ref = FirebaseDatabase.getInstance().getReference("Games");
    }

    public void done(View view){
        String s = word.getText().toString();
        s = s.toUpperCase().trim();
        if(s.contains(" ")){
            word.setText("");
            word.setHint("Enter Word");
            return;
        }
        gameWord.child(new playerName().getGameCode()).child("Word").setValue(s);
        gameWord.child(new playerName().getGameCode()).child("Progress").setValue("0");
        gameWord.child(new playerName().getGameCode()).child("Leader").setValue("0");
        ref.child(new playerName().getGameCode()).setValue("Begun");
        ref.child(new playerName().getGameCode()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class) == "Begun"){
                    Intent intent = new Intent(EnterContactWord.this,leader_game_screen.class);
                    startActivityForResult(intent,2);
                }else{
                    finishActivity(2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
