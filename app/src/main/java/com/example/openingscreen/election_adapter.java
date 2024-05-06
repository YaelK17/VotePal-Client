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

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
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

        // Set OnClickListener on photoImageView- only if its delete image
        if (getItem(position).getImage() == R.drawable.baseline_delete_24) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create the object of AlertDialog Builder class

                    AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);

                    // Set the message show for the Alert time
                    builder.setMessage("Are you sure you want to delete " + getItem(position).getElection_name() + "?");

                    // Set Alert Title
                    builder.setTitle("Alert !");

                    // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                    builder.setCancelable(false);

                    // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {

                        // When the user click yes button then app will close

                        // todo send to sever the delete

                        Toast.makeText(mcontext, "deleted " + getItem(position).getElection_name(), Toast.LENGTH_SHORT).show();

                    });

                    // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {

                        // If user click no then dialog box is canceled.

                        dialog.cancel();

                    });

                    // Create the Alert dialog
                    AlertDialog alertDialog = builder.create();

                    // Show the Alert Dialog box
                    alertDialog.show();
                }
            });
        }

        return convertView;
    }

}
