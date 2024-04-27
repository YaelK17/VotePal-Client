package com.example.openingscreen;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

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
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.ListView;
import android.widget.RadioButton;
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
import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity  {
    FirebaseAuth auth;
    TextView email;
    FirebaseUser user;
    TextView server_message;
    Button choose_btn;
    String election_option;

    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;

    SearchView searchView;
    ListView listView;
    ArrayList arrayList;
    ArrayAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        button_dialog_related();
        search_related();

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.userdetails);
        user = auth.getCurrentUser();

        OnStart(); //checks if user had login

        choose_btn = findViewById(R.id.choose);
        server_message = findViewById(R.id.servermessage);



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

            //client(user.getUid(),"askingforoptions");
            Get_election_names();  // adds to the list the election names
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
    public void search_related(){
        searchView = findViewById(R.id.search_bar);
        listView = findViewById(R.id.list_item);

        arrayList = new ArrayList();

        adapter=new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String choice = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "election_option selected: " + choice, Toast.LENGTH_SHORT).show();
                client("option", ("op" + choice));  // sending choice to server
                Intent intent = new Intent(HomeActivity.this, ChoosingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

    }
    private void Get_election_names() {
        //sends the userid and also asks for options
        // function receives the names from the server
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataInputStream dIn = client.getdin();
                    DataOutputStream dOut = client.getdout();

                    byte[] bytes = user.getUid().getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data

                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server


                    bytes = "askingforoptions".getBytes(); //sending the user id to asking for options
                    dOut.write(bytes);
                    dOut.flush(); // send off the data

                    dIn.read(bytes_received); //receiving bytes message from server


                    String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    String[] election_names = s.trim().split(",");
                    for (int i=0; i<election_names.length; i++){
                        arrayList.add(election_names[i]);
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