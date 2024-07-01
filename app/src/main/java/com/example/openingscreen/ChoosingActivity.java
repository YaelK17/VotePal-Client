package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.Manifest;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.net.URI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChoosingActivity extends AppCompatActivity {
    Button send_choice;
    TextView title_election_name;
    FirebaseAuth auth;
    FirebaseUser user;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        button_dialog_related();
        send_choice = findViewById(R.id.candidate_choice);
        title_election_name = findViewById(R.id.election_name);
        RadioButton[] buttons = {findViewById(R.id.radio_option1), findViewById(R.id.radio_option2), findViewById(R.id.radio_option3), findViewById(R.id.radio_option4), findViewById(R.id.radio_option5), findViewById(R.id.radio_option6)};
        ImageView[] photos = {findViewById(R.id.photo_one), findViewById(R.id.photo_two), findViewById(R.id.photo_three), findViewById(R.id.photo_four), findViewById(R.id.photo_five), findViewById(R.id.photo_six)};
        receiving_candidates(buttons, photos);

        send_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Is_one_checked(buttons)){ // if the user chose a candidate
                    String choice = Find_choice(buttons); // find the candidate chosen
                    Sending_choice(choice); // sends the choice to server
                }
                else{
                    Toast.makeText(ChoosingActivity.this, "you must choose a candidate" ,
                            Toast.LENGTH_LONG).show();
                    // make the user know that there must be a choice to send
                }
            }
        });
    }
    private boolean Is_one_checked(RadioButton[] buttons) {
        // checks if a candidate is checked
        for (int i = 0; i < 6; i++) { // checking what button is clicked
            if (buttons[i].isChecked()) {
                return true;
            }
        }
        return false;
    }
    private String Find_choice(RadioButton[] buttons){
        // finds the choice (button checked) and returns it
        String choice = "";
        for (int i=0; i< 6; i++){ // checking what button is clicked
            if(buttons[i].isChecked()){
                return (String) buttons[i].getText();
            }
        }
        return choice;
    }
    private void Sending_choice(String choice) {
        // function sends choice to server
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataInputStream dIn = client.getdin();
                    DataOutputStream dOut = client.getdout();
                    String to_send = "votefor" + "-" + user.getUid() + "-" + title_election_name.getText().toString() + "," + choice;
                    //encryption
                    String[] encryppted_m = encryptMessage(to_send);
                    to_send = encryppted_m[0] + "!" + encryppted_m[1];
                    byte[] bytes = to_send.getBytes(); //sending all info to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    byte[] bytes_received = new byte[1000];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String decryptedMessage = new String(bytes_received);
                    String[] decryptedMessage_iv_and_m = decryptedMessage.trim().split("!");
                    decryptedMessage = decryptMessage(decryptedMessage_iv_and_m[0], decryptedMessage_iv_and_m[1]);
                    Toast.makeText(ChoosingActivity.this, decryptedMessage ,
                            Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        // moving back to home
        Intent intent = new Intent(ChoosingActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    private String[] RemoveFirstElement(String[] arr) {
        // function returns the array without its first element
        String[] newArr = new String[arr.length - 1];
        for (int i = 1; i < arr.length; i++) {
            newArr[i-1] = arr[i];
        }
        return newArr;
    }
    private void receiving_candidates(RadioButton[] buttons, ImageView[] photos) {
        // function receives the candidates from the server
        Intent previousIntent = getIntent();
        String info = previousIntent.getStringExtra("detail");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataOutputStream dOut = client.getdout();
                    DataInputStream dIn = client.getdin();
                    String to_send = "option" + "-" + user.getUid() + "-" + info;  // sending all in one message
                    //encryption
                    String[] encryppted_m = encryptMessage(to_send);
                    to_send = encryppted_m[0] + "!" + encryppted_m[1];

                    byte[] bytes = to_send.getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    byte[] bytes_received = new byte[1000];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String decryptedMessage = new String(bytes_received);
                    String[] decryptedMessage_iv_and_m = decryptedMessage.trim().split("!");
                    decryptedMessage = decryptMessage(decryptedMessage_iv_and_m[0], decryptedMessage_iv_and_m[1]);

                    String[] list_of_pics_and_candidates =  decryptedMessage.trim().split("-");
                    String[] list_of_candidate = list_of_pics_and_candidates[0].trim().split(",");
                    title_election_name.setText(list_of_candidate[0]); // sets the title to be name of election
                    list_of_candidate = RemoveFirstElement(list_of_candidate); // removes the name of the election
                    for (int i = 0; i<list_of_candidate.length; i++){
                        buttons[i].setText(list_of_candidate[i]);
                    }

                    for (int i = list_of_candidate.length; i<buttons.length; i++){
                        buttons[i].setVisibility(View.GONE);
                        photos[i].setVisibility(View.GONE);
                    }

                    String[] list_of_pics =  list_of_pics_and_candidates[1].trim().split(",");
                    for (int i = 0; i<list_of_pics.length; i++){
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Permission is not granted
                            // Request the permission

                            ActivityCompat.requestPermissions(ChoosingActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    123);
                        }
                        else {
                            try {
                                Uri urii = Uri.parse(list_of_pics[i]);
                                photos[i].setImageURI(urii);
                            } catch (NullPointerException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
//                        Glide.with(getApplicationContext())
//                                .load(Uri.parse(list_of_pics[i]))
//                                .diskCacheStrategy(DiskCacheStrategy.NONE) // Optional, if you don't want to cache the image
//                                .skipMemoryCache(true) // Optional, if you want to load the image from scratch every time
//                                .into(photos[i]);
//                        InputStream imageStream = getContentResolver().openInputStream(Uri.parse(list_of_pics[i]));
//
//                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//
//                        photos[i].setImageBitmap(selectedImage);
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();

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

    public void button_dialog_related(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

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
    public void onRadioButtonClicked(View view) {
        // Check which radio button was clicked
        RadioButton clickedRadioButton = (RadioButton) view;
        RadioButton[] buttons = {findViewById(R.id.radio_option1), findViewById(R.id.radio_option2), findViewById(R.id.radio_option3), findViewById(R.id.radio_option4), findViewById(R.id.radio_option5), findViewById(R.id.radio_option6)};
        // Uncheck all radio buttons in the group
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != clickedRadioButton) {
                buttons[i].setChecked(false);
            }
        }
    }

}