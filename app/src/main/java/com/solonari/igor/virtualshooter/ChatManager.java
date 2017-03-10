package com.solonari.igor.virtualshooter;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class ChatManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private static final String TAG = "ChatHandler";
    ObjectInputStream in;
    ObjectOutputStream out;
    private final String id = "id";
    private final String ship = "ship";
    //ArrayList<String> line;

    ChatManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }


    @Override
    public void run() {
        try {
            // Create PrintWriter object for sending messages to server.
            out = new ObjectOutputStream(socket.getOutputStream());
            //Create BufferedReader object for receiving messages from server.
            in = new ObjectInputStream(socket.getInputStream());
            Log.d(TAG, "In/Out created");
            handler.obtainMessage(1, this).sendToTarget();

            while (true) {
                try {
                    ArrayList<String> line = (ArrayList) in.readObject();
                    String head = line.get(0);
                    switch (head) {
                        case id:
                            String points = line.get(1);
                            Message msg = handler.obtainMessage(2, points);
                            handler.sendMessage(msg);
                            break;
                        case ship:
                            Message msg = handler.obtainMessage(3, line);
                            handler.sendMessage(msg);
                            break;
                        default:
                            break;
                    }

                } catch (IOException e) {
                    Log.d(TAG, "Cant read message", e);
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, "Cant read this kind of object", e);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "can't create in/out", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "can't close socket", e);
            }
        }
    }

    void sendMessage(ArrayList message) {
        try {
            out.writeObject(message);
        } catch (Exception e){
            Log.d(TAG, "Cant send message", e);
        }
    }
}
