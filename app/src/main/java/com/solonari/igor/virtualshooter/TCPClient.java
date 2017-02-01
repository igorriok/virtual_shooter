package com.solonari.igor.virtualshooter;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by isolo on 1/28/2017.
 */

public class TCPClient {

    private static final String TAG = "TCPClient";
    private final Handler mHandler;
    private String ipNumber, incomingMessage, command;
    BufferedReader in;
    PrintWriter out;
    private MessageCallback listener = null;
    private boolean mRun = false;

    /**
     * TCPClient class constructor, which is created in AsyncTasks
     * @param mHandler Handler passed as an argument for updating the UI with sent message
     * @param listener Callback interface object
     */
    public TCPClient(Handler mHandler, String command, String ipNumber, MessageCallback listener) {
        this.listener = listener;
        this.ipNumber = ipNumber;
        this.command = command;
        this.mHandler = mHandler;
    }

    /**Public method for sending the message via OutputStream object.
     * @param message Message passed as an argument and sent via OutputStream object.
     */
    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
            Log.d(TAG, "Sent Message: " + message);
        }
    }
    /**
     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
     */
    public void stopClient() {
        Log.d(TAG, "Client stopped!");
        mRun = false;
    }

    public void run() {
        mRun = true;
        try {
            // Creating InetAddress object from ipNumber passed via constructor from IpGetter class.
            InetAddress serverAddress = InetAddress.getByName(ipNumber);
            Log.d(TAG, "Connecting...");
            //Here the socket is created with hardcoded port.
            Socket socket = new Socket(serverAddress, 57349);
            try {
                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //Create BufferedReader object for receiving messages from server.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.d(TAG, "In/Out created");
                //Sending message with command specified by AsyncTask
                this.sendMessage(command);
                //Listen for the incoming messages while mRun = true
                while (mRun) {
                    incomingMessage = in.readLine();
                    if (incomingMessage != null && listener != null) {
                        /**Incoming message is passed to MessageCallback object.
                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                         */
                        listener.callbackMessageReceiver(incomingMessage);
                    }
                    incomingMessage = null;
                }
                Log.d(TAG, "Received Message: " + incomingMessage);
            } catch (Exception e) {
                Log.d(TAG, "Error on streamers", e);
            } finally {
                out.flush();
                out.close();
                in.close();
                socket.close();
                Log.d(TAG, "Socket Closed");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error on socket", e);
        }
    }
    /**Callback Interface for sending received messages to 'onPublishProgress' method in AsyncTask.
     */
    public interface MessageCallback {
        /**Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
         * @param message Received message from server app.
         */
        public void callbackMessageReceiver(String message);
    }
}
