package com.example.vivek.mypoll;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.MyPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
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
    }

    @Override
    public int getItemCount() {
        return mPolls.size();
    }



    public class PollHolder extends RecyclerView.ViewHolder{

        public TextView pollQuesCardTv;

        public PollHolder(View itemView) {
            super(itemView);

            pollQuesCardTv = itemView.findViewById(R.id.pollquesCardTv);
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            firebaseAuth = FirebaseAuth.getInstance();
            progressDialog = new ProgressDialog(mContext);
            map = new HashMap<>();

            MyPreferences.setHasPolled(mContext,false);
            if(firebaseAuth.getCurrentUser()==null){
                mContext.startActivity(new Intent(mContext, LoginActivity.class));
            }else{
                currUser = firebaseAuth.getCurrentUser().getUid();
                Log.i("mac","currUser:"+currUser);
            }

        }
    }


    private void getDatabaseValues(){
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mDatabaseRef.child("PollResults").child(MyPreferences.getPollQues(mContext))
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
                       // ((Activity)mContext).finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


