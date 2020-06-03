package com.watt.canpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    TextView forpass,register;
    Button login;
    EditText username,password;

    ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    SignInButton sign;

    NfcAdapter nfcAdapter;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.btn_login);

        forpass = findViewById(R.id.tvForgotPassword);
        register = findViewById(R.id.tvRegisterL);

        username = findViewById(R.id.edUsernL);
        password = findViewById(R.id.edPassL);

        sign = findViewById(R.id.gsign_in_button);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null) {
            Toast.makeText(Login.this,"This device doesnt have NFC!",Toast.LENGTH_LONG).show();
            finish();
        }

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            finish();
            startActivity(new Intent(Login.this,MainActivity.class));
        }

        configureSignIn();

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkInternet()) {

                    if (username.getText().toString().length() == 0) {
                        username.setError("Empty Field");
                    }
                    if (password.getText().toString().length() == 0) {
                        password.setError("Empty Field");
                    } else {
                        username.setError(null);
                        password.setError(null);
                        validate(username.getText().toString(), password.getText().toString());
                    }
                } else {
                    Toast.makeText(Login.this,"No Internet Connection!",Toast.LENGTH_LONG).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));
                finish();
            }
        });
    }

    public boolean checkInternet() {
        ConnectivityManager connectivityManager= (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void configureSignIn(){

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();
        mGoogleApiClient.connect();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void validate(String usern, String upass){

        progressDialog.setMessage("Authenticating....Please Wait!");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(usern,upass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(Login.this,"Login Successful!",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Login.this,MainActivity.class));
                    finish();
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(Login.this,"Login Failed!",Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this,"Login Successful!",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Login.this,MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(Login.this, "Login Failed! ", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }
}