package com.example.firebaseassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
            }
        });

        enterButton.setOnClickListener(v -> {
            System.out.println(CLIENT_REGISTRATION_TOKEN);
            Intent stickerActivity = new Intent(getApplicationContext(), StickerActivity.class);

            stickerActivity.putExtra("username", usernameEditText.getText().toString());
            stickerActivity.putExtra("CLIENT_REGISTRATION_TOKEN", CLIENT_REGISTRATION_TOKEN);
            stickerActivity.putExtra("SERVER_KEY", SERVER_KEY);

            if (stickerActivity.resolveActivity(getPackageManager()) != null) {
                startActivity(stickerActivity);
            }

        });

    }
}