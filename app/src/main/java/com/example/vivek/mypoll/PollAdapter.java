package com.example.vivek.mypoll;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.MyPreferences;

import java.util.List;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollHolder>{

    private Context mContext;
    private List<String> mPolls;

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
                mContext.startActivity(new Intent(mContext,PollPageActivity.class));
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
        }
    }
}


