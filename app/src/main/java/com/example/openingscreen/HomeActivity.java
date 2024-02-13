package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
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


public class HomeActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button logout_btn;
    TextView email;
    FirebaseUser user;
    TextView server_message;
    TextView choice;
    EditText message_to_send;
    String[] election_options = {"מועצת תלמידים אורט גוטמן", "וועד בניין רחוב משהו משהו", "וועד בית רחוב משהו"};
    AutoCompleteTextView autocompleteTxt;
    ArrayAdapter<String> adapter_election_options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        logout_btn = findViewById(R.id.logout_btn);
        email = findViewById(R.id.userdetails);
        user = auth.getCurrentUser();

        OnStart(); //checks if user had login

        choice = findViewById(R.id.choose);
        server_message = findViewById(R.id.servermessage);
        message_to_send = findViewById(R.id.messagetosend);

        autocompleteTxt = findViewById(R.id.auto_complete_txt);
        adapter_election_options = new ArrayAdapter<String>(this,R.layout.list_elections_options, election_options);

        autocompleteTxt.setAdapter(adapter_election_options);

        autocompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String election_option = parent.getItemAtPosition(position).toString();
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
    }
    private void GetUserStatus(){
        if (user == null) { //if the user didnt login go to the get started screen
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else { //if user already logined
            email.setText(user.getEmail()); //the email is written in the home screen
        }
    }
    private void OnStart(){
        super.onStart();
        GetUserStatus(); //checks if user had login
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
                    byte[] bytes = user.getUid().getBytes(); //sending the user id to server
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