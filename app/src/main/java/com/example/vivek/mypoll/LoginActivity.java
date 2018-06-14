package com.example.vivek.mypoll;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etusername;
    private EditText etpassword;
    private Button LoginButton;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private LinearLayout bgLayout;
    private TextView loginHelpText;

    private boolean isLoginenabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

        loginHelpText = findViewById(R.id.loginHelpText);
        progressDialog = new ProgressDialog(this);
        etusername = (EditText) findViewById(R.id.etusername);
        etpassword = (EditText) findViewById(R.id.etpassword);
        LoginButton = (Button) findViewById(R.id.LoginButton);
        tvRegister = (TextView) findViewById(R.id.tvRegister);
        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        bgLayout = (LinearLayout) findViewById(R.id.linearbgLayout);

        bgLayout.setOnClickListener(this);
        LoginButton.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);

    }

    private void setUI(){
        if(isLoginenabled){
            LoginButton.setText("Login");
            tvRegister.setText("Register Here");
            loginHelpText.setText("Don't have an account?");
        }else {
            LoginButton.setText("Register");
            tvRegister.setText("Login Here");
            loginHelpText.setText("Or,");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == LoginButton){
            if(isLoginenabled)
                UserLogin();
            else
                UserRegister();
        }
        if (v == tvRegister){
            if(isLoginenabled){
                isLoginenabled=false;
                setUI();
            }else {
                isLoginenabled=true;
                setUI();
            }
        }
        if (v == tvForgotPassword) {
            ForgotPassword();
        }

        if (v == bgLayout){
            closeKeyBoard();
        }
    }

    private void closeKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }

    private void UserLogin() {
        String email = etusername.getText().toString().trim();
        String password = etpassword.getText().toString().trim();


        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Email can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Password can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Login In...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void UserRegister(){
        final String email = etusername.getText().toString().trim();
        String password = etpassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Email can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Password can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void ForgotPassword() {
    }
}
