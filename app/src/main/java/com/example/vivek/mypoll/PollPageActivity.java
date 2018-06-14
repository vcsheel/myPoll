package com.example.vivek.mypoll;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.MyPreferences;

import java.util.List;
import java.util.Objects;

import static com.example.vivek.mypoll.R.drawable.border_colored;

public class PollPageActivity extends AppCompatActivity {

    private LinearLayout pollLayout;
    private String chosenOption;
    private TextView pollQuestion;
    private Button submitPoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_page);

        pollLayout = findViewById(R.id.pollLinearLayout);
        pollQuestion = findViewById(R.id.pollQuestionTv);
        submitPoll = findViewById(R.id.submitPoll);
        setPoll();

        submitPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),PollResult.class));
            }
        });
    }

    private void setPoll(){

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<String> Options = MyPreferences.getOptionsList(this);
        pollQuestion.setText(MyPreferences.getPollQues(this ));

        for(int i=0;i<Options.size();i++){
            final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.rowoptions,null);
            final Button b = rowView.findViewById(R.id.pollOption);
            b.setText(Options.get(i));
            b.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            b.setPadding(24,0,0,0);
            b.setTextSize(16);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    b.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.border_colored));
                    chosenOption = b.getText().toString();

                    for(int j=1;j<pollLayout.getChildCount();j++){
                        try {
                            View v = pollLayout.getChildAt(j);
                            Button button = v.findViewById(R.id.pollOption);
                            String btext = button.getText().toString();
                            if(btext!=chosenOption){
                                button.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.border));
                            }


                        }catch (Exception e){
                            Log.i("mac","error:"+e.getMessage());
                        }
                    }
                }
            });

            pollLayout.addView(rowView, pollLayout.getChildCount());
        }
    }
}
