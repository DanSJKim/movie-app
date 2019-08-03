package com.example.retrofitexample.Streaming;

import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.retrofitexample.R;
import com.google.android.gms.common.api.internal.StatusCallback;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

public class PlayerActivity extends AppCompatActivity{

    WOWZPlayerView mStreamPlayerView;
    WOWZPlayerConfig mStreamPlayerConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);
        WowzaGoCoder goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-BE46-010C-D7F5-A882-CDEE");

        mStreamPlayerConfig = new WOWZPlayerConfig();
        mStreamPlayerConfig.setIsPlayback(true);
        mStreamPlayerConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
        mStreamPlayerConfig.setApplicationName("app-fc5b");
        mStreamPlayerConfig.setStreamName("fa06bb8a");
        mStreamPlayerConfig.setPortNumber(1935);

//        goCoderBroadcastConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
//        goCoderBroadcastConfig.setPortNumber(1935);
//        goCoderBroadcastConfig.setApplicationName("app-fc5b");
//        goCoderBroadcastConfig.setStreamName("fa06bb8a");
//        goCoderBroadcastConfig.setUsername("client44490");
//        goCoderBroadcastConfig.setPassword("b12dbc74");

        WOWZStatusCallback statusCallback = new StatusCallback();
        //mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mStreamPlayerView.play(mStreamPlayerConfig, statusCallback/*WOWZStatusCallback*/);
            }
        }, 2000);
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    class StatusCallback implements WOWZStatusCallback{
        @Override
        public void onWZStatus(WOWZStatus wzStatus) {
            Log.d("1", "onWZStatus: " + wzStatus);
        }
        @Override
        public void onWZError(WOWZStatus wzStatus) {
            Log.d("2", "onWZError: " + wzStatus);
        }
    }
}



