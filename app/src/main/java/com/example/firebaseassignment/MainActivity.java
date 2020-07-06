package com.example.firebaseassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_KEY = "key=AAAAwxr5f20:APA91bEdVeYAllDVcJzzRyWmcnYohVU_7h_F8PEdDi73VN8kUGEY2v-bhsF26QPlumgtD523Fv25lOA7sZKe2WqsLGhl44xFWhXrDVBmaRUTHFo2YKNsKhkJAjzVVbBOU5tm6duEOfzh";
    private static String CLIENT_REGISTRATION_TOKEN;
    private static String username;

    private EditText usernameEditText;
    private Button enterButton;

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

                //TODO: I'm not sure where this logic belongs. But here might be an option.
                // - After getting the token, we might want to go to the database and get the
                // username associated with the token (if there is one). If we allow someone to enter the same
                // username that is associated with the token, it will wipe the user's data
                // and replace it as a new user (this was a piazza post).
            }
        });

        // Set listener for enterButton
        enterButton.setOnClickListener(v -> {
            Intent stickerActivity = new Intent(getApplicationContext(), StickerActivity.class);

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