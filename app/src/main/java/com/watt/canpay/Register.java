package com.watt.canpay;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    EditText usern,pass,cpass;
    Button register;
    TextView loginR;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usern = findViewById(R.id.edUsernR);
        pass = findViewById(R.id.edPassR);
        cpass = findViewById(R.id.edCPassR);

        register = findViewById(R.id.btn_register);

        loginR = findViewById(R.id.tvLoginR);

        firebaseAuth = FirebaseAuth.getInstance();

        cpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(pass.getText().toString().trim().equals(cpass.getText().toString().trim())){
                    register.setEnabled(true);
                    cpass.setError(null);
                }else{
                    register.setEnabled(false);
                    cpass.setError("Password doesn't match!");
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkInternet()) {
                    String emailid = usern.getText().toString().trim();
                    String passw = pass.getText().toString().trim();

                    if (emailid.length() == 0) {
                        usern.setError("Empty Field");
                    }
                    if (passw.length() == 0) {
                        pass.setError("Empty Field");
                    } else {
                        usern.setError(null);
                        pass.setError(null);
                        firebaseAuth.createUserWithEmailAndPassword(emailid, passw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(Register.this, Login.class));
                                    finish();
                                } else {
                                    Toast.makeText(Register.this, "Error! User not Registered.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }else {
                    Toast.makeText(Register.this,"No Internet Connection!",Toast.LENGTH_LONG).show();
                }
            }
        });

        loginR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,Login.class));
                finish();
            }
        });
    }

    public boolean checkInternet() {
        ConnectivityManager connectivityManager

                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

