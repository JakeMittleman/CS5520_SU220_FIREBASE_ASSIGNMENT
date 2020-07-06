package com.example.firebaseassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.Scanner;

public class StickerActivity extends AppCompatActivity {

    private String SERVER_KEY;
    private String CLIENT_REGISTRATION_TOKEN;

    // SECOND_DEVICE_TOKEN is the ID for my phone. I use the emulator as one device, my pixel 2 as another.
    // Comment this out in onCreate() and hard code your own device ID in for testing purposes.
    private String SECOND_DEVICE_TOKEN;
    private String username;

    private TextView usernameTextView;
    private TextView serverTextView;
    private Button sendMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker);

        // These came over from MainActivity in the intent's extras
        SERVER_KEY = getIntent().getStringExtra("SERVER_KEY");
        CLIENT_REGISTRATION_TOKEN = getIntent().getStringExtra("CLIENT_REGISTRATION_TOKEN");
        username = getIntent().getStringExtra("username");

        // My personal device. Comment out and add your own for testing purposes.
        SECOND_DEVICE_TOKEN = "e9RI84qydLE:APA91bFSy88Adx2kzWmsxyyEEaVc-PzkMyP_cAcbWomDSR6PVnkw1V5mfCLFKm9_aY-kAPKCv6j2ISDw7oE9pggrl-MQPs-aIuOnWuY__dDEuRj5n8K0lWLo5aas2bc8I0nrDiY0wkBX";

        usernameTextView = findViewById(R.id.usernameTextView);
        serverTextView = findViewById(R.id.serverTextView);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // This button initiates a hard coded message, defined in sendMessage(), to be sent to the SECOND_DEVICE_TOKEN
        sendMessageButton.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessage(SECOND_DEVICE_TOKEN);
                }
            }).start();
        });

        // Included for testing purposes
        usernameTextView.setText(username);
        serverTextView.setText(CLIENT_REGISTRATION_TOKEN);
    }


    /**
     * This method is referenced from Dr. Feinberg Firebase Demo sample code,
     * in FCMActivity, sendMessageToDevice()
     * The sample code makes up the basic structure of sending a message to a firebase client ID
     *
     * @param clientToken the token of the client we are sending to
     */
    public void sendMessage(String clientToken) {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
        JSONObject jdata = new JSONObject();

        try {
            jNotification.put("title", "Message Title");
            jNotification.put("body", "Message body ");
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");


            jdata.put("title", "data title");
            jdata.put("content", "data content");

            /***
             * The Notification object is now populated.
             * Next, build the Payload that we send to the server.
             */

            // If sending to a single client
            jPayload.put("to", clientToken); // CLIENT_REGISTRATION_TOKEN);

            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
            jPayload.put("data", jdata);


            /***
             * The Payload object is now populated.
             * Send it to Firebase to send the message to the appropriate recipient.
             */
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);


            //TODO: This is the debugging response given back to the sender. This probably doesn't need to
            // be include in the final app
            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(StickerActivity.this, resp, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is copied from Dr. Feinberg Firebase Demo example code, from FCMActivity
     */
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }



}