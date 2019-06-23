package com.example.android.contact_greyseed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EnterUsername extends AppCompatActivity {
    EditText username;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_username);
        username = findViewById(R.id.username);
        auth = FirebaseAuth.getInstance();
    }


    public void enter(View view){

        final EditText username = findViewById(R.id.username);
        new playerName().setName(username.getText().toString());
        auth.signInWithEmailAndPassword(username.getText().toString() + "@home.com","password!@#$")
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(EnterUsername.this,MainActivity.class);
                    startActivity(intent);
                }else{
                    register();
                }
            }
        });
    }
    public void register(){
        auth.createUserWithEmailAndPassword(username.getText().toString() + "@home.com","password!@#$")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Intent intent = new Intent(EnterUsername.this,MainActivity.class);
                        startActivity(intent);
                    }
                });
    }


}
