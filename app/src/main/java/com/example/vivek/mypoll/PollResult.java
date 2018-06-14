package com.example.vivek.mypoll;

import android.content.Context;
import android.content.Intent;
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

import com.example.vivek.mypoll.Utility.CustomProgress;
import com.example.vivek.mypoll.Utility.MyPreferences;

import java.util.List;
import java.util.Objects;

public class PollResult extends AppCompatActivity {

    private LinearLayout pollResultLayout;
    private TextView pollResultQues;
    private Button goHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_result);

        pollResultLayout = findViewById(R.id.pollResultLayout);
        pollResultQues = findViewById(R.id.pollResultQues);
        goHome = findViewById(R.id.goHome);
        setPoll();

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo change to polls home
                startActivity(new Intent(getApplicationContext(),PollPageActivity.class));
            }
        });
    }


    private void setPoll() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<String> Options = MyPreferences.getOptionsList(this);
        pollResultQues.setText(MyPreferences.getPollQues(this));

        for (int i = 0; i < Options.size(); i++) {
            final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.rowresult, null);
            final CustomProgress customProgress = rowView.findViewById(R.id.customProgress);


            customProgress.setMaximumPercentage(0.2f);
            customProgress.setProgressColor(Color.parseColor("#123f12"));

            int p = customProgress.getCurrentPercentage();
            customProgress.setShowingPercentage(false);

            String LeftText = Options.get(i);
            String RightText = String.valueOf(p)+"%";

            final String resultText = LeftText + "\t" + RightText;
            customProgress.setText(resultText);

            customProgress.setTextSize(20);
            customProgress.setTextColor(Color.WHITE);
            customProgress.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            customProgress.setPadding(50, 0, 0, 0);

            pollResultLayout.addView(rowView, pollResultLayout.getChildCount());
        }
    }
}
