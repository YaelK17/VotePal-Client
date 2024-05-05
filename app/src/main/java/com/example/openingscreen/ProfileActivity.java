package com.example.openingscreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ListView listView_creations, listView_voted_in;

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

        //todo change into what we receive from server
        arrayList_of_created_elections.add(new election_details(R.drawable.baseline_delete_24, "elections1", "due date: 10/5/2020"));
        arrayList_of_created_elections.add(new election_details(R.drawable.baseline_delete_24, "2", "due date: 19/5/2120"));
        arrayList_of_created_elections.add(new election_details(R.drawable.baseline_delete_24, "3", "due date: 1/7/2024"));
        arrayList_of_created_elections.add(new election_details(R.drawable.baseline_delete_24, "4", "due date: 10/2/2021"));

        election_adapter created_electionAdapter = new election_adapter(this, R.layout.list_view_of_created, arrayList_of_created_elections);

        listView_creations.setAdapter(created_electionAdapter);  // setting the adapter


        ArrayList<election_details> arrayList_of_voted_elections = new ArrayList<>(); // arraylist for the elections the user created

        //todo change into what we receive from server
        arrayList_of_voted_elections.add(new election_details(R.drawable.baseline_voted_vi_circle_24, "elections1", "due date: 10/5/2020"));
        arrayList_of_voted_elections.add(new election_details(R.drawable.baseline_voted_vi_circle_24, "2", "due date: 19/5/2120"));
        arrayList_of_voted_elections.add(new election_details(R.drawable.baseline_voted_vi_circle_24, "3", "due date: 1/7/2024"));
        arrayList_of_voted_elections.add(new election_details(R.drawable.baseline_voted_vi_circle_24, "4", "due date: 10/2/2021"));

        election_adapter voted_electionAdapter = new election_adapter(this, R.layout.list_view_of_created, arrayList_of_voted_elections);

        listView_voted_in.setAdapter(voted_electionAdapter);  // setting the adapter


//        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String choice = parent.getItemAtPosition(position).toString();
//                Toast.makeText(getApplicationContext(), "election_option selected: " + choice, Toast.LENGTH_SHORT).show();
//                client("option", ("op" + choice));  // sending choice to server
//                Intent intent = new Intent(ProfileActivity.this, ChoosingActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });






    }
}