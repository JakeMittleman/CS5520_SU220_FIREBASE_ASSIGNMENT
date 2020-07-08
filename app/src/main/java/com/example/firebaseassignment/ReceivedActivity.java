package com.example.firebaseassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class ReceivedActivity extends AppCompatActivity {

    private ImageView receivedImageView;
    private String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received);

        image = getIntent().getStringExtra("sticker");

        receivedImageView = findViewById(R.id.receivedImageView);

        receivedImageView.setImageDrawable(getDrawable(Integer.parseInt(image)));
    }
}