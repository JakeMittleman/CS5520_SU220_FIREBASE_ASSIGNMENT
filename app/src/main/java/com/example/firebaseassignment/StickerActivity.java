package com.example.firebaseassignment;

import androidx.appcompat.app.AppCompatActivity;

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
    private String PIXEL_TOKEN;
    private String username;

    private TextView usernameTextView;
    private TextView serverTextView;
    private Button sendMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker);

        SERVER_KEY = getIntent().getStringExtra("SERVER_KEY");
        CLIENT_REGISTRATION_TOKEN = getIntent().getStringExtra("CLIENT_REGISTRATION_TOKEN");
        username = getIntent().getStringExtra("username");
        PIXEL_TOKEN = "e9RI84qydLE:APA91bFSy88Adx2kzWmsxyyEEaVc-PzkMyP_cAcbWomDSR6PVnkw1V5mfCLFKm9_aY-kAPKCv6j2ISDw7oE9pggrl-MQPs-aIuOnWuY__dDEuRj5n8K0lWLo5aas2bc8I0nrDiY0wkBX";

        usernameTextView = findViewById(R.id.usernameTextView);
        serverTextView = findViewById(R.id.serverTextView);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        sendMessageButton.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessage(PIXEL_TOKEN);
                }
            }).start();

        });

        usernameTextView.setText(username);
        serverTextView.setText(CLIENT_REGISTRATION_TOKEN);
    }


    /**
     * This method is referenced (in part) from Dr. Feinberg Firebase Demo example code. The
     * example code makes up the basic structure of sending a message to a firebase client ID
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


            jdata.put("title","data title");
            jdata.put("content","data content");

            /***
             * The Notification object is now populated.
             * Next, build the Payload that we send to the server.
             */

            // If sending to a single client
            jPayload.put("to", clientToken); // CLIENT_REGISTRATION_TOKEN);

            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
            jPayload.put("data",jdata);


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

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(StickerActivity.this, resp,Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is copied from Dr. Feinberg Firebase Demo example code, FCMActivity
     */
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }


}