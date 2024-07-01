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
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;


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
    String detail;

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

            Get_election_names();  // adds to the list the election names
            //sends the userid and also asks for options
        }
    }
    private void OnStart(){
        super.onStart();
        GetUserStatus(); //checks if user had login
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
                String choice = ((election_details) parent.getItemAtPosition(position)).getElection_name();;
                Toast.makeText(getApplicationContext(), "election_option selected: " + choice, Toast.LENGTH_SHORT).show();

                // now there are two cases- if election has not reached due date then its green icon and we go to choosing activity
                // else we go to the results
                if (((election_details) parent.getItemAtPosition(position)).getImage() == R.drawable.baseline_green_circle_24) {
                    Intent showDetail = new Intent(getApplicationContext(), ChoosingActivity.class);
                    showDetail.putExtra("detail", choice);
                    startActivity(showDetail);  // moves details between activities
                }
                else {
                    Intent showDetail = new Intent(getApplicationContext(), ResultsActivity.class);
                    showDetail.putExtra("detail", choice);
                    startActivity(showDetail);  // moves details between activities
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

//                    if (com.example.openingscreen.PublicKey.is_PublicKey_instance_null()){
//                        byte[] publicKeyBytes = new byte[1000];
//                        dIn.read(publicKeyBytes); // Read the bytes into the array
//                        // Convert the received bytes into a PublicKey object
//                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//                        com.example.openingscreen.PublicKey.SetPublicKey(keyFactory.generatePublic(keySpec));
//                    }
//                    PublicKey publicKey1 = com.example.openingscreen.PublicKey.get_PublicKey_instance().getPublicKey();

//
                    String to_Send = "askingforoptions" + "-" + user.getUid() + "-" + "";

                    //encryption
                    String[] encryppted_m = encryptMessage(to_Send);
                    to_Send = encryppted_m[0] + "!" + encryppted_m[1];



                    byte[] bytes = to_Send.getBytes(); //sending the user id to asking for options
                    //byte[] bytes = encrypt(to_Send, publicKey1);
                    dOut.write(bytes);
                    dOut.flush(); // send off the data

                    byte[] bytes_received = new byte[1000];

                    dIn.read(bytes_received); //receiving bytes message from server
                    String decryptedMessage = new String(bytes_received);
                    String[] decryptedMessage_iv_and_m = decryptedMessage.trim().split("!");
                    decryptedMessage = decryptMessage(decryptedMessage_iv_and_m[0], decryptedMessage_iv_and_m[1]);



                    //String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    String[] election_names_and_due_dates = decryptedMessage.trim().split(",");
                    for (int i=0; i<election_names_and_due_dates.length; i++){
                        String[] name_and_due_date = election_names_and_due_dates[i].trim().split("-");
                        String due_date_to_display = "due date: " + name_and_due_date[1];
                        String name = name_and_due_date[0];

                        if (name_and_due_date[2].equals("T")){  // means date has passed
                            arrayList.add(new election_details(R.drawable.baseline_red_circle_24, name, due_date_to_display));
                        }
                        else {
                            arrayList.add(new election_details(R.drawable.baseline_green_circle_24, name, due_date_to_display));
                        }
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }public static String[] encryptMessage(String message) throws Exception {
        String CLIENT_KEY = "PAQfYscxlOFsvGzz"; // Replace with your actual key
        String ALGORITHM = "AES/CBC/PKCS5Padding";
        byte[] iv = getInitializationVector();
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        SecretKeySpec keySpec = new SecretKeySpec(CLIENT_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(message.getBytes());

        // Use Android's Base64 to encode iv and encrypted bytes
        String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
        String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT);

        return new String[]{ivString, encryptedString};
    }

    public static String decryptMessage(String iv, String encryptedText) throws Exception {
        String CLIENT_KEY = "PAQfYscxlOFsvGzz"; // Replace with your actual key
        String ALGORITHM = "AES/CBC/PKCS5Padding";
        byte[] ivBytes = Base64.decode(iv, Base64.DEFAULT);
        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(CLIENT_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encryptedBytes);

        return new String(decrypted);
    }

    private static byte[] getInitializationVector() {
        // Generate a random IV (Initialization Vector)
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
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
            if (election_details.getImage() == R.drawable.baseline_red_circle_24 && selectedFilter.equals("inactive")) {
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