package com.example.android.contact_greyseed;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.sql.Array;
import java.util.ArrayList;

public class EnterCode extends AppCompatActivity {
    private DatabaseReference ref,players;
    private ArrayList<String> playerNames;
    boolean toAdd = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);
        ref = FirebaseDatabase.getInstance().getReference("Games");
        players = FirebaseDatabase.getInstance().getReference("Players");
        EditText text = findViewById(R.id.codeEntered);
        text.requestFocus();
        showKeyboard();
        toAdd = true;
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() < 5){
                    toAdd = true;
                }
                if(editable.length() > 5){
                    toAdd = false;
                }
                if(toAdd && editable.length() == 5){
                    editable.append(" ");
                    toAdd = false;
                }
                if(editable.length() == 11){
                    UIUtil.hideKeyboard(EnterCode.this);
                }
            }
        });
    }

    public void enter(View view) {
        EditText text = findViewById(R.id.codeEntered);
        final String txt = text.getText().toString().toUpperCase().trim();
        if(txt.length() != 11){
            text.setText("");
            text.setHint("Enter Code");
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Toast.makeText(EnterCode.this, dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                if (dataSnapshot.hasChild(txt)) {
                    players.child(txt).child(new playerName().getName()).setValue("Player");
                    Intent intent = new Intent(EnterCode.this,Lobby_other.class);
                    intent.putExtra("gameCode",txt);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UIUtil.hideKeyboard(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UIUtil.hideKeyboard(this);
    }
}
