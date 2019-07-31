package com.example.retrofitexample.Chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

public class ChatImageDetailActivity extends AppCompatActivity {
    public static final String TAG = "ChatImageDetail: ";

    ImageView ivChatDetailImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_detail);

        ivChatDetailImage = (ImageView) findViewById(R.id.ivChatDetailImage);

        Intent intent = getIntent();
        String image = intent.getStringExtra("image");
        Log.d(TAG, "onCreate: image: " + image);

        GlideApp.with(this).load("http://13.209.49.7/movieApp"+image)
                .into(ivChatDetailImage);
    }
}
