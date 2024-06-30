package com.example.openingscreen;

import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.inject.Singleton;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class Client {
   private static Client client_instance = null;
   private Socket socket;
   private DataInputStream din;
   private DataOutputStream dout;
//   private  PublicKey publicKey;


    private Client() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        socket=new Socket("192.168.1.29", 1234);
        dout = new DataOutputStream(socket.getOutputStream());
        din = new DataInputStream(socket.getInputStream());
// Receive the public key bytes from the server
//        byte[] publicKeyBytes = new byte[1000];
//        din.read(publicKeyBytes); // Read the bytes into the array
//
//        // Convert the received bytes into a PublicKey object
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//        publicKey = keyFactory.generatePublic(keySpec);
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    // Receive the public key bytes from the server
//                    byte[] publicKeyBytes = new byte[1000];
//                    din.read(publicKeyBytes); // Read the bytes into the array
//
//                     // Convert the received bytes into a PublicKey object
//                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//                    publicKey = keyFactory.generatePublic(keySpec);
//                    }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
    }

    public static synchronized Client getClient_instance() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (client_instance == null) {
            client_instance = new Client();
        }
        return client_instance;
    }

    public Socket getSocket() {
        return socket;
    }
    public DataOutputStream getdout(){
        return dout;
    }
    public DataInputStream getdin(){
        return din;
    }
//    public  PublicKey getPublicKey(){return publicKey;}


    //DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
    //DataInputStream dIn = new DataInputStream(socket.getInputStream());
}
