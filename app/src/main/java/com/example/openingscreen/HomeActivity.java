package com.example.openingscreen;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    FirebaseAuth auth;
    Button logout_btn;
    TextView email;
    FirebaseUser user;
    TextView server_message;
    Button choose_btn;
    EditText message_to_send;
    String[] election_options = {"election4", "elections1", "gvnhf"};
    AutoCompleteTextView autocompleteTxt;
    ArrayAdapter<String> adapter_election_options;
    String election_option;
    Button creating;
    Button finished_creating;

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

        EditText[] candidate_names = {findViewById(R.id.candidate_name1), findViewById(R.id.candidate_name2), findViewById(R.id.candidate_name3), findViewById(R.id.candidate_name4), findViewById(R.id.candidate_name5), findViewById(R.id.candidate_name6)};

        finished_creating = findViewById(R.id.finished_creating);

        autocompleteTxt = findViewById(R.id.auto_complete_txt);
        adapter_election_options = new ArrayAdapter<String>(this,R.layout.list_elections_options, election_options);

        autocompleteTxt.setAdapter(adapter_election_options);
        ImageView[] candidate_pictures = {findViewById(R.id.adding_photo_one), findViewById(R.id.adding_photo_two), findViewById(R.id.adding_photo_three), findViewById(R.id.adding_photo_four), findViewById(R.id.adding_photo_five), findViewById(R.id.adding_photo_six)};
        // image picker


        //candidates number options
        Spinner spinner = (Spinner) findViewById(R.id.planets_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.planets_array,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);



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
                spinner.setVisibility(View.VISIBLE);
            }
        });

        finished_creating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client("create", String.valueOf(message_to_send.getText()));
                client(Get_candidate_names(candidate_names), ""); // sending the name of the new election
                // sending the name of candidates
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

        for (int i = 0; i<candidate_pictures.length; i++){
            Pick_picture(candidate_pictures[i]);
        }


    }
    private String Get_candidate_names(EditText[] candidate_names){
        // function returns string of names of the candidates
        String string_candidate_names = "";
        for (int i = 0; i< candidate_names.length; i++){
            if (! candidate_names[i].getText().toString().matches("")){
                // if the editext is not empty
                string_candidate_names += candidate_names[i].getText() + ",";
            }
        }
        return string_candidate_names;
    }
    private void Pick_picture(ImageView imageview){
        // the function make that when clicked on add image it will add
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the photo picker and let the user choose only images.
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
    }
    private void Are_fields_full(){
        // function checks if all fields are full
        // if they are not then it asks the user to fill it

        //todo
    }
    private void SetVisible(int number_of_candidates, EditText[] candidate_names, ImageView[] candidate_photos){
        // function make the creating election options visible according to the number of candidates
        finished_creating.setVisibility(View.VISIBLE);

        for (int i = 0; i < number_of_candidates; i++){
            // sets the candidates to structure visible
            candidate_names[i].setVisibility(View.VISIBLE);
            candidate_photos[i].setVisibility(View.VISIBLE);
        }
        for (int i = number_of_candidates; i < candidate_photos.length; i++){
            // sets the rest GONE
            candidate_names[i].setVisibility(View.GONE);
            candidate_photos[i].setVisibility(View.GONE);
        }

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item is selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos).
        EditText[] candidate_names = {findViewById(R.id.candidate_name1), findViewById(R.id.candidate_name2), findViewById(R.id.candidate_name3), findViewById(R.id.candidate_name4), findViewById(R.id.candidate_name5), findViewById(R.id.candidate_name6)};
        ImageView[] candidate_pictures = {findViewById(R.id.adding_photo_one), findViewById(R.id.adding_photo_two), findViewById(R.id.adding_photo_three), findViewById(R.id.adding_photo_four), findViewById(R.id.adding_photo_five), findViewById(R.id.adding_photo_six)};
        String selected_number_of_candidates = parent.getItemAtPosition(position).toString();
        SetVisible(Integer.valueOf(selected_number_of_candidates), candidate_names, candidate_pictures);
        Toast.makeText(HomeActivity.this, selected_number_of_candidates ,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}