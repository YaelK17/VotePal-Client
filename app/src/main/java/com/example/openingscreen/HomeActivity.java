package com.example.openingscreen;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


public class HomeActivity extends AppCompatActivity  {
    FirebaseAuth auth;
    Button logout_btn;
    TextView email;
    FirebaseUser user;
    TextView server_message;
    Button choose_btn;
    String[] election_options = {"election4", "elections1", "gvnhf"};
    AutoCompleteTextView autocompleteTxt;
    ArrayAdapter<String> adapter_election_options;
    String election_option;

    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        button_dialog_related();

        auth = FirebaseAuth.getInstance();
        logout_btn = findViewById(R.id.logout_btn);
        email = findViewById(R.id.userdetails);
        user = auth.getCurrentUser();

        OnStart(); //checks if user had login

        choose_btn = findViewById(R.id.choose);
        server_message = findViewById(R.id.servermessage);

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
                client("option", ("op" + election_option));
                Intent intent = new Intent(HomeActivity.this, ChoosingActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void GetUserStatus(){
        // functions checks if the user already login
        if (user == null) { //if the user didnt login go to the get started screen
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else { //if user already logined
            email.setText(user.getEmail()); //the email is written in the home screen

            client(user.getUid(),"askingforoptions");
            //sends the userid and also asks for options
        }
    }
    private void OnStart(){
        super.onStart();
        GetUserStatus(); //checks if user had login
    }
    private void client(String to_send_first, String to_Send_later) {
            Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataOutputStream dOut = client.getdout();
                    DataInputStream dIn = client.getdin();
                    byte[] bytes = to_send_first.getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    String s = "";
                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string

                    if (to_send_first == "option"){
                        bytes = to_Send_later.substring(2).getBytes(); //sending the user id to server
                        dOut.write(bytes);
                        dOut.flush();
                    }
                    else {
                        server_message.setText(s); //setting message to be message from server
                        //election_options = s.trim().split(",");
                    }

                    // second send
                    if (!to_Send_later.equals(" ") && !to_send_first.equals("option")){
                        bytes = to_Send_later.getBytes(); //sending the user id to server
                        dOut.write(bytes);
                        dOut.flush(); // send off the data
                        s = "";
                        String x = to_Send_later.substring(0,1);
                        if (!x.equals("op")) { // if its the option then it does need to receive an answer in this activity
                            bytes_received = new byte[100];
                            dIn.read(bytes_received); //receiving bytes message from server
                            s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                            server_message.setText(s);
                        }


                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    public void button_dialog_related(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            if (id == R.id.home){
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            if (id == R.id.my){
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            if (id == R.id.createactivity){
                startActivity(new Intent(getApplicationContext(), CreateActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return true;
        });

    }

}