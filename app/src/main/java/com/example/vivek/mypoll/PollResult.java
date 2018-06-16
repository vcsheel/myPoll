package com.example.vivek.mypoll;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.CustomProgress;
import com.example.vivek.mypoll.Utility.MyPreferences;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PollResult extends AppCompatActivity {

    private LinearLayout pollResultLayout;
    private TextView pollResultQues;
    private Button goHome;
    private DatabaseReference mDatabaseRef;
    private Map<String, Integer> map;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_result);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        pollResultLayout = findViewById(R.id.pollResultLayout);
        pollResultQues = findViewById(R.id.pollResultQues);
        progressDialog = new ProgressDialog(this);
        goHome = findViewById(R.id.goHome);
        map = new HashMap<>();

        getDatabaseValues();

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });
    }


    private void setPoll() {
        int total = 0;
        float currP = 0;

        for (int v : map.values()) {
            total += v;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<String> Options = MyPreferences.getOptionsList(this);
        pollResultQues.setText(MyPreferences.getPollQues(this));

        for (int i = 0; i < Options.size(); i++) {
            final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.rowresult, null);
            final CustomProgress customProgress = rowView.findViewById(R.id.customProgress);


            if (map.containsKey(Options.get(i))) {
                int oval = map.get(Options.get(i));
                currP = (float) oval / (float) total;
            } else {
                currP = 0;
            }

            customProgress.setMaximumPercentage(currP);
            customProgress.setProgressColor(R.color.green_200);
            customProgress.useRoundedRectangleShape(20.0f);
            customProgress.setShowingPercentage(false);

            String LeftText = Options.get(i);
            String RightText = String.valueOf((int) Math.round(currP * 100)) + "%";

            final String resultText = LeftText + "       " + RightText;
            customProgress.setText(resultText);

            customProgress.setTextSize(20);
            customProgress.setTextColor(Color.WHITE);
            customProgress.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            customProgress.setPadding(50, 0, 0, 0);

            pollResultLayout.addView(rowView, pollResultLayout.getChildCount());
        }
    }


    private void getDatabaseValues() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String date = df.format(Calendar.getInstance().getTime());

        progressDialog.setTitle("Getting Results");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mDatabaseRef.child("PollResults").child(date)
                .child(MyPreferences.getPollQues(this))
                .child(MyPreferences.getAddress(this))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        for (DataSnapshot mysnap : dataSnapshot.getChildren()) {
                            String key = mysnap.getKey();
                            String val = mysnap.getValue().toString();

                            if (map.containsKey(val)) {
                                map.put(val, map.get(val) + 1);
                            } else {
                                map.put(val, 1);
                            }
                        }
                        setPoll();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }


}
