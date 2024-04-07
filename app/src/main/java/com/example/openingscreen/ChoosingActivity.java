package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ChoosingActivity extends AppCompatActivity {
    Button send_choice;
    TextView title_election_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);
        send_choice = findViewById(R.id.candidate_choice);
        title_election_name = findViewById(R.id.election_name);
        RadioButton[] buttons = {findViewById(R.id.radio_option1), findViewById(R.id.radio_option2), findViewById(R.id.radio_option3), findViewById(R.id.radio_option4), findViewById(R.id.radio_option5), findViewById(R.id.radio_option6)};
        receiving_candidates(buttons);

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
                    byte[] bytes = "votefor".getBytes(); //sending votefor to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server

                    bytes = title_election_name.getText().toString().getBytes(); //sending the election name to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server

                    bytes = choice.getBytes(); //sending the choice to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    Toast.makeText(ChoosingActivity.this, s ,
                            Toast.LENGTH_LONG).show();
                    // moving back to home
                    Intent intent = new Intent(ChoosingActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private String[] RemoveFirstElement(String[] arr) {
        // function returns the array without its first element
        String[] newArr = new String[arr.length - 1];
        for (int i = 1; i < arr.length; i++) {
            newArr[i-1] = arr[i];
        }
        return newArr;
    }
    private void receiving_candidates(RadioButton[] buttons) {
        // function receives the candidates from the server
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataInputStream dIn = client.getdin();
                    DataOutputStream dOut = client.getdout();

                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    String[] list_of_candidate = s.trim().split(",");
                    title_election_name.setText(list_of_candidate[0]); // sets the title to be name of election
                    list_of_candidate = RemoveFirstElement(list_of_candidate); // removes the name of the election
                    for (int i = 0; i<list_of_candidate.length; i++){
                        buttons[i].setText(list_of_candidate[i]);
                    }

                    for (int i = list_of_candidate.length; i<buttons.length; i++){
                        buttons[i].setVisibility(View.GONE);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}