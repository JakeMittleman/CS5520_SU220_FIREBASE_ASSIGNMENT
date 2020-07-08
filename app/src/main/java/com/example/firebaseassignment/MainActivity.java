package com.example.firebaseassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
            }
        });

        // Set listener for enterButton
        enterButton.setOnClickListener(v -> {
            username = usernameEditText.getText().toString();
            Intent stickerActivity = new Intent(getApplicationContext(), StickerActivityWithImages.class);

            MainActivity.this.createUser(
                    usernameEditText.getText().toString(),
                    CLIENT_REGISTRATION_TOKEN
                    );

            stickerActivity.putExtra("username", username);
            stickerActivity.putExtra("CLIENT_REGISTRATION_TOKEN", CLIENT_REGISTRATION_TOKEN);
            stickerActivity.putExtra("SERVER_KEY", SERVER_KEY);

            if(username.equals("")) {
                new AlertDialog.Builder(this)
                        .setMessage("You must enter a username!")
                        .show();
            }
            else {
                startActivity(stickerActivity);
            }

        });
    }

    /**
     * This adds a user to the database
     * @param username the username of the user
     * @param clientRegToken the registration token of the user
     */
    private void createUser(String username, String clientRegToken) {
        User user = new User(username, clientRegToken);
        database.child("users").child(username).setValue(user);
    }
}