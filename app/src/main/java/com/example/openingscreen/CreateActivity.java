package com.example.openingscreen;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CreateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    Button creating;
    Button finished_creating;

    EditText message_to_send;
    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CreateActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        button_dialog_related();

        message_to_send = findViewById(R.id.messagetosend);

        creating = findViewById(R.id.creating);

        EditText[] candidate_names = {findViewById(R.id.candidate_name1), findViewById(R.id.candidate_name2), findViewById(R.id.candidate_name3), findViewById(R.id.candidate_name4), findViewById(R.id.candidate_name5), findViewById(R.id.candidate_name6)};

        finished_creating = findViewById(R.id.finished_creating);

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
                //client(String.valueOf(message_to_send.getText()), Get_candidate_names(candidate_names), Get_candidate_pictures(candidate_pictures)); // sending the name of the new election
                // sending the name of candidates

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Client client = Client.getClient_instance();
                            Socket socket = client.getSocket();
                            DataOutputStream dOut = client.getdout();
                            DataInputStream dIn = client.getdin();

                            // first send- send "create" which is code word
                            byte[] bytes = "create".getBytes(); //sending the user id to server
                            dOut.write(bytes);
                            dOut.flush(); // send off the data

                            String s = "";
                            byte[] bytes_received = new byte[100];
                            dIn.read(bytes_received); //receiving bytes message from server
                            s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string


                            // second send - sending the election name, names and photos
                            String to_send = message_to_send.getText().toString() + "," + Get_candidate_names(candidate_names) + Get_candidate_pictures(candidate_pictures);
                            bytes = to_send.getBytes(); //sending the user id to server
                            dOut.write(bytes);
                            dOut.flush(); // send off the data

                            s = "";
                            bytes_received = new byte[100];
                            dIn.read(bytes_received); //receiving bytes message from server
                            s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                            if (!s.equals("nameexists")){
                                Toast.makeText(CreateActivity.this,"successfully created",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(CreateActivity.this, "this election name already exist" ,
                                        Toast.LENGTH_LONG).show();
                            }


                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();



            }
        });

        for (int i = 0; i<candidate_pictures.length; i++){
            Pick_picture(candidate_pictures[i]);
        }
    }
    public void button_dialog_related(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.createactivity); //new line added that hopefully will change color
        fab = findViewById(R.id.fab);

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
                overridePendingTransition(0,0);
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout CreateLayout = dialog.findViewById(R.id.layoutCreate);
        LinearLayout ShareLayout = dialog.findViewById(R.id.layoutShare);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        CreateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(CreateActivity.this,"Upload a Video is clicked",Toast.LENGTH_SHORT).show();

            }
        });

        ShareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(CreateActivity.this,"Go live is Clicked",Toast.LENGTH_SHORT).show();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
    private String Get_candidate_names(EditText[] candidate_names){
        // function returns string of names of the candidates
        String string_candidate_names = "";
        for (int i = 0; i< candidate_names.length; i++){
            if ( candidate_names[i].getVisibility() == View.VISIBLE){
                // if the editext is visible
                string_candidate_names += candidate_names[i].getText() + ",";
            }
        }
        return string_candidate_names;
    }
    private String Get_candidate_pictures(ImageView[] candidate_pictures){
        // function returns string of names of the candidates
        String string_candidate_pics = "";
        for (int i = 0; i< candidate_pictures.length; i++){
            if ( candidate_pictures[i].getVisibility() == View.VISIBLE){
                // if the editext is visible
                Uri imageUri = (Uri) candidate_pictures[i].getTag();
                string_candidate_pics += imageUri.toString() + ",";
            }
        }
        return string_candidate_pics;
    }
    private void Pick_picture(ImageView imageview){
        // the function make that when clicked on add image it will add
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        imageview.setImageURI(uri);
                        imageview.setTag(uri);
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
    private void Are_fields_full(TextView[] candidate_names){
        // function checks if all fields are full
        // if they are not then it asks the user to fill it
        for (int i = 0; i< candidate_names.length; i++){
            if (! candidate_names[i].getTag().toString().matches("")){
                // if the editext is not empty
                }
        }
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
    private void client(String name_of_election, String candidate_name, String candidate_pictures) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataOutputStream dOut = client.getdout();
                    DataInputStream dIn = client.getdin();

                    // first send- send "create" which is code word
                    byte[] bytes = "create".getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data

                    String s = "";
                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string


                    // second send - sending the election name
                    bytes = name_of_election.getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data

                    s = ""; // if its the option then it does need to receive an answer in this activity
                    bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    if (!s.equals("nameexists")){
                        // third send - sending the candidate names
                        bytes = candidate_name.getBytes(); //sending the user id to server
                        dOut.write(bytes);
                        dOut.flush(); // send off the data

                        s = ""; // if its the option then it does need to receive an answer in this activity
                        bytes_received = new byte[100];
                        dIn.read(bytes_received); //receiving bytes message from server
                        s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string

                        // forth send (and last) - sending the pictures
                        bytes = candidate_pictures.getBytes(); //sending the user id to server
                        dOut.write(bytes);
                        dOut.flush(); // send off the data

                        s = ""; // if its the option then it does need to receive an answer in this activity
                        bytes_received = new byte[100];
                        dIn.read(bytes_received); //receiving bytes message from server
                        s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string

                    }
                    else{
                        Toast.makeText(CreateActivity.this, "this election name already exist" ,
                                Toast.LENGTH_LONG).show();
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
        Toast.makeText(CreateActivity.this, selected_number_of_candidates ,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}