package com.example.firebaseassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebaseassignment.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_KEY = "key=AAAAwxr5f20:APA91bEdVeYAllDVcJzzRyWmcnYohVU_7h_F8PEdDi73VN8kUGEY2v-bhsF26QPlumgtD523Fv25lOA7sZKe2WqsLGhl44xFWhXrDVBmaRUTHFo2YKNsKhkJAjzVVbBOU5tm6duEOfzh";
    private static String CLIENT_REGISTRATION_TOKEN;
    private static String username;

    private EditText usernameEditText;
    private Button enterButton;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameEditText = findViewById(R.id.usernameEditText);
        enterButton = findViewById(R.id.enterButton);

        // Referenced (in part) from Dr. Feinberg sample code, FCM Main Activity, onCreate()
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                CLIENT_REGISTRATION_TOKEN = instanceIdResult.getToken();
                database = FirebaseDatabase.getInstance().getReference();

                //TODO: I'm not sure where this logic belongs. But here might be an option.
                // - After getting the token, we might want to go to the database and get the
                // username associated with the token (if there is one). If we allow someone to enter the same
                // username that is associated with the token, it will wipe the user's data
                // and replace it as a new user (this was a piazza post).
            }
        });

        /*
        These are triggers for when our data changes in the database. If the database notices
        that info has changed in the 'users' table, it'll call one of these based on the condition.
        We may not need it, but we may. I'm putting the skeleton here in-case you want to use it.
        It doesn't do anything as it is so you can comment it out if you want.
         */
        database.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

        // Set listener for enterButton
        enterButton.setOnClickListener(v -> {
            Intent stickerActivity = new Intent(getApplicationContext(), StickerActivityWithImages.class);

            MainActivity.this.storeUserInfo(
                    usernameEditText.getText().toString(),
                    CLIENT_REGISTRATION_TOKEN
                    );

            stickerActivity.putExtra("username", usernameEditText.getText().toString());
            stickerActivity.putExtra("CLIENT_REGISTRATION_TOKEN", CLIENT_REGISTRATION_TOKEN);
            stickerActivity.putExtra("SERVER_KEY", SERVER_KEY);

            if (stickerActivity.resolveActivity(getPackageManager()) != null) {
                startActivity(stickerActivity);
            }
        });

        // This code is relevant if we click a notification message from another device. See
        // documentation on extractDataFromNotification
        // Referenced from Dr. Feinberg Firebase sample code, DemoMessagingService/MainActivity
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if (extras != null) {
            extractDataFromNotification(extras);
        }

    }

    /**
     * This should add a user to te database. I haven't tested it. I'm so tired.
     * @param username the username of the user
     * @param clientRegToken the registration token of the user
     */
    private void storeUserInfo(String username, String clientRegToken) {
        database.child("users")
                .child(username)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        User u = mutableData.getValue(User.class);
                        if (u == null) {
                            return Transaction.success(mutableData);
                        }

                        u.username = username;
                        u.CLIENT_REGISTRATION_TOKEN = clientRegToken;
                        mutableData.setValue(u);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                    }
                });
    }

    /**
     * This method is called once the user clicks a notification. Upon receiving and selecting the
     * notification, user is taken back to the app, and then this method is called.
     * Copied from Dr. Feinberg Firebase sample code, DemoMessagingService/MainActivity
     * @param extras
     */
    private void extractDataFromNotification(Bundle extras) {
        //TODO: how do we launch a specific activity? right now, clicking the notification
        // brings us to the MainActivity, and we get the data there

        String dataTitle = extras.getString("title", "Nothing");
        String dataContent = extras.getString("content", "Nothing");
        postToastMessage("Received : " + dataTitle + " " + dataContent);
    }

    /**
     * Copied from Dr. Feinberg Firebase sample code, DemoMessagingService/MainActivity
     * @param message
     */
    private void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}