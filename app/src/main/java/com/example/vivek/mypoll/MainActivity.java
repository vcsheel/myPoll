package com.example.vivek.mypoll;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private RecyclerView mPollRecyclerView;
    private PollAdapter mPollAdapter;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog1;
    public static TextView noPollsTv;
    private FirebaseAuth firebaseAuth;
    public final static String adminemail = "vsheel008@gmail.com";
    private List<String> mPolls;
    private Map<String,List<String>> map;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private GoogleApiClient googleApiClient;
    private String TAG = "mac";
    private static int LOCATION_REQUEST_CODE = 2;
    private String address=null;
    private String curremail;
    private boolean backpresscount =false;
    private TextView userLocationTv;
    private TextView updateLocationTv;
    private boolean refresh=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()==null){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }else {
            curremail = firebaseAuth.getCurrentUser().getEmail();
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mPollRecyclerView = findViewById(R.id.pollRecyclerView);
        mPollRecyclerView.setHasFixedSize(true);
        mPollRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);
        noPollsTv = findViewById(R.id.noPollsTv);
        userLocationTv = findViewById(R.id.userMainLocationTv);
        updateLocationTv = findViewById(R.id.updateLocText);


        if(MyPreferences.getAddress(this)==null){
            //displayLocationSettingsRequest(this);
            getPolls();

        }else {
            userLocationTv.setText(MyPreferences.getAddress(this));
            getPolls();

        }


        Objects.requireNonNull(getSupportActionBar()).setTitle("Ongoing Polls");

        updateLocationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayLocationSettingsRequest(getApplicationContext());
            }
        });

    }

    private void getPolls(){
        mPolls = new ArrayList<>();
        map = new HashMap<>();
        boolean isNew = MyPreferences.getisNewPoll(this);

        if(!(MyPreferences.getAllPolls(this).isEmpty()) && !(MyPreferences.getAllPolls(this)==null) && !refresh && !isNew){
            map = MyPreferences.getAllPolls(this);
            mPolls = new ArrayList<String>(map.keySet());
            mPollAdapter = new PollAdapter(MainActivity.this, mPolls);
            mPollRecyclerView.setAdapter(mPollAdapter);
        }else {

            progressDialog1.setTitle("Getting All Polls");
            progressDialog1.setMessage("Please wait ...");
            progressDialog1.setCancelable(false);
            progressDialog1.show();


            mDatabaseRef.child("Polls").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressDialog1.dismiss();

                    if (!dataSnapshot.exists()) {
                        noPollsTv.setVisibility(View.VISIBLE);
                    } else {
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
                        refresh=false;
                        MyPreferences.setisNewPoll(getApplicationContext(),false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog1.dismiss();
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.extras_menu,menu);

        MenuItem item = menu.findItem(R.id.newPoll);

        if(adminemail.equals(curremail)){
            item.setVisible(true);
        }else{
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.newPoll:
                startActivity(new Intent(this,AddPollActivity.class));
                this.finish();
                return true;
            case R.id.logout:
                if(firebaseAuth.getCurrentUser()!=null){
                    firebaseAuth.signOut();
                    MyPreferences.clearSP();
                    startActivity(new Intent(this,LoginActivity.class));
                    Toast.makeText(this,"Logged Out",Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }

            case R.id.refreshButton:
                refresh=true;
                getPolls();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MyPreferences.setHasPolled(this,false);
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

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is required since poll is based on location")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

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
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //ask permission
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                //we have permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120, 500, locationListener);
                //Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //storeLocation(lastknownlocation);
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
                    if (address != null && !address.isEmpty()) {
                        MyPreferences.setAddress(this, address);
                        progressDialog.dismiss();
                        userLocationTv.setText(address);
                        locationManager.removeUpdates(locationListener);
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


    public void displayLocationSettingsRequest(Context context) {
        progressDialog.setTitle("Getting your Location");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
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
        if(!backpresscount){
            backpresscount = true;
            Toast.makeText(this,"Press back again to exit!",Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backpresscount=false;
                }
            }, 2000);
        }else {
            super.onBackPressed();
        }
    }
}
