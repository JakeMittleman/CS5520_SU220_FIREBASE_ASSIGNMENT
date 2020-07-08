package com.example.firebaseassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseassignment.models.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/*
I was afraid of losing your work so I just duplicated it. If you like what I did you can just delete
the other activity *shrug*. I looked in the manifest, it should be fine to just delete it. If you do
make sure you delete the layout and the entry in the manifest also :)
 */
public class StickerActivityWithImages extends AppCompatActivity {

    private String SERVER_KEY;
    private String CLIENT_REGISTRATION_TOKEN;

    // SECOND_DEVICE_TOKEN is the ID for my phone. I use the emulator as one device, my pixel 2 as another.
    // Comment this out in onCreate() and hard code your own device ID in for testing purposes.
    private String SECOND_DEVICE_TOKEN;
    private String username;

    private Button sendImageButton;
    private int selectedSticker = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_with_images);

        // These came over from MainActivity in the intent's extras
        SERVER_KEY = getIntent().getStringExtra("SERVER_KEY");
        CLIENT_REGISTRATION_TOKEN = getIntent().getStringExtra("CLIENT_REGISTRATION_TOKEN");
        username = getIntent().getStringExtra("username");
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Jon's Device Token
        //SECOND_DEVICE_TOKEN = "e9RI84qydLE:APA91bFSy88Adx2kzWmsxyyEEaVc-PzkMyP_cAcbWomDSR6PVnkw1V5mfCLFKm9_aY-kAPKCv6j2ISDw7oE9pggrl-MQPs-aIuOnWuY__dDEuRj5n8K0lWLo5aas2bc8I0nrDiY0wkBX";

        // Jake's Device Token
        SECOND_DEVICE_TOKEN = "ey2SVu7mF8w:APA91bGnGZSRH0J-0Z_PpyE02bj0YNaD6MGVMaUDLhVlYBgaSJP-jWXXwBaOYBj2YcEzbPiV6hrwxd2E7VHOQw2aJslNQ7OpO8gCE8Nk3ocwaQIxeNLKT4IWGEVj8xTNqnjDDdGxNaQF";

        sendImageButton = findViewById(R.id.sendImageButton);

        database.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);
                if (dataSnapshot.getKey().equalsIgnoreCase(username)) {
                    TextView userInfo = (TextView) findViewById(R.id.userInfoText);
                    userInfo.setText(
                            String.format(
                                    "username: %s\n" +
                                            "client reg token: %s\n" +
                                            "sentCount: %s\n",
                                    user.username,
                                    user.CLIENT_REGISTRATION_TOKEN,
                                    user.sentCount
                            )
                    );
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // This button initiates a hard coded message, defined in sendMessage(), to be sent to the SECOND_DEVICE_TOKEN
        sendImageButton.setOnClickListener(v -> {
            if (selectedSticker != -1) {
                increaseSentCount(database);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendMessage(SECOND_DEVICE_TOKEN);
                    }
                }).start();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("In order to send a sticker you must select one!")
                        .show();
            }
        });
    }

    private void increaseSentCount(DatabaseReference database) {
        database.child("users").child(username).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                User user = mutableData.getValue(User.class);
                if (user == null) {
                    return Transaction.success(mutableData);
                }

                user.sentCount = user.sentCount + 1;

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("onComplete", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    /*
    This sets 'selected sticker' as the ID for the actual image, not the imageview.
    This allows us to pass it around and populate it elsewhere when we need to.
     */
    public void onImageClick(View view) {
        switch (view.getId()) {
            case R.id.goggleImage:
                selectedSticker = R.drawable.icon_001_goggles;
                break;
            case R.id.faceMaskImage:
                selectedSticker = R.drawable.icon_002_face_mask;
                break;
        }
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
            jNotification.put("tag", "" + selectedSticker);


            jdata.put("title", "data title");
            jdata.put("content", "data content");
            jdata.put("image", "" + selectedSticker);

            /*
             * The Notification object is now populated.
             * Next, build the Payload that we send to the server.
             */

            // If sending to a single client
            jPayload.put("to", clientToken); // CLIENT_REGISTRATION_TOKEN);

            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
            jPayload.put("data", jdata);


            /*
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
                    Toast.makeText(StickerActivityWithImages.this, resp, Toast.LENGTH_LONG).show();
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

    /*
    This is for testing purposes and can be deleted
     */
    public void onGetUser(View view) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("users").child(username).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                User user = mutableData.getValue(User.class);
                if (user == null) {
                    return Transaction.success(mutableData);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("onComplete", "postTransaction:onComplete:" + databaseError);
            }
        });
    }
}