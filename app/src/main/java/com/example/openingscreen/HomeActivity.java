package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Socket;
import java.io.DataOutputStream;


public class HomeActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button logout_btn;
    TextView email;
    FirebaseUser user;
    TextView server_message;
    TextView connecting;
    EditText message_to_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        logout_btn = findViewById(R.id.logout_btn);
        email = findViewById(R.id.userdetails);
        user = auth.getCurrentUser();

        connecting = findViewById(R.id.connecting);
        server_message = findViewById(R.id.servermessage);
        message_to_send = findViewById(R.id.messagetosend);

        if (user == null) { //if the user didnt login go to the get started screen
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else { //if user already logined
            email.setText(user.getEmail()); //the email is written in the home screen
        }

        logout_btn.setOnClickListener(new View.OnClickListener() { //if you click the sign out button it will sign out and move to login screen
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //ClientSocket c = new ClientSocket();
        // making the connection with server
        client();
    }

    public void client() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket socket = new Socket("127.0.0.1", 5000);
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                    dOut.writeByte(1);
                    dOut.writeUTF("first message");
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