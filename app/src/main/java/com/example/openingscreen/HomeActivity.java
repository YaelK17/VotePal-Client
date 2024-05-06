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
import android.graphics.drawable.Drawable;
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
    Button view_result_btn;
    String election_option;

    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;

    SearchView searchView;
    ListView listView;
    ArrayList<election_details> arrayList;
    String selectedFilter = "all";
    String currentSearchText = "";

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

        view_result_btn = findViewById(R.id.view_results);
        server_message = findViewById(R.id.servermessage);


        view_result_btn.setOnClickListener(new View.OnClickListener() {
            // asking for the results
            @Override
            public void onClick(View v) {
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//                            Client client = Client.getClient_instance();
//                            Socket socket = client.getSocket();
//                            DataOutputStream dOut = client.getdout();
//                            DataInputStream dIn = client.getdin();
//                            byte[] bytes = "results".getBytes(); //asking for results
//                            dOut.write(bytes);
//                            dOut.flush(); // send off the data
//                            String s = "";
//                            byte[] bytes_received = new byte[100];
//                            dIn.read(bytes_received); //receiving bytes message from server
//                            s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
//
//
//                            // second send
//                            // todo change results to the name of election
//                            bytes = "results".getBytes(); //sends the election name that we need its results
//                            dOut.write(bytes);
//                            dOut.flush(); // send off the data
//                            //we receive the results in results activity
//
//                        }
//                        catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                thread.start();
                Intent intent = new Intent(HomeActivity.this, ResultsActivity.class);
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
    public void search_related() {
        searchView = findViewById(R.id.search_bar);
        listView = findViewById(R.id.list_item);

        arrayList = new ArrayList<>();

        election_adapter adapter = new election_adapter(this, R.layout.list_view_of_created, arrayList);

        listView.setAdapter(adapter);  // setting the adapter
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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                ArrayList<election_details> filtered_elections = new ArrayList<election_details>();
//                for (election_details election_details: arrayList) { // going through items in arraylist
//                    if (election_details.getElection_name().contains(newText)){
//                        filtered_elections.add(election_details);
//                    }
//
//                }

                currentSearchText = newText;
                ArrayList<election_details> filtered_elections = new ArrayList<election_details>();

                for (election_details election_details : arrayList) // going through items in arraylist
                {
                    if (election_details.getElection_name().contains(newText)) {
                        if (selectedFilter.equals("all")) {
                            filtered_elections.add(election_details);
                        } else {
                            if (election_details.getImage() == R.drawable.baseline_green_circle_24 && selectedFilter.equals("active")) {
                                filtered_elections.add(election_details);
                            }
                            if (election_details.getImage() == R.drawable.baseline_red_circle_24 && selectedFilter.equals("inactivefilte")) {
                                filtered_elections.add(election_details);
                            }
                        }
                    }

                }


                election_adapter new_adapter = new election_adapter(getApplicationContext(), R.layout.list_view_of_created, filtered_elections);

                listView.setAdapter(new_adapter);  // setting the adapter
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
                        arrayList.add(new election_details(R.drawable.baseline_green_circle_24, election_names[i], "due date: 10/5/2020"));
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

    private void filterList(String status)
    {
        selectedFilter = status;

        ArrayList<election_details> filteredelections = new ArrayList<election_details>();

        for(election_details election_details: arrayList)
        {
            if (election_details.getImage() == R.drawable.baseline_green_circle_24 && selectedFilter.equals("active")) {
                if(currentSearchText == "")
                {
                    filteredelections.add(election_details);
                }
                else
                {
                    if(election_details.getElection_name().contains(currentSearchText.toLowerCase()))
                    {
                        filteredelections.add(election_details);
                    }
                }
            }
            if (election_details.getImage() == R.drawable.baseline_red_circle_24 && selectedFilter.equals("inactivefilte")) {
                if(currentSearchText == "")
                {
                    filteredelections.add(election_details);
                }
                else
                {
                    if(election_details.getElection_name().contains(currentSearchText.toLowerCase()))
                    {
                        filteredelections.add(election_details);
                    }
                }
            }

        }

        election_adapter adapter = new election_adapter(getApplicationContext(), R.layout.list_view_of_created, filteredelections);
        listView.setAdapter(adapter);
    }


    public void allFilterTapped(View view) {
        selectedFilter = "all";

        election_adapter adapter = new election_adapter(getApplicationContext(), R.layout.list_view_of_created, arrayList);
        listView.setAdapter(adapter);
    }

    public void activeFilterTapped(View view) {
        filterList("active");
    }

    public void inactiveFilterTapped(View view) {
        filterList("inactive");
    }
}