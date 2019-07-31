package com.example.retrofitexample;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.retrofitexample.Chat.ChatService;


public class BootReceiver extends BroadcastReceiver {
    // BroadcastReceiver를 상속하여 처리 해줍니다.

    //ChatService chatService;
    public static boolean isService = false; // 서비스 중인지 확인용
    private static final String TAG = "BootReceiver: ";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        // TODO Auto-generated method stub
        // 전달 받은 Broadcast의 값을 가져오기
        // androidmanifest.xml에 정의한 인텐트 필터를 받아 올 수 있습니다.
        String action = intent.getAction();
        // 전달된 값이 '부팅완료' 인 경우에만 동작 하도록 조건문을 설정 해줍니다.
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            // TODO
            // 부팅 이후 처리해야 할 코드 작성
            // Ex.서비스 호출, 특정 액티비티 호출등등

            if(!isService){
                Log.d(TAG, "onReceive: isServce: " + isService);

//                Intent i = new Intent(context, ChatService.class);
//                context.startService(i); // 서비스 시작


                // 서비스를 실행할 때
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, ChatService.class));
                } else {
                    context.startService(new Intent(context, ChatService.class));
                }

                isService = true;
            }
        }
    }
}