package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EnterContactWord extends AppCompatActivity {
    private DatabaseReference gameWord,contactWord,ref;
    EditText word;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_contact_word);

        word = findViewById(R.id.editText);
        gameWord = FirebaseDatabase.getInstance().getReference("GameWord");
        contactWord = FirebaseDatabase.getInstance().getReference("ContactWord").child(new playerName().getGameCode());
        contactWord.child("Status").setValue("No Contact");
        ref = FirebaseDatabase.getInstance().getReference("Games");
    }

    public void done(View view){
        String s = word.getText().toString();
        s = s.trim();
        if(s.contains(" ")){
            Toast.makeText(this, "Enter a single word.", Toast.LENGTH_SHORT).show();
            return;
        }
        gameWord.child(new playerName().getGameCode()).child("Word").setValue(s);
        gameWord.child(new playerName().getGameCode()).child("Progress").setValue("0");
        ref.child(new playerName().getGameCode()).setValue("Begun");
        Intent intent = new Intent(EnterContactWord.this,leader_game_screen.class);
        startActivity(intent);
    }
}
