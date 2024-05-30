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
        socket=new Socket("192.168.1.32", 1234);
        dout = new DataOutputStream(socket.getOutputStream());
        din = new DataInputStream(socket.getInputStream());

//        // Read the length of the public key bytes
//        int publicKeyLength = din.readInt();
//
//        // Create a byte array to hold the public key bytes
//        byte[] publicKeyBytes = new byte[publicKeyLength];
//
//        // Read the public key bytes
//        din.readFully(publicKeyBytes);
//
//        // Convert the received bytes into a PublicKey object
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
//        publicKey = keyFactory.generatePublic(keySpec);
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
//    private byte[] encrypt(String plaintext, PublicKey publicKey) {
//        try {
//            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            return cipher.doFinal(plaintext.getBytes());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    //DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
    //DataInputStream dIn = new DataInputStream(socket.getInputStream());
}
