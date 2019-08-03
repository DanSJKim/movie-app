package com.example.retrofitexample.Streaming;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.retrofitexample.Board.BoardActivity;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.R;

public class StreamingListActivity extends AppCompatActivity {

    Button btnStart;
    Button btnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_list);

        btnStart = (Button) findViewById(R.id.btnStreamingStart);
        btnPlay = (Button) findViewById(R.id.btnPlay);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StreamingListActivity.this, StreamingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StreamingListActivity.this, PlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

    }
}
