package com.example.vivek.mypoll;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.MyPreferences;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.example.vivek.mypoll.R.drawable.border_colored;

public class PollPageActivity extends AppCompatActivity {

    private static int LOCATION_REQUEST_CODE = 2;
    private LinearLayout pollLayout;
    private String chosenOption;
    private TextView pollQuestion;
    private Button submitPoll;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private String address;
    private TextView userLocation;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private GoogleApiClient googleApiClient;
    private String TAG = "mac";
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private String currUser;
    private Map<String, List<String>> map;
    private List<String> Options;
    private static int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_page);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        pollLayout = findViewById(R.id.pollLinearLayout);
        pollQuestion = findViewById(R.id.pollQuestionTv);
        submitPoll = findViewById(R.id.submitPoll);
        userLocation = findViewById(R.id.userLocation);
        progressDialog = new ProgressDialog(this);

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            currUser = firebaseAuth.getCurrentUser().getUid();
        }

        if (MyPreferences.getHasPolled(this)) {
            Toast.makeText(this,"You have already polled for this poll",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PollResult.class));
            finish();
        }


        setPoll();

        submitPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (address != null && !address.isEmpty()) {
                    if (chosenOption != null && !chosenOption.isEmpty()) {

                        progressDialog.setTitle("Submitting your choice");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        mDatabase.child("PollResults").child(MyPreferences.getPollQues(getApplicationContext()))
                                .child(address).child(currUser).setValue(chosenOption)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Polled successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), PollResult.class));
                                        finish();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), PollResult.class));
                                        finish();
                                    }
                                });


                    } else {
                        Toast.makeText(getApplicationContext(), "Please choose one option!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please set your location", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void setPoll() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        address = MyPreferences.getAddress(this);
        userLocation.setText(address);


        map = MyPreferences.getAllPolls(this);
        Options = map.get(MyPreferences.getPollQues(this));
        MyPreferences.setOptionssList(this, Options);

        pollQuestion.setText(MyPreferences.getPollQues(this));

        for (int i = 0; i < Options.size(); i++) {
            final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.rowoptions, null);
            final Button b = rowView.findViewById(R.id.pollOption);
            b.setText(Options.get(i));
            b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            b.setPadding(24, 0, 0, 0);
            b.setTextSize(16);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    b.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_colored));
                    chosenOption = b.getText().toString();

                    for (int j = 1; j < pollLayout.getChildCount(); j++) {
                        try {
                            View v = pollLayout.getChildAt(j);
                            Button button = v.findViewById(R.id.pollOption);
                            String btext = button.getText().toString();
                            if (!btext.equals(chosenOption)) {
                                button.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border));
                            }

                        } catch (Exception e) {
                            Log.i("mac", "error:" + e.getMessage());
                        }
                    }
                }
            });

            pollLayout.addView(rowView, pollLayout.getChildCount());
        }
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


