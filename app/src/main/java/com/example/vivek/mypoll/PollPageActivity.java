package com.example.vivek.mypoll;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
                                        Toast.makeText(getApplicationContext(), "voted", Toast.LENGTH_SHORT).show();
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


        userLocation.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                if (MyPreferences.getAddress(getApplicationContext()) == null) {
                    displayLocationSettingsRequest(getApplicationContext());

                    progressDialog.setTitle("Getting your Location");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                } else {
                    address = MyPreferences.getAddress(getApplicationContext());
                    userLocation.setText(address);

                }
            }
        });
    }


    private void setPoll() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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


    private void getLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("mac", "location: " + location.toString());
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                storeLocation(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is required since poll is based on location")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(PollPageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressDialog.dismiss();
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //ask permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                //we have permission

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120, 500, locationListener);
                Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                storeLocation(lastknownlocation);
            }
        }
    }

    private void storeLocation(Location location) {
        try {
            List<Address> listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (listAddress != null && listAddress.size() > 0) {
                Log.i("mac", "Place Info: " + listAddress.get(0).toString());
                if (listAddress.get(0).getLocality() != null) {
                    address = listAddress.get(0).getLocality();
                    userLocation.setText(address);
                    if (address != null && !address.isEmpty()) {
                        MyPreferences.setAddress(this, address);
                        progressDialog.dismiss();
                    }
                }
            }


        } catch (Exception e) {
            Log.i("mac", "location error: " + e.getMessage());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120, 500, locationListener);

                }
            } else {
                progressDialog.dismiss();
            }
        }
    }


    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");

                        getLocation();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(PollPageActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        displayLocationSettingsRequest(getApplicationContext());//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}


