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
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;

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
                    //PublicKey publicKey = client.getPublicKey();
                    String to_send = "profile" + "-" + user.getUid() + "-" + "";  // sending all in one message
                    //byte[] bytes = encrypt(to_send, publicKey);
                    byte[] bytes = to_send.getBytes();
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    String s ;
                    byte[] bytes_received = new byte[1000];
                    dIn.read(bytes_received); //receiving bytes message from server
                    s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    String[] list_of_both = s.trim().split("-"); // first index will be created and second voted for
                    String[] created_list = list_of_both[0].trim().split(",");
                    String[] voted_for_list = list_of_both[1].trim().split(",");
                    for (int i = 0; i < created_list.length; i++) {
                         arrayList_of_created_elections.add(new election_details(R.drawable.baseline_delete_24, created_list[i], "created by you"));
                    }
                    for (int i = 0; i < voted_for_list.length; i++) {
                        arrayList_of_voted_elections.add(new election_details(R.drawable.baseline_voted_vi_circle_24, voted_for_list[i], "you voted in this election"));
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
    public byte[] encrypt(String plaintext, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plaintext.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}