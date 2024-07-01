package com.example.openingscreen;

import android.content.Context;

import android.content.Intent;
import android.util.Base64;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Client client = Client.getClient_instance();
                                    Socket socket = client.getSocket();
                                    DataOutputStream dOut = client.getdout();
                                    DataInputStream dIn = client.getdin();
                                    String to_send = "delete" + "-" + "Uid_doesnt_matter" + "-" + getItem(position).getElection_name();  // sending all in one message
                                    //encryption
                                    String[] encryppted_m = encryptMessage(to_send);
                                    to_send = encryppted_m[0] + "!" + encryppted_m[1];
                                    byte[] bytes = to_send.getBytes(); //sending the user id to server
                                    dOut.write(bytes);
                                    dOut.flush(); // send off the data
                                    String s ;
                                    byte[] bytes_received = new byte[1000];
                                    dIn.read(bytes_received); //receiving bytes message from server
                                    String decryptedMessage = new String(bytes_received);
                                    String[] decryptedMessage_iv_and_m = decryptedMessage.trim().split("!");
                                    decryptedMessage = decryptMessage(decryptedMessage_iv_and_m[0], decryptedMessage_iv_and_m[1]);
                                    if (decryptedMessage.equals("success")){
                                        Toast.makeText(mcontext, "deleted " + getItem(position).getElection_name(), Toast.LENGTH_SHORT).show();

                                    }


                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();

                        Intent intent = new Intent(mcontext, ProfileActivity.class);
                        mcontext.startActivity(intent);
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
    public static String[] encryptMessage(String message) throws Exception {
        String CLIENT_KEY = "PAQfYscxlOFsvGzz"; // Replace with your actual key
        String ALGORITHM = "AES/CBC/PKCS5Padding";
        byte[] iv = getInitializationVector();
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        SecretKeySpec keySpec = new SecretKeySpec(CLIENT_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(message.getBytes());

        // Use Android's Base64 to encode iv and encrypted bytes
        String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
        String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT);

        return new String[]{ivString, encryptedString};
    }

    public static String decryptMessage(String iv, String encryptedText) throws Exception {
        String CLIENT_KEY = "PAQfYscxlOFsvGzz"; // Replace with your actual key
        String ALGORITHM = "AES/CBC/PKCS5Padding";
        byte[] ivBytes = Base64.decode(iv, Base64.DEFAULT);
        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(CLIENT_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encryptedBytes);

        return new String(decrypted);
    }

    private static byte[] getInitializationVector() {
        // Generate a random IV (Initialization Vector)
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

}
