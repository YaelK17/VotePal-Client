package com.example.openingscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ResultsActivity extends AppCompatActivity {
    // Create the object of TextView of the candidates names
    TextView[] candidates_names_textview;
    TextView[] candidates_names_nextto_colors_textview;
    // Create the object of TextView of the candidates percentage
    TextView[] percentages_textview;
    // Create PieChart class
    PieChart pieChart;
    String[] percentages_from_server, names;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        button_dialog_related();

        candidates_names_textview = new TextView[]{findViewById(R.id.first_candidate), findViewById(R.id.second_candidate), findViewById(R.id.third_candidate), findViewById(R.id.forth_candidate), findViewById(R.id.fifth_candidate), findViewById(R.id.sixth_candidate)};
        percentages_textview = new TextView[]{findViewById(R.id.first_percentage), findViewById(R.id.second_percentage), findViewById(R.id.third_percentage), findViewById(R.id.forth_percentage), findViewById(R.id.fifth_percentage), findViewById(R.id.sixth_percentage)};
        candidates_names_nextto_colors_textview = new TextView[]{findViewById(R.id.candidate_first),findViewById(R.id.candidate_second),findViewById(R.id.candidate_third), findViewById(R.id.candidate_forth), findViewById(R.id.candidate_fifth), findViewById(R.id.candidate_sixth)};
        // Link those objects with their respective
        // id's that we have given in .XML file

        pieChart = findViewById(R.id.piechart);

        // Get the percentages and names from server
        //receiving_results();
        names = new String[]{"yael", "batel", "rachel"};
        percentages_from_server = new String[]{"30", "40", "30"};
        // Creating a method setData()
        // to set the text in text view and pie chart

        //setData( percentages_from_server, names);
        receiving_results();


    }
    private void setData(String[] percentages_from_server, String[] names)
    {
        String[] colors = {"#FFA726", "#99ccff", "#FF000000", "#66BB6A", "#EF5350", "#29B6F6"};
        Integer candidates_number = names.length;
        String text_to_set;

        // Set the names of every candidate
        for (int i = 0; i < candidates_number; i++) {
            candidates_names_textview[i].setText(names[i]);
            candidates_names_nextto_colors_textview[i].setText(names[i]);
            // Set the percentage of every candidate
            percentages_textview[i].setText(percentages_from_server[i] + "%");
        }

        // Set the rest gone (not visible)
        for (int i = candidates_number; i < candidates_names_textview.length; i++) {
            candidates_names_textview[i].setVisibility(View.GONE);
            candidates_names_nextto_colors_textview[i].setVisibility(View.GONE);
            percentages_textview[i].setVisibility(View.GONE);
        }

        // Set the data and color to the pie chart
        for (int i = 0; i < candidates_number; i++){
            pieChart.addPieSlice(
                    new PieModel(
                            names[i],  //legend lable
                            Integer.parseInt(percentages_textview[i].getText().toString().substring(0, percentages_textview[i].getText().toString().length() - 1)), // the percent
                            Color.parseColor(colors[i])));  // the color string
        }

        // To animate the pie chart
        pieChart.startAnimation();
    }
    private void receiving_results() {
        // function receives the candidates from the server
        Intent previousIntent = getIntent();
        String info = previousIntent.getStringExtra("detail"); // received the election choice

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Client client = Client.getClient_instance();
                    Socket socket = client.getSocket();
                    DataOutputStream dOut = client.getdout();
                    DataInputStream dIn = client.getdin();
                    String to_send = "results" + "-" + "Uid_doesnt_matter" + "-" + info;  // sending all in one message
                    byte[] bytes = to_send.getBytes(); //sending the user id to server
                    dOut.write(bytes);
                    dOut.flush(); // send off the data
                    String s ;
                    byte[] bytes_received = new byte[1000];
                    dIn.read(bytes_received); //receiving bytes message from server
                    s = new String(bytes_received, StandardCharsets.UTF_8); //converting bytes to string
                    if (s.equals("no_votes")){
                        names = new String[]{"yael", "batel", "rachel"};
                        percentages_from_server = new String[]{"30", "40", "30"};
                        setData( percentages_from_server, names);
                    }
                    else{
                        String[] list_of_candidates_and_persetages = s.trim().split(",");  // now we have a list of candidates and percentages

                        // we know for sure the array will have even items because it has names and percentage for each name which we received from server
                        int halfSize = list_of_candidates_and_persetages.length / 2;

                        names = new String[halfSize];
                        percentages_from_server = new String[halfSize];

                        // Copy elements from the original array to the first and second half arrays
                        System.arraycopy(list_of_candidates_and_persetages, 0, names, 0, halfSize);
                        System.arraycopy(list_of_candidates_and_persetages, halfSize, percentages_from_server, 0, halfSize);
                        setData( percentages_from_server, names);
                    }



                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();

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
}