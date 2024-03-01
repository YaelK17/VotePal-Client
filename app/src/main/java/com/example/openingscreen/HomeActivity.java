package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;


public class HomeActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button logout_btn;
    TextView email;
    FirebaseUser user;
    TextView server_message;
    Button choose_btn;
    EditText message_to_send;
    String[] election_options = {"elections1", "elections2", "elections3"};
    AutoCompleteTextView autocompleteTxt;
    ArrayAdapter<String> adapter_election_options;
    String election_option;
    Button creating;
    Button finished_creating;
    EditText candidates_names;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        logout_btn = findViewById(R.id.logout_btn);
        email = findViewById(R.id.userdetails);
        user = auth.getCurrentUser();

        OnStart(); //checks if user had login

        choose_btn = findViewById(R.id.choose);
        server_message = findViewById(R.id.servermessage);
        message_to_send = findViewById(R.id.messagetosend);
        creating = findViewById(R.id.creating);
        candidates_names = findViewById(R.id.candidates_names);
        finished_creating = findViewById(R.id.finished_creating);

        autocompleteTxt = findViewById(R.id.auto_complete_txt);
        adapter_election_options = new ArrayAdapter<String>(this,R.layout.list_elections_options, election_options);

        autocompleteTxt.setAdapter(adapter_election_options);

        autocompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                election_option = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "election_option: " + election_option, Toast.LENGTH_SHORT).show();
            }
        });
        creating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message_to_send.setVisibility(View.VISIBLE); //the name of ekection
                candidates_names.setVisibility(View.VISIBLE);
                finished_creating.setVisibility(View.VISIBLE);
            }
        });

        finished_creating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String m ="create" + message_to_send.getText();
                client(m);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() { //if you click the sign out button it will sign out and move to login screen
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        choose_btn.setOnClickListener(new View.OnClickListener() { //if you click the choose button it will go to choosing activity
            @Override
            public void onClick(View v) {
                String to_send = "option" + election_option;
                client(to_send);
                Intent intent = new Intent(HomeActivity.this, ChoosingActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void GetUserStatus(){
        if (user == null) { //if the user didnt login go to the get started screen
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else { //if user already logined
            email.setText(user.getEmail()); //the email is written in the home screen
            client("d"); // d means asking for election options
        }
    }
    private void OnStart(){
        super.onStart();
        GetUserStatus(); //checks if user had login
    }
    private void client(String election_option) {
            Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataOutputStream dOut = client.getdout();
                    DataInputStream dIn = client.getdin();
                    byte[] bytes = election_option.getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data

                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string

                    server_message.setText(s); //setting message to be message from server

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}