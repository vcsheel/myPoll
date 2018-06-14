package com.example.vivek.mypoll;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.vivek.mypoll.Utility.MyPreferences;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private RecyclerView mPollRecyclerView;
    private PollAdapter mPollAdapter;

    private List<String> mPolls;
    private Map<String,List<String>> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mPollRecyclerView = findViewById(R.id.pollRecyclerView);
        mPollRecyclerView.setHasFixedSize(true);
        mPollRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPolls = new ArrayList<>();
        map = new HashMap<>();

        mDatabaseRef.child("Polls").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    String s = postSnapshot.getKey().toString();
                    List<String> val = (List<String>) postSnapshot.getValue();
                    map.put(s,val);

                    Log.i("mac","children:"+s+" val: "+val.toString());
                    mPolls.add(s);
                }

                MyPreferences.setAllPolls(getApplicationContext(),map);
                mPollAdapter = new PollAdapter(MainActivity.this,mPolls);
                mPollRecyclerView.setAdapter(mPollAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
