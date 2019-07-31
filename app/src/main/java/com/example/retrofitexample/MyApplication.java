package com.example.retrofitexample;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;

/**
 * SQLite 데이터를 stetho에서 확인하기 위해 만든 Application 클래스. 모든 액티비티 시작 전에 가장 먼저 실행 된다.
 */

public class MyApplication extends Application {
    public static final String TAG = "MyApplication : ";

    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate(); Stetho.initializeWithDefaults(this);
    }
}
