package com.example.openingscreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ListView listView_creations, listView_voted_in;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        lists_related();
        button_dialog_related();

    }
    public void button_dialog_related(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.my);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            if (id == R.id.home){
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
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
            if (id == R.id.my){
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return true;
        });


    }
    public void lists_related(){
        listView_voted_in = findViewById(R.id.list_voted_for);
        listView_creations = findViewById(R.id.list_created_elections);


        ArrayList<election_details> arrayList_of_created_elections = new ArrayList<>(); // arraylist for the elections the user created
        ArrayList<election_details> arrayList_of_voted_elections = new ArrayList<>(); // arraylist for the elections the user created

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataOutputStream dOut = client.getdout();
                    DataInputStream dIn = client.getdin();
                    String to_send = "profile" + "-" + user.getUid() + "-" + "";  // sending all in one message
                    byte[] bytes = to_send.getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    String s ;
                    byte[] bytes_received = new byte[1000];
                    dIn.read(bytes_received); //receiving bytes message from server
                    s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    String[] list_of_both = s.trim().split(",");

                    // todo change into spliting based on "-"
                    // Calculate the middle index
                    int middleIndex = list_of_both.length / 2;

                    // Create two arrays to hold elements
                    String[] firstHalf = new String[middleIndex];
                    String[] secondHalf = new String[list_of_both.length - middleIndex];

                    // Copy elements from the original array to the first half array
                    for (int i = 0; i < middleIndex; i++) {
                        firstHalf[i] = list_of_both[i];
                    }

                    // Copy elements from the original array to the second half array
                    for (int i = middleIndex; i < list_of_both.length; i++) {
                        secondHalf[i - middleIndex] = list_of_both[i];
                    }

                    for (int i = 0; i < firstHalf.length; i++) {
                         arrayList_of_created_elections.add(new election_details(R.drawable.baseline_delete_24, firstHalf[i], "due date: 10/5/2020"));
                    }
                    for (int i = 0; i < secondHalf.length; i++) {
                        arrayList_of_voted_elections.add(new election_details(R.drawable.baseline_voted_vi_circle_24, secondHalf[i], "due date: 10/5/2020"));
                    }



                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        election_adapter created_electionAdapter = new election_adapter(this, R.layout.list_view_of_created, arrayList_of_created_elections);

        listView_creations.setAdapter(created_electionAdapter);  // setting the adapter

        election_adapter voted_electionAdapter = new election_adapter(this, R.layout.list_view_of_created, arrayList_of_voted_elections);

        listView_voted_in.setAdapter(voted_electionAdapter);  // setting the adapter

    }
}