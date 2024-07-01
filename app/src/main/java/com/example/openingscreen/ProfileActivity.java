package com.example.openingscreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
                    //encryption
                    String[] encryppted_m = encryptMessage(to_send);
                    to_send = encryppted_m[0] + "!" + encryppted_m[1];

                    byte[] bytes = to_send.getBytes();
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    byte[] bytes_received = new byte[1000];
                    dIn.read(bytes_received); //receiving bytes message from server

                    //decryption
                    String decryptedMessage = new String(bytes_received);
                    String[] decryptedMessage_iv_and_m = decryptedMessage.trim().split("!");
                    decryptedMessage = decryptMessage(decryptedMessage_iv_and_m[0], decryptedMessage_iv_and_m[1]);

                    String[] list_of_both = decryptedMessage.trim().split("-"); // first index will be created and second voted for
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
    public static String[] encryptMessage(String message) throws Exception {
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
}