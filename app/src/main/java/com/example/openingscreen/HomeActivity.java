package com.example.openingscreen;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


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
    EditText candidates_names2;
    EditText candidates_names3;
    ImageView ImageView1;
    ImageView ImageView2;
    ImageView ImageView3;

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

        candidates_names = findViewById(R.id.candidates_names1);
        candidates_names2 = findViewById(R.id.candidates_names2);
        candidates_names3 = findViewById(R.id.candidates_name3);


        finished_creating = findViewById(R.id.finished_creating);

        autocompleteTxt = findViewById(R.id.auto_complete_txt);
        adapter_election_options = new ArrayAdapter<String>(this,R.layout.list_elections_options, election_options);

        autocompleteTxt.setAdapter(adapter_election_options);

        ImageView1 = findViewById(R.id.adding_photo_one);
        ImageView2 = findViewById(R.id.adding_photo_two);
        ImageView3 = findViewById(R.id.adding_photo_three);
        // image picker

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
                // setting the hidden creating options visible
                message_to_send.setVisibility(View.VISIBLE); //the name of election
                candidates_names.setVisibility(View.VISIBLE);
                candidates_names2.setVisibility(View.VISIBLE);
                candidates_names3.setVisibility(View.VISIBLE);
                finished_creating.setVisibility(View.VISIBLE);
                ImageView1.setVisibility(View.VISIBLE);
                ImageView2.setVisibility(View.VISIBLE);
                ImageView3.setVisibility(View.VISIBLE);
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
        //ActivityResultLauncher<PickVisualMediaRequest> launcher = getlancher(imageView1);
        ImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_launcher(ImageView1).launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        ImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_launcher(ImageView2).launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        ImageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_launcher(ImageView3).launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

    }
    private ActivityResultLauncher<PickVisualMediaRequest> get_launcher(ImageView imageView){
        ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                if (o == null) {
                } else {
                    Glide.with(getApplicationContext()).load(o).into(imageView);
                }
            }
        });
        return launcher;
    }
    private void GetUserStatus(){
        if (user == null) { //if the user didnt login go to the get started screen
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else { //if user already logined
            email.setText(user.getEmail()); //the email is written in the home screen
            client("askingforoptions"); // means asking for election options
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
                    String s = "";
                    String x = election_option.substring(0,6);
                    if (!x.equals("option")){ // if its the option then it does need to receive an answer in this activity
                        byte[] bytes_received = new byte[100];
                        dIn.read(bytes_received); //receiving bytes message from server
                        s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string

                    }

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