package com.example.vivek.mypoll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.MyPreferences;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollHolder>{

    private Context mContext;
    private List<String> mPolls;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Map<String,Integer> map;
    private String currUser;


    public PollAdapter(Context mContext, List<String> mPolls) {
        this.mContext = mContext;
        this.mPolls = mPolls;
    }

    @Override
    public PollHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.poll_card,parent,false);
        return new PollHolder(v);
    }

    @Override
    public void onBindViewHolder(PollHolder holder, final int position) {

        holder.pollQuesCardTv.setText(mPolls.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyPreferences.setPollQues(mContext,mPolls.get(position));
                if(MyPreferences.getAddress(mContext)==null)
                {
                    Toast.makeText(mContext,"Location is required to proceed",Toast.LENGTH_SHORT).show();
                    ((MainActivity)mContext).displayLocationSettingsRequest(mContext);
                }
                else
                    getDatabaseValues();
            }
        });

        holder.delPollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder;

                builder = new AlertDialog.Builder(mContext);

                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                delEntry(position);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {

        return mPolls.size();
    }



    public class PollHolder extends RecyclerView.ViewHolder{

        public TextView pollQuesCardTv;
        public ImageView delPollButton;


        public PollHolder(View itemView) {
            super(itemView);

            pollQuesCardTv = itemView.findViewById(R.id.pollquesCardTv);
            delPollButton = itemView.findViewById(R.id.delPollButton);
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            firebaseAuth = FirebaseAuth.getInstance();
            progressDialog = new ProgressDialog(mContext);
            map = new HashMap<>();

            MyPreferences.setHasPolled(mContext,false);
            if(firebaseAuth.getCurrentUser()==null){
                mContext.startActivity(new Intent(mContext, LoginActivity.class));
            }else{
                currUser = firebaseAuth.getCurrentUser().getUid();
                if(firebaseAuth.getCurrentUser().getEmail().equals(MainActivity.adminemail)){
                    delPollButton.setVisibility(View.VISIBLE);
                }else {
                    delPollButton.setVisibility(View.GONE);
                }
            }


        }
    }


    private void getDatabaseValues(){
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i("mac","date:"+date);

        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mDatabaseRef.child("PollResults").child(date)
                .child(MyPreferences.getPollQues(mContext))
                .child(MyPreferences.getAddress(mContext))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        for(DataSnapshot mysnap: dataSnapshot.getChildren()){
                            String key = mysnap.getKey();
                            if(key.equals(currUser)){
                                MyPreferences.setHasPolled(mContext,true);
                                break;
                            }
                        }
                        mContext.startActivity(new Intent(mContext,PollPageActivity.class));
                        //((Activity)mContext).finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void delEntry(final int pos){
        final String ques = mPolls.get(pos);

        mDatabaseRef.child("Polls").child(ques).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext,"Entry deleted from database",Toast.LENGTH_SHORT).show();
                        mPolls.remove(pos);
                        notifyDataSetChanged();
                        mDatabaseRef.child("PollResults").child(ques).removeValue();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext,"Error while deleting",Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


