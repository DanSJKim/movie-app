package com.example.retrofitexample.Streaming;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.Streaming.Model.StreamingListContent;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 방송 시작하는 액티비티
 * 앱에서 방송 시작 버튼을 클릭하기 전에 Wowza Streaming Cloud 또는 Wowza Streaming Engine에서 라이브 스트림을 시작하자.
 */
public class StreamingActivity extends AppCompatActivity implements WOWZStatusCallback, View.OnClickListener {

    private static final String TAG = "StreamingActivity: ";

    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    String roomName; // 방제
    String roomHost; // 방장

    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    // 방송을 하기 위해 사용 되는 퍼미션 확인 변수
    private boolean mPermissionsGranted = true;
    // 필요한 퍼미션
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);

        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");// 방제
        roomHost = intent.getStringExtra("roomHost");// 방장
        Log.d(TAG, "onCreate: 방제: " + roomName);
        Log.d(TAG, "onCreate: 방장: " + roomHost);

        // Initialize the GoCoder SDK
        // GoCoder SDK 라이센스를 등록하고 SDK를 초기화하고 GOSK-XXXX-XXXX-XXXX-XXXX-XXXX를 등록 된 라이센스 키로 바꾼다.
        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-BE46-010C-D7F5-A882-CDEE");
        Log.d(TAG, "onCreate: goCoder: " + goCoder);

        if (goCoder == null) {
            Log.d(TAG, "onCreate: goCoder가 null이면 마지막 에러 내용을 가져와서 Toast로 표시한다.");
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Associate the WOWZCameraView defined in the U/I layout with the corresponding class member
        goCoderCameraView = (WOWZCameraView) findViewById(R.id.camera_preview);
        Log.d(TAG, "onCreate: goCoderCameraView: " + goCoderCameraView);

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WOWZAudioDevice();
        Log.d(TAG, "onCreate: goCoderAudioDevice: " + goCoderAudioDevice);

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();
        Log.d(TAG, "onCreate: goCoderBroadCaster: " + goCoderBroadcaster);

// Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1920x1080);

// Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
        // 방송을 위한 기본 설정
        goCoderBroadcastConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("app-fc5b");
        goCoderBroadcastConfig.setStreamName("fa06bb8a");
        goCoderBroadcastConfig.setUsername("client44490");
        goCoderBroadcastConfig.setPassword("b12dbc74");

// Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

// Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        // Associate the onClick() method as the callback for the broadcast button's click event
        // 클래스의 onCreate () 메소드의 맨 아래에 다음을 추가하여 onClick () 이벤트 핸들러를 액티비티의 레이아웃에 정의 된 브로드 캐스트 버튼과 연결
        // 방송 시작 버튼
        Button broadcastButton = (Button) findViewById(R.id.broadcast_button);
        broadcastButton.setOnClickListener(this);

        // 방송 종료 버튼
        Button finishButton = (Button) findViewById(R.id.broadcast_finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the broadcast that is currently running
                goCoderBroadcaster.endBroadcast(StreamingActivity.this);
                deleteStreamingRoom(roomHost);
                finish();
            }
        });
    }

//
// Enable Android's immersive, sticky full-screen mode
//
    //  Android의 몰입 형 고정 전체 화면 모드를 활성화
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged: 윈도우 포커스 변경됨");

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Log.d(TAG, "onWindowFocusChanged: rootView: " + rootView);
        if (rootView != null){
            Log.d(TAG, "onWindowFocusChanged: rootView가 null이 아니면 풀화면 모드로 변경한다.");
            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

//
// Called when an activity is brought to the foreground
//
    @Override
    protected void onResume() {
        super.onResume();

        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Log.d(TAG, "onResume: SDK 버전이 마시멜로 이상이면 Permission을 체크한다.");
            // CAMERA, RECORD_AUDIO
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            Log.d(TAG, "onResume: mPermissionsGranted: " + mPermissionsGranted);
            if (!mPermissionsGranted){
                Log.d(TAG, "onResume: CAMERA, RECORD_AUDIO 퍼미션이 승인되지 않았으면 승인을 요청한다");
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
            }

        } else
            Log.d(TAG, "onResume: SDK 버전이 마시멜로 이하면 퍼미션을 검사하지 않고 승인 처리");
            mPermissionsGranted = true;

        // Start the camera preview display
        // 앱이 전면에 나왔을 때 카메라 미리보기를 켠다.
        if (mPermissionsGranted && goCoderCameraView != null) {
            Log.d(TAG, "onResume: 퍼미션과 카메라뷰가 null이 아니면 카메라뷰를 실행한다.");
            if (goCoderCameraView.isPreviewPaused()){
                Log.d(TAG, "onResume: 카메라 프리뷰 상태가 pause였으면 resume시켜 준다.");
                goCoderCameraView.onResume();
            }
            else{
                Log.d(TAG, "onResume: 카메라 프리뷰 상태가 pause 이외 상태였으면 start시켜 준다.");
                goCoderCameraView.startPreview();
            }

        }
    }

//
// ActivityCompat.requestPermissions ()에 대한 호출에 대한 응답으로 콜백 호출
// the results of the permissions request
//
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: 방송할 때 필요한 퍼미션을 요청하기 위해 requestPermissions()메소드를 실행하면 호출되는 메소드");
        mPermissionsGranted = true;
        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for(int grantResult : grantResults) {
                    Log.d(TAG, "onRequestPermissionsResult: 퍼미션 요청 결과: " + grantResult);
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: 퍼미션 요청 결과가 없으면 mPermissionsGranted를 false로 리턴한다.");
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    }

//
// Utility method to check the status of a permissions request for an array of permission identifiers
//
    private static boolean hasPermissions(Context context, String[] permissions) {
        Log.d(TAG, "hasPermissions: CAMERA, AUDIO_RECORD 퍼미션을 모두 가지고 있는지 확인하는 메소드");
        for(String permission : permissions)
            // 승인되지 않은 퍼미션이 있으면 false를 리턴한다.
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        // 모든 퍼미션이 승인되었을 경우 true를 리턴한다.
        return true;
    }

    //
    // The callback invoked upon changes to the state of the broadcast
    //
    // WOWZStatusCallback 인터페이스에서 정의한 메소드를 StreamingActivity 클래스에 추가해서 방송 중에 상태 업데이트 및 오류를 모니터한다.
    @Override
    public void onWZStatus(final WOWZStatus goCoderStatus) {

        // A successful status transition has been reported by the GoCoder SDK
        Log.d(TAG, "onWZStatus: 방송 상태를 확인한다.");
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");
        Log.d(TAG, "onWZStatus: goCoderStatus.getState(): " + goCoderStatus.getState());

        switch (goCoderStatus.getState()) {
            case WOWZState.STARTING: // 시작 중
                statusMessage.append("Broadcast initialization");
                Log.d(TAG, "onWZStatus: 방송 상태: " + statusMessage);
                break;

            case WOWZState.READY: // 대기 중
                statusMessage.append("Ready to begin streaming");
                Log.d(TAG, "onWZStatus: 방송 상태: " + statusMessage);
                break;

            case WOWZState.RUNNING: // 실행 중
                statusMessage.append("Streaming is active");
                Log.d(TAG, "onWZStatus: 방송 상태: " + statusMessage);
                createStreamingRoom(roomHost, roomName);

                break;

            case WOWZState.STOPPING: // 정지 중
                statusMessage.append("Broadcast shutting down");
                Log.d(TAG, "onWZStatus: 방송 상태: " + statusMessage);
                break;

            case WOWZState.IDLE: // 정지 상태
                statusMessage.append("The broadcast is stopped");
                Log.d(TAG, "onWZStatus: 방송 상태: " + statusMessage);
                break;

            default:
                return;
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: 현재 방송 상태를 Toast로 띄워 준다.");
                Toast.makeText(StreamingActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    //
// The callback invoked when an error occurs during a broadcast
//
    @Override
    public void onWZError(final WOWZStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamingActivity.this,
                        "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //
// The callback invoked when the broadcast button is tapped
//
    @Override
    public void onClick(View view) {
        // return if the user hasn't granted the app the necessary permissions
        Log.d(TAG, "onClick: 퍼미션 승인 상태: " + mPermissionsGranted);
        if (!mPermissionsGranted) return;

        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();
        Log.d(TAG, "onClick: 방송을 위한 최소 구성 설정이 되어있는지 확인한다. " + configValidationError);

        if (configValidationError != null) {

            Log.d(TAG, "onClick: 방송을 위한 최소 구성 설정에 에러가 있으면 토스트로 에러 메세지를 표시한다.");
            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else if (goCoderBroadcaster.getStatus().isRunning()) {

            Log.d(TAG, "onClick: 방송을 하고있는 도중에 버튼을 누르면 방송을 중지시킨다.");
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(this);
        } else {

            Log.d(TAG, "onClick: 방송을 시작한다.");
            // Start streaming
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
        }
    }

    public void createStreamingRoom(String userEmail, String roomName){
        Api api = ApiClient.getClient().create(Api.class);
        Call<StreamingListContent> call = api.createStreamingRoom(userEmail, roomName);
        Log.d(TAG, "createStreamingRoom: userEmail: " + userEmail);
        Log.d(TAG, "createStreamingRoom: roomName: " + roomName);
        Log.d(TAG, "createStreamingRoom: 스트리밍 방을 생성한다.");

        call.enqueue(new Callback<StreamingListContent>() {
            @Override
            public void onResponse(Call<StreamingListContent> call, final Response<StreamingListContent> response) {
                Log.d(TAG, "onResponse: 방 만들기 결과: " + response.body().getRoomHost());


            }
            @Override
            public void onFailure(Call<StreamingListContent> call, Throwable t) {

                Log.d("방 생성 에러", t.getMessage());
            }
        });
    }

    public void deleteStreamingRoom(String userEmail){
        Api api = ApiClient.getClient().create(Api.class);
        Call<StreamingListContent> call = api.deleteStreamingRoom(userEmail);
        Log.d(TAG, "createStreamingRoom: userEmail: " + userEmail);
        Log.d(TAG, "createStreamingRoom: 스트리밍 방을 삭제한다.");

        call.enqueue(new Callback<StreamingListContent>() {
            @Override
            public void onResponse(Call<StreamingListContent> call, final Response<StreamingListContent> response) {
                Log.d(TAG, "onResponse: 방 지우기 결과: " + response.body().getRoomHost());


            }
            @Override
            public void onFailure(Call<StreamingListContent> call, Throwable t) {

                Log.d("방 삭제 에러", t.getMessage());
            }
        });
    }
}
