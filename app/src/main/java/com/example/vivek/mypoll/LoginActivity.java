package com.example.vivek.mypoll;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;


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
    private SignInButton GButton;

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
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
        GButton = findViewById(R.id.GoogleButton);

        bgLayout.setOnClickListener(this);
        LoginButton.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        GButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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

        if(v == GButton){
            Log.i("mac","clicked");
            progressDialog.setMessage("Login In...");
            progressDialog.show();
            signIn();
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
                            Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("mac", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("mac", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Log.w("mac", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("mac", "Google sign in failed", e);
                Toast.makeText(getApplicationContext(),"Google sign in failed",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                // ...
            }
        }
    }

    private void ForgotPassword() {
        startActivity(new Intent(getApplicationContext(),ForgotPassword.class));
    }
}
