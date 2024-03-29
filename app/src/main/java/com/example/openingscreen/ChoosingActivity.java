package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ChoosingActivity extends AppCompatActivity {
    Button send_choice;
    RadioButton one;
    RadioButton two;
    RadioButton three;
    RadioButton four;
    RadioButton five;
    RadioButton six;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);
        send_choice = findViewById(R.id.candidate_choice);
        one = findViewById(R.id.radio_option1);
        two = findViewById(R.id.radio_option2);
        three = findViewById(R.id.radio_option3);
        four = findViewById(R.id.radio_option4);
        five = findViewById(R.id.radio_option5);
        six = findViewById(R.id.radio_option6);
        receiving_candidates();

        send_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
            }
        });
    }
    private void receiving_candidates() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataInputStream dIn = client.getdin();

                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    six.setText(s); //setting message to be message from server
                    String[] list_of_candidate = s.trim().split(",");
                    int i = 0;
                    if (list_of_candidate.length == 3){
                        one.setText(list_of_candidate[0]);
                        two.setText(list_of_candidate[1]);
                        three.setText(list_of_candidate[2]);
                        four.setVisibility(View.GONE);
                        five.setVisibility(View.GONE);
                        six.setVisibility(View.GONE);
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