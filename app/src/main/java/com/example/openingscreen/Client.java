package com.example.openingscreen;

import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.inject.Singleton;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Client {
   private static Client client_instance = null;
   private Socket socket;
   private DataInputStream din;
   private DataOutputStream dout;

    private Client() throws IOException {
        socket=new Socket("192.168.1.32", 1234);
        dout = new DataOutputStream(socket.getOutputStream());
        din = new DataInputStream(socket.getInputStream());
    }

    public static synchronized Client getClient_instance() throws IOException {
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

    //DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
    //DataInputStream dIn = new DataInputStream(socket.getInputStream());
}
