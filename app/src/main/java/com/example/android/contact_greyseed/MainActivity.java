package com.example.android.contact_greyseed;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent intent = new Intent(MainActivity.this,EnterUsername.class);
            startActivity(intent);
        }else{
            String email = user.getEmail();
            String name = "";
            int i = 0;
            while(email.charAt(i) != '@'){
                name += email.charAt(i);
                i++;
            }
            new playerName().setName(name);
        }
    }
    public void create(View view){
        Intent intent = new Intent(MainActivity.this,Lobby.class);
        startActivity(intent);
    }
    public void join(View view){
        Intent intent = new Intent(MainActivity.this,EnterCode.class);
        startActivity(intent);
    }
    public void user(View view){
        Intent intent = new Intent(MainActivity.this,EnterUsername.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
}
