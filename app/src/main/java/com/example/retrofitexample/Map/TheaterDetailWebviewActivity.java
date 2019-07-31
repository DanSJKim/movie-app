package com.example.retrofitexample.Map;

import android.content.Intent;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.retrofitexample.R;

public class TheaterDetailWebviewActivity extends AppCompatActivity {

    private static final String TAG = "TheaterDetailWebview";
    private WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater_detail_webview);
        Log.d(TAG, "onCreate: ");

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Log.d(TAG, "onCreate: url: " + url);

        webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);

        MaterialButton button = (MaterialButton) findViewById(R.id.btnBack);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
