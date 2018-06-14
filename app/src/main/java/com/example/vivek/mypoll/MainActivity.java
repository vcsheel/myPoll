package com.example.vivek.mypoll;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private RecyclerView mPollRecyclerView;
    private PollAdapter mPollAdapter;
    private ProgressDialog progressDialog;
    private TextView noPollsTv;

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
        progressDialog = new ProgressDialog(this);
        noPollsTv = findViewById(R.id.noPollsTv);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Ongoing Polls");
        progressDialog.setTitle("Getting All Polls");
        progressDialog.setMessage("Please wait ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mPolls = new ArrayList<>();
        map = new HashMap<>();

        mDatabaseRef.child("Polls").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if(!dataSnapshot.exists()){
                    noPollsTv.setVisibility(View.VISIBLE);
                }else {
                    noPollsTv.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String s = postSnapshot.getKey().toString();
                        List<String> val = (List<String>) postSnapshot.getValue();
                        map.put(s, val);

                        mPolls.add(s);
                    }

                    MyPreferences.setAllPolls(getApplicationContext(), map);
                    mPollAdapter = new PollAdapter(MainActivity.this, mPolls);
                    mPollRecyclerView.setAdapter(mPollAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.extras_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.newPoll:
                startActivity(new Intent(this,AddPollActivity.class));
                return true;
            case R.id.logout:
                //todo for logout
                Toast.makeText(this,"To do",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
