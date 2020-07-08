package com.example.firebaseassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class StickerActivityWithImages extends AppCompatActivity {

    private ListView usersListView;
    private ArrayList<User> users;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> userNameList;

    private String SERVER_KEY;
    private String CLIENT_REGISTRATION_TOKEN;

    private String username;

    private Button sendImageButton;
    private int selectedSticker = -1;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_with_images);

        // Set up list view and list of users
        usersListView = findViewById(R.id.usersListView);

        users = new ArrayList<>();
        userNameList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                userNameList);
        usersListView.setAdapter(adapter);

        // These came over from MainActivity in the intent's extras
        SERVER_KEY = getIntent().getStringExtra("SERVER_KEY");
        CLIENT_REGISTRATION_TOKEN = getIntent().getStringExtra("CLIENT_REGISTRATION_TOKEN");
        username = getIntent().getStringExtra("username");
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        sendImageButton = findViewById(R.id.sendImageButton);


        database.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                user = dataSnapshot.getValue(User.class);

                if (!user.username.equals(username)) {
                    users.add(user);
                    userNameList.add(user.username);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                user = dataSnapshot.getValue(User.class);
                if (dataSnapshot.getKey().equalsIgnoreCase(username)) {
                    TextView userInfo = (TextView) findViewById(R.id.userInfoText);
                    userInfo.setText(
                            String.format(
                                    "username: %s\n" +
                                            "sentCount: %s\n",
                                    user.username,
                                    user.sentCount
                            )
                    );
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // No implementation

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // No implementation

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // No implementation

            }
        });

        sendImageButton.setOnClickListener(v -> {
            if (selectedSticker != -1) {
                increaseSentCount(database);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (User user : users) {
                            sendMessage(user.CLIENT_REGISTRATION_TOKEN);
                        }
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
        //TODO: getting  java.lang.NullPointerException: Can't pass null for argument 'pathString' in child(), for line below, sometimes
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