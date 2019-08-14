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
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;


/**
 * 방송 시청 액티비티
 */
public class PlayerActivity extends AppCompatActivity{

    WOWZPlayerView mStreamPlayerView; // 방송 플레이어
    WOWZPlayerConfig mStreamPlayerConfig; // 방송 시청에 필요한 설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Log.d("PlayerActivity: ", "onCreate: 방송 시청 액티비티 시작");

        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);
        WowzaGoCoder goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-BE46-010C-D7F5-A882-CDEE");

        // 방송에 필요한 설정을 입력한다.
        mStreamPlayerConfig = new WOWZPlayerConfig();
        mStreamPlayerConfig.setIsPlayback(true);
        mStreamPlayerConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
        mStreamPlayerConfig.setApplicationName("app-fc5b");
        mStreamPlayerConfig.setStreamName("fa06bb8a");
        mStreamPlayerConfig.setPortNumber(1935);

        // WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        mStreamPlayerView.setScaleMode(WOWZMediaConfig.FILL_VIEW);
//        mStreamPlayerConfig.setHLSEnabled(true);
//        mStreamPlayerConfig.setHLSBackupURL("http://[a092d0.entrypoint.cloud.wowza.com]:1935/[app-fc5b]/[fa06bb8a]/playlist.m3u8");

        WOWZStatusCallback statusCallback = new StatusCallback();
        //mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Handler", "run: play메소드를 바로 시작하면 오류가 발생하기 때문에 딜레이를 주고 play메소드를 실행한다.");
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
            Log.d("시청 상태: ", "onWZStatus: " + wzStatus);
        }
        @Override
        public void onWZError(WOWZStatus wzStatus) {
            Log.d("시청 에러: ", "onWZError: " + wzStatus);
        }
    }
}



