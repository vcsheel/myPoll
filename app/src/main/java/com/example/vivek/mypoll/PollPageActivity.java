package com.example.vivek.mypoll;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.vivek.mypoll.Utility.MyPreferences;

import java.util.List;
import java.util.Objects;

import static com.example.vivek.mypoll.R.drawable.border_colored;

public class PollPageActivity extends AppCompatActivity {

    private LinearLayout pollLayout;
    private String chosenOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_page);

        pollLayout = findViewById(R.id.pollLinearLayout);

        setPoll();

    }

    private void setPoll(){

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<String> Options = MyPreferences.getOptionsList(this);

        for(int i=0;i<Options.size();i++){
            final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.rowoptions,null);
            final Button b = rowView.findViewById(R.id.pollOption);
            b.setText(Options.get(i));
            //b.setBackgroundColor(Color.parseColor("#F59797"));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    b.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.border_colored));
                    chosenOption = b.getText().toString();
                }
            });

            pollLayout.addView(rowView, pollLayout.getChildCount());
        }
    }
}
