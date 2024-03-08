package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.RadioButton;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChoosingActivity extends AppCompatActivity {
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
        one = findViewById(R.id.radio_option1);
        two = findViewById(R.id.radio_option2);
        three = findViewById(R.id.radio_option3);
        four = findViewById(R.id.radio_option4);
        five = findViewById(R.id.radio_option5);
        six = findViewById(R.id.radio_option6);
        receiving_candidates();
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
                    one.setText(s); //setting message to be message from server

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}