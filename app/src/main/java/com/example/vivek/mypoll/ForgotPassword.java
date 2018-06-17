package com.example.vivek.mypoll;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText resetEmail;
    private TextView resetText;
    private Button resetButton;
    private Button goLogin;
    private FirebaseAuth auth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resetEmail = findViewById(R.id.resetEmailId);
        resetText = findViewById(R.id.resetText);
        resetButton = findViewById(R.id.resetButton);
        goLogin = findViewById(R.id.goLogin);
        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();

        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = resetEmail.getText().toString().trim();

                if(email.isEmpty() || email.equals("")){
                    Toast.makeText(getApplicationContext(),"Email can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setMessage("Sending reset email");
                progressDialog.setCancelable(false);
                progressDialog.show();

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    resetText.setVisibility(View.VISIBLE);
                                    Log.d("mac", "Email sent.");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Error while sending mail",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    }
}
