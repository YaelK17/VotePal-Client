package com.example.openingscreen;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class election_adapter extends ArrayAdapter<election_details>{
    private Context mcontext;
    private int mResource;

    public election_adapter(@NonNull Context context, int resource, @NonNull ArrayList<election_details> objects) {
        super(context, resource, objects);
        this.mcontext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mcontext);
        convertView = layoutInflater.inflate(mResource, parent, false);
        ImageView imageView = convertView.findViewById(R.id.delete_image);
        TextView election_title = convertView.findViewById(R.id.election_title);
        TextView due_date = convertView.findViewById(R.id.due_date);
        imageView.setImageResource(getItem(position).getImage());
        election_title.setText(getItem(position).getElection_name());
        due_date.setText(getItem(position).getDue_date());

        // Set OnClickListener on photoImageView
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mcontext, "election_option selected: " + getItem(position).getElection_name(), Toast.LENGTH_SHORT).show();

            }
        });

        return convertView;
    }

}
