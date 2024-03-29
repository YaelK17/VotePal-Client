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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);
        send_choice = findViewById(R.id.candidate_choice);
        RadioButton[] buttons = {findViewById(R.id.radio_option1), findViewById(R.id.radio_option2), findViewById(R.id.radio_option3), findViewById(R.id.radio_option4), findViewById(R.id.radio_option5), findViewById(R.id.radio_option6)};
        receiving_candidates(buttons);

        send_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
            }
        });
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

                    byte[] bytes_received = new byte[100];
                    dIn.read(bytes_received); //receiving bytes message from server
                    String s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    String[] list_of_candidate = s.trim().split(",");
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