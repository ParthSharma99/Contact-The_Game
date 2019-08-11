package com.example.android.contact_greyseed;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewModelMessages extends ViewModel {
    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("GameChat");
    private  FirebaseQuery liveData ;

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData(String game){
        liveData = new FirebaseQuery(mDatabase.child(game));
        return liveData;
    }
}
