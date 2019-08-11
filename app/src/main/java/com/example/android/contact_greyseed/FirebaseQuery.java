package com.example.android.contact_greyseed;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseQuery extends LiveData<DataSnapshot> {
    private final Query query;
    private final MyValueEventListener listener = new MyValueEventListener();

    public FirebaseQuery(Query query) {
        this.query = query;
    }

    public FirebaseQuery(DatabaseReference ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
//        Log.d("MessageQuery","Active");
        query.addValueEventListener(listener);
    }

    @Override
    protected void onInactive() {
//        Log.d("MessageQuery","Inactive");
        query.removeEventListener(listener);
    }

    private class MyValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
