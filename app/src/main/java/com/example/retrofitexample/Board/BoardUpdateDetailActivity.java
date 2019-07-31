package com.example.retrofitexample.Board;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;

public class BoardUpdateDetailActivity extends AppCompatActivity {

    public static final String TAG = "BoardUpdateDetailAct : ";
    String imgPath;
    ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_update_detail);

        img = (ImageView) findViewById(R.id.ivBoardUpdateImageDetail);

        Intent intent = getIntent();
        imgPath = intent.getStringExtra("imgPath");
        Log.d(TAG, "onCreate: imgPath: " + imgPath);

        //Uri이미지면
        if(imgPath.contains("content://")){
            GlideApp.with(this).load(imgPath).override(500, 500).into(img);
        //아니면
        }else {
            GlideApp.with(this).load("http://13.209.49.7/movieApp"+imgPath).override(500, 500).into(img);
        }
    }
}
