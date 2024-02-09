package com.example.openingscreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class LoginActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button login_btn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView gotosignup;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressbar);
        gotosignup = findViewById(R.id.gotosignup);
        login_btn = findViewById((R.id.login_btn));
        gotosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());


                //if the email field is empty shows message
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this,"Enter email", Toast.LENGTH_SHORT ).show();
                    return;
                }

                //if the password field is empty shows message
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"Enter password", Toast.LENGTH_SHORT ).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Login successful",
                                            Toast.LENGTH_SHORT).show();
                                    user = mAuth.getCurrentUser();
                                    client(); // connecting to server

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }
    private void client() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //InetAddress host = InetAddress.getLocalHost();
                    Socket socket = new Socket("10.0.2.2", 1234);
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
//                    dOut.writeByte(100);
//                    dOut.writeUTF(user.getUid());
                    byte[] bytes = user.getUid().getBytes();
                    dOut.write(bytes);
                    //dOut.writeBytes(user.getUid());
                    dOut.flush(); // send off the data
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}