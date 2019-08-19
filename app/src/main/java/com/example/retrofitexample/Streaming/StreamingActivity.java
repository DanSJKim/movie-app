package com.example.retrofitexample.Streaming;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.retrofitexample.Chat.ChatService;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.Streaming.Model.StreamingListContent;
import com.example.retrofitexample.Streaming.Model.StreamingMessage;
import com.example.retrofitexample.Streaming.Model.StreamingMessageAdapter;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.android.opengl.WOWZGLES;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.render.WOWZRenderAPI;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BootReceiver.isService;
import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;
import static com.example.retrofitexample.Chat.ChatService.currentRoomNo;
import static com.example.retrofitexample.Chat.ChatService.dos;
import static com.example.retrofitexample.Chat.ChatService.socket;
import static com.example.retrofitexample.Streaming.StreamingListActivity.roomcount;

/**
 * 방송 시작하는 액티비티
 * 앱에서 방송 시작 버튼을 클릭하기 전에 Wowza Streaming Cloud 또는 Wowza Streaming Engine에서 라이브 스트림을 시작하자.
 */
public class StreamingActivity extends AppCompatActivity implements WOWZStatusCallback, View.OnClickListener, ChatService.ServiceCallbacks, WOWZRenderAPI.VideoFrameListener {

    private static final String TAG = "StreamingActivity: ";

    // The top-level GoCoder API interface
    // 와우자 gocoder(live방송 지원)
    private WowzaGoCoder goCoder;

    // The GoCoder SDK camera view
    // 와우자 카메라뷰
    private WOWZCameraView goCoderCameraView;

    // The GoCoder SDK audio device
    // 와우자 오디오
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    // 와우자 방송
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    String roomName; // 방제

    private ChatService myService; // 서비스

    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    // 방송을 하기 위해 사용 되는 퍼미션 확인 변수
    private boolean mPermissionsGranted = true;
    // 필요한 퍼미션
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    //채팅
    EditText etMessage; // 채팅 메세지
    Button btnSend; // 채팅 전송
    SendThread send; // 메세지 전송 스레드
    String profileImage; //SharedPreference로 부터 가져온 내 이미지

    // 리사이클러뷰 변수
    private static RecyclerView mRecyclerView;
    private static ArrayList<StreamingMessage> mArrayList;
    private static StreamingMessageAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    int roomNo; // 현재 방 번호

    int broadcastFinish; // 방송 나가기 버튼을 눌러서 액티비티를 종료하기 전에 나가기 버튼을 눌러서 종료하는것인지 확인하기 위한 변수

    Button broadcastButton; // 방송 시작 버튼

    String app;
    String streamname;
    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);

        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");// 방제

        Log.d(TAG, "onCreate: 방제: " + roomName);

        broadcastFinish = 0;

        profileImage = SharedPref.getInstance(this).StoredProfileImage(); // 내 프로필 이미지

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

        Log.d(TAG, "onCreate: roomcount: " + roomcount);
// Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
        // 방송을 위한 기본 설정
        //if(roomcount == 0){
            Log.d(TAG, "onCreate: 0");
            goCoderBroadcastConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-fc5b");
            goCoderBroadcastConfig.setStreamName("fa06bb8a");
            goCoderBroadcastConfig.setUsername("client44490");
            goCoderBroadcastConfig.setPassword("b12dbc74");
//        }else if(roomcount == 1){
//            Log.d(TAG, "onCreate: 1");
//            goCoderBroadcastConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
//            goCoderBroadcastConfig.setPortNumber(1935);
//            goCoderBroadcastConfig.setApplicationName("app-fee6");
//            goCoderBroadcastConfig.setStreamName("89fcd457");
//            goCoderBroadcastConfig.setUsername("client44490");
//            goCoderBroadcastConfig.setPassword("ca5ce168");
//        }


// Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

// Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        // Associate the onClick() method as the callback for the broadcast button's click event
        // 클래스의 onCreate () 메소드의 맨 아래에 다음을 추가하여 onClick () 이벤트 핸들러를 액티비티의 레이아웃에 정의 된 브로드 캐스트 버튼과 연결
        // 방송 시작 버튼
        broadcastButton = (Button) findViewById(R.id.broadcast_button);
        broadcastButton.setOnClickListener(this);

        // 방송 종료 버튼
//        ImageView finishButton = (ImageView) findViewById(R.id.broadcast_finish_button);
//        finishButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: 방송 종료버튼 클릭");
//                Log.d(TAG, "onClick: 방송 상태: " + goCoderBroadcaster.getStatus().isRunning());
//                Log.d(TAG, "onClick: 방송 중지 1");
//                // Stop the broadcast that is currently running
//
//                goCoderBroadcaster.endBroadcast(StreamingActivity.this);
//                // 데이터베이스에서 방 삭제
//                deleteStreamingRoom(loggedUseremail);
//            }
//        });

        // 카메라 전환 버튼
        ImageView ivSwitchCamera = (ImageView) findViewById(R.id.ivSwitchCamera);
        ivSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 카메라 전후면 전환 버튼 클릭");

                onSwitchCamera(); // 카메라 전환하기 위한 메소드
            }
        });

        //채팅
        etMessage = (EditText) findViewById(R.id.etStreamingChatContent); // 채팅 내용
        btnSend = (Button) findViewById(R.id.btnStreamingChatSend); // 채팅 전송
        mArrayList = new ArrayList<>();

        //리사이클러뷰 초기화
        mRecyclerView = (RecyclerView) findViewById(R.id.rvStreamingChat); // 리사이클러뷰 연결
        mLinearLayoutManager = new LinearLayoutManager(this); // 레이아웃 매니저
        mRecyclerView.setLayoutManager(mLinearLayoutManager); // 리사이클러뷰에 레이아웃 매니저 적용

        mAdapter = new StreamingMessageAdapter(mArrayList); // 어댑터
        mRecyclerView.setAdapter(mAdapter); // 리사이클러뷰에 어댑터 적용

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString(); // 채팅 메세지

                // 아무것도 입력하지 않았을 때
                if (message == null || TextUtils.isEmpty(message) || message.equals("") || message.trim().matches("")) {
                    Log.d(TAG, "onClick: 아무것도 입력하지 않음.");
                    Toast.makeText(StreamingActivity.this, "메세지를 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {
                    Log.d(TAG, "onClick: 스티리밍 채팅 메세지 1: 채팅 메세지를 전송한다.");
                    send = new SendThread(socket, 8, roomNo, loggedUseremail, message); // ChatService에서 생성한 클라이언트 소켓 변수
                    send.start();
                }
            }
        });

    }

    /**
     * Click handler for the switch camera button
     */
    public void onSwitchCamera() {
        if (goCoderCameraView == null) return;
        Log.d(TAG, "onSwitchCamera: 카메라 변환 버튼을 누름.");

        // Set the new surface extension prior to camera switch such that
        // setting will take place with the new one.  So if it is currently the front
        // camera, then switch to default setting (not mirrored).  Otherwise show mirrored.
//        if(mWZCameraView.getCamera().getDirection() == WOWZCamera.DIRECTION_FRONT) {
//            mWZCameraView.setSurfaceExtension(mWZCameraView.EXTENSION_DEFAULT);
//        }
//        else{
//            mWZCameraView.setSurfaceExtension(mWZCameraView.EXTENSION_MIRROR);
//        }

        WOWZCamera newCamera = goCoderCameraView.switchCamera();
        if (newCamera != null) {
            if (newCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
                newCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);
        }
    }

//
// Enable Android's immersive, sticky full-screen mode
//
    //  Android의 몰입 형 고정 전체 화면 모드를 활성화
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged: 방송 화면 실행 5: 윈도우 포커스 변경 시에 실행된다.");

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

            // EditText 활성화 시 모양이 변형되는 것을 방지하기 위해 아래 코드를 추가해 준다.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

//
// Called when an activity is brought to the foreground
//
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: 방송 화면 실행 1: 액티비티가 화면에 보일 시에 실행된다.");

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
        // 앱이 전면에 나왔을 때 카메라를 켠다.
        if (mPermissionsGranted && goCoderCameraView != null) {
            Log.d(TAG, "onResume: 방송 화면 실행 3: 퍼미션과 카메라뷰가 null이 아니면 카메라뷰를 실행한다.");
            if (goCoderCameraView.isPreviewPaused()){
                Log.d(TAG, "onResume: 방송 화면 실행 4: 카메라 프리뷰 상태가 pause였으면 resume시켜 준다.");
                goCoderCameraView.onResume();
            }
            else{
                Log.d(TAG, "onResume: 방송 화면 실행 4: 카메라 프리뷰 상태가 pause 이외 상태였으면 start시켜 준다.");
                goCoderCameraView.startPreview();
            }
        }

        Log.d(TAG, "onResume: isService: " + isService);
        // bind to Service
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    // 클래스 객체 생성 및 구현
    // Service에서 bind한 것에 대한 결과로, 연결이 되거나, 끊어지기도 하는데 그 상태에 대한 Callback들이다.
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected: onResume에서 bindService메소드가 실행될 때 호출된다.");

            // 서비스와 연결되었을 때 호출되는 메서드
            // cast the IBinder and get MyService instance
            ChatService.LocalBinder binder = (ChatService.LocalBinder) service;
            // getService 메소드를 사용해서 service객체를 사용한다.
            myService = binder.getService();
            myService.setCallbacks(StreamingActivity.this); // register

            isService = true; // 서비스 실행 여부
        }

        // 서비스와 연결이 끊기거나 종료되었을 때
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: onPause에서 unbindService메소드가 실행될 때 호출된다.");

            isService = false; // 서비스 실행 여부
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");

        currentRoomNo = -1; // 화면이 보이지 않을 떄는 방 번호를 -1로 지정해 준다

        // Unbind from service
        if (isService) {
            Log.d(TAG, "onPause(): unbind 서비스 연결 해제");
            currentRoomNo = -1; // 방 번호를 바꿔 준다.
            myService.setCallbacks(null); // unregister
            unbindService(conn); // 서비스 unbind
            //isService = false;
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        //deleteStreamingRoom(loggedUseremail); // 액티비티 종료 시 데이터베이스에서 방 삭제
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
    //CAMERA, AUDIO_RECORD 퍼미션을 모두 가지고 있는지 확인하는 메소드
    private static boolean hasPermissions(Context context, String[] permissions) {
        Log.d(TAG, "hasPermissions: 방송 화면 실행 2: onResume의 Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 조건문에서 실행되는 메소드");
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
        Log.d(TAG, "onWZStatus: 방송 시작 or 중지 클릭 시 이 메소드가 실행 된다.");

        // A successful status transition has been reported by the GoCoder SDK
        Log.d(TAG, "onWZStatus: 방송 상태를 확인한다.");

        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");
        Log.d(TAG, "onWZStatus: goCoderStatus.getState() 방송 상태를 case문으로: " + goCoderStatus.getState());

        switch (goCoderStatus.getState()) {
            // 시작 중
            case WOWZState.STARTING:
                Log.d(TAG, "onWZStatus: 방송 시작 2: STARTING case 실행");
                statusMessage.append("Broadcast initialization");
                Log.d(TAG, "onWZStatus: statusMessage: " + statusMessage);
                break;

            case WOWZState.READY: // 대기 중
                Log.d(TAG, "onWZStatus: 방송 시작 3: READY case 실행");
                statusMessage.append("Ready to begin streaming");
                Log.d(TAG, "onWZStatus: statusMessage: " + statusMessage);
                break;

            case WOWZState.RUNNING: // 실행 중
                Log.d(TAG, "onWZStatus: 방송 시작 4: RUNNING case 실행");
                statusMessage.append("Streaming is active");
                Log.d(TAG, "onWZStatus: statusMessage: " + statusMessage);

                // 서비스로 부터 받은 메세지 내용
                String msg = myService.getMsg();
                Log.d(TAG, "ChatdoSomething: msg: " + msg);

                // 스트리밍이 성공적으로 시작 되면 데이터베이스에 방 정보를 저장한다.
                createStreamingRoom(loggedUseremail, roomName);

                break;

            case WOWZState.STOPPING: // 정지 중
                Log.d(TAG, "onWZStatus: 방송 중지 2: STOPPING case 실행");
                statusMessage.append("Broadcast shutting down");
                Log.d(TAG, "onWZStatus: statusMessage: " + statusMessage);
                break;

            case WOWZState.IDLE: // 정지 상태
                Log.d(TAG, "onWZStatus: 방송 중지 3: IDLE case 실행");
                statusMessage.append("The broadcast is stopped");
                Log.d(TAG, "onWZStatus: statusMessage: " + statusMessage);

                break;

            default:
                return;
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() { // case문이 각각 실행될 때마다 해당 토스트를 띄워 준다.
                Log.d(TAG, "run: Toast: " + statusMessage);
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
        Log.d(TAG, "onClick: 방송 시작 버튼 클릭 시 실행");
        
        // return if the user hasn't granted the app the necessary permissions
        Log.d(TAG, "onClick: 퍼미션 승인 상태: " + mPermissionsGranted);
        if (!mPermissionsGranted) return;

        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();
        Log.d(TAG, "onClick: 1. 방송을 위한 최소 구성 설정이 되어있는지 확인한다. " + configValidationError);
        Log.d(TAG, "onClick: goCoderCameraView.getPreviewStatus()1: " + goCoderCameraView.getPreviewStatus());

        if (configValidationError != null) {

            Log.d(TAG, "onClick: 1. 방송을 위한 최소 구성 설정에 에러가 있으면 토스트로 에러 메세지를 표시한다.");
            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();

            // 방송이 실행 중이면
        } else if (goCoderBroadcaster.getStatus().isRunning()) {
            Log.d(TAG, "onClick: 방송이 실행중일 때");
            Log.d(TAG, "onClick: 방송 종료 버튼을 누르면 방송이 정지된다.");

            broadcastButton.setText("방송 시작"); // 방송 시작으로 텍스트 변경

//            if(goCoderCameraView.getPreviewStatus().isIdle()){ //일시 정지 된 상태이면 시작
//
//                Log.d(TAG, "onClick: 일시 정지된 상태에서 다시 시작");
//                Log.d(TAG, "onClick: goCoderCameraView.isPreviewPaused()2: " + goCoderCameraView.getPreviewStatus());
//                goCoderCameraView.startPreview();
//
//            }else{
//                Log.d(TAG, "onClick: 화면 정지");
//                Log.d(TAG, "onClick: goCoderCameraView.isPreviewPaused()2: " + goCoderCameraView.getPreviewStatus());
//                broadcastButton.setText("방송 정지"); // 방송 정지로 텍스트 변경
//                goCoderCameraView.stop();
//            }

            //Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(this);

            // 방송 종료 시에 시스템 메세지를 띄워주기 위해 핸들러를 호출한다.
            // 핸들러에게 전달할 메세지 객체
            Message hdmg = msgHandler.obtainMessage();

            // 핸들러에게 전달할 메세지의 식별자
            hdmg.what = 4444;

            // 핸들러에게 메세지 전달 ( 화면 처리 )
            msgHandler.sendMessage(hdmg);

            //데이터베이스에서 방 삭제
            deleteStreamingRoom(loggedUseremail);

        } else {

            Log.d(TAG, "onClick: 방송 시작 1: 방송 시작 버튼 클릭 시 방송을 시작한다.");
            // Start streaming
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);

            broadcastButton.setText("방송 종료"); // 방송 중지로 텍스트 변경
        }
    }

    public void createStreamingRoom(String userEmail, String roomName){
        Api api = ApiClient.getClient().create(Api.class);
        Call<StreamingListContent> call = api.createStreamingRoom(userEmail, roomName);
        Log.d(TAG, "onWZStatus: 방송 시작 5: RUNNING case 실행 할 때 호출 한다.");
        Log.d(TAG, "createStreamingRoom: userEmail: " + userEmail);
        Log.d(TAG, "createStreamingRoom: roomName: " + roomName);
        Log.d(TAG, "createStreamingRoom: 스트리밍 방을 생성한다.");


        call.enqueue(new Callback<StreamingListContent>() {
            @Override
            public void onResponse(Call<StreamingListContent> call, final Response<StreamingListContent> response) {
                Log.d(TAG, "onResponse: 방 만들기 결과 방 번호: " + response.body().getRoomNo());

                roomNo = response.body().getRoomNo();
                currentRoomNo = roomNo; // 현재 내가 접속한 방 번호를 지정해 준다.

                // 처음 방송 실행 시에 시스템 메세지를 띄워주기 위해 핸들러를 호출한다.
                // 핸들러에게 전달할 메세지 객체
                Message hdmg = msgHandler.obtainMessage();

                // 핸들러에게 전달할 메세지의 식별자
                hdmg.what = 3333;

                // 핸들러에게 메세지 전달 ( 화면 처리 )
                msgHandler.sendMessage(hdmg);

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
        Log.d(TAG, "createStreamingRoom: 방송 중지 3-2: 스트리밍 방을 삭제한다.");

        call.enqueue(new Callback<StreamingListContent>() {
            @Override
            public void onResponse(Call<StreamingListContent> call, final Response<StreamingListContent> response) {
                Log.d(TAG, "onResponse: 방 지우기 결과: " + response.body().getRoomHost());

                //finish();
            }
            @Override
            public void onFailure(Call<StreamingListContent> call, Throwable t) {

                Log.d("방 삭제 에러", t.getMessage());
            }
        });
    }


    // 내부 클래스  ( 메세지 전송용 )
    class SendThread extends Thread{
        Socket socket;
        int mode;
        int roomNo;
        String myEmail;
        String yourEmail;
        String message;
        DataOutputStream output;

        public SendThread(Socket socket, int mode, int roomNo, String myEmail, String message){
            Log.d(TAG, "SendThread: 메세지 전송 2: send 클릭 메소드를 실행 하면 스레드가 실행된다.");

            Log.d(TAG, "SendThread: mode: " + mode);
            Log.d(TAG, "SendThread: message: " + message);
            this.mode = mode;
            this.socket = socket;
            this.roomNo = roomNo;
            this.myEmail = myEmail;
            this.message = message;
            this.output = dos; // ChatService의 아웃풋스트림 변수를 가져온다.
        }

        // 서버로 메세지 전송
        public  void run(){
            try {
                if (message != null){
                    Log.d(TAG, "SendThread run: 메세지 전송 3: 스레드를 실행해서 서버로 메세지를 전송 한다.");

                    JSONObject jo = new JSONObject();

                    Log.d(TAG, "SendThread run: mode: " + mode);

                    jo.put("mode", mode);
                    jo.put("roomNo", roomNo);
                    jo.put("myEmail", myEmail);
                    jo.put("myProfile", profileImage);
                    jo.put("message", message);

                    dos.writeUTF(jo.toString());

                    /* 핸들러 */
                    // 핸들러에게 전달할 메세지 객체
                    Message hdmg = msgHandler.obtainMessage();

                    // 핸들러에게 전달할 메세지의 식별자
                    hdmg.what = 2222;

                    // 메세지의 본문
                    hdmg.obj = jo.toString();

                    // 핸들러에게 메세지 전달 ( 화면 처리 )
                    msgHandler.sendMessage(hdmg);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    // 메세지 전송하고 서버로 부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
    @SuppressLint("HandlerLeak")
    Handler msgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1111){
                Log.d(TAG, "handleMessage: 스트리밍 메세지 수신 2: chatDoSomething 메소드에서 호출");
                Log.d(TAG, "handleMessage: message: " + msg.obj.toString());

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(msg.obj.toString());

                    String mode = jsonObject.getString("mode");
                    int modeToInt = Integer.parseInt(mode);

                    Log.d(TAG, "handleMessage: modeToInt2: " + modeToInt);

                    String roomNo = jsonObject.getString("roomNo");
                    String myEmail = jsonObject.getString("myEmail");
                    String myProfile = jsonObject.getString("myProfile");
                    String message = jsonObject.getString("message");
                    int roomNoToInt = Integer.parseInt(roomNo);

                    // 메세지 내용
                    StreamingMessage messageContent = new StreamingMessage(modeToInt, myProfile, myEmail, message);
                    // 리스트에 수신 한 메세지 아이템 추가
                    mArrayList.add(messageContent);
                    // 메세지 변경 알림
                    mAdapter.notifyDataSetChanged();

                    //맨 아래 아이템으로 스크롤
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

                    // 키보드 내려주기
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(btnChatSend.getWindowToken(),0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else if(msg.what == 2222){
                Log.d(TAG, "handleMessage: 메세지 전송 4: SendThread 실행 후 핸들러에서 나의 채팅 UI를 업데이트 한다.");
                Log.d(TAG, "handleMessage: jo.tostring(): " + msg.obj);

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;

                try {

                    jsonObject = new JSONObject(msg.obj.toString()); //

                    int mode = jsonObject.getInt("mode");
                    String roomNo = jsonObject.getString("roomNo"); // 방 번호
                    String myEmail = jsonObject.getString("myEmail"); // 내 이메일
                    String myProfile = jsonObject.getString("myProfile"); // 내 프로필
                    String message = jsonObject.getString("message"); // 내 채팅 메세지

                    int roomNoToInt = Integer.parseInt(roomNo);

                    Log.d(TAG, "handleMessage: roomNo: " + roomNo);
                    Log.d(TAG, "handleMessage: mode: " + mode);
                    Log.d(TAG, "handleMessage: roomNo: " + roomNo);
                    Log.d(TAG, "handleMessage: message: " + message);

                    //내 채팅내용 업데이트
                    // 채팅 내용 생성
                    StreamingMessage messageContent = new StreamingMessage(mode, myProfile, myEmail, message);

                    // ArrayList에 채팅 내용 추가
                    mArrayList.add(messageContent);

                    // 리사이클러뷰 데이터 변경을 알린다.
                    mAdapter.notifyDataSetChanged();

                    // 에디트 텍스트 비워주기
                    etMessage.setText(null);

                    //맨 아래 아이템으로 스크롤
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

                    // 키보드 내려주기
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(btnChatSend.getWindowToken(),0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else if(msg.what == 3333){
                Log.d(TAG, "handleMessage: 처음 방송 실행 시 추가 할 시스템 메세지");

                //내 채팅내용 업데이트
                // 채팅 내용 생성
                StreamingMessage messageContent = new StreamingMessage(9999, "Server", "Server", "따뜻한 소통과 배려로 더욱 즐거운 스트리밍 방송을 만들어주세요! 특정인에 대한 비방과 비하, 인종/지역/성/장애인 차별, 청소년 보호법 위반, 정치 선동성 채팅은 제재의 대상이 됩니다.");

                // ArrayList에 채팅 내용 추가
                mArrayList.add(messageContent);

                // 리사이클러뷰 데이터 변경을 알린다.
                mAdapter.notifyDataSetChanged();

                // 에디트 텍스트 비워주기
                etMessage.setText(null);

                //맨 아래 아이템으로 스크롤
                mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

            }else if(msg.what == 4444){
                Log.d(TAG, "handleMessage: 방송 종료 시 추가 할 시스템 메세지");

                //내 채팅내용 업데이트
                // 채팅 내용 생성
                StreamingMessage messageContent = new StreamingMessage(9999, "Server", "Server", "방송이 종료되었습니다.");

                // ArrayList에 채팅 내용 추가
                mArrayList.add(messageContent);

                // 리사이클러뷰 데이터 변경을 알린다.
                mAdapter.notifyDataSetChanged();

                // 에디트 텍스트 비워주기
                etMessage.setText(null);

                //맨 아래 아이템으로 스크롤
                mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

            }
        }
    };

    @Override
    public void ChatdoSomething() {
        Log.d(TAG, "ChatdoSomething: 스트리밍 채팅 메세지 수신 1: 스트리밍 방 메세지 수신 시 호출");

        // 서비스로 부터 받은 메세지 내용
        String msg = myService.getMsg();
        Log.d(TAG, "ChatdoSomething: msg: " + msg);

        // 핸들러에게 전달할 메세지 객체
        Message hdmg = msgHandler.obtainMessage();

        // 핸들러에게 전달할 메세지의 식별자
        hdmg.what = 1111;

        // 메세지의 본문
        hdmg.obj = msg;

        // 핸들러에게 메세지 전달 ( 화면 처리 )
        msgHandler.sendMessage(hdmg);
    }


    @Override
    public boolean isWZVideoFrameListenerActive() {
        return false;
    }

    @Override
    public void onWZVideoFrameListenerInit(WOWZGLES.EglEnv eglEnv) {

    }

    @Override
    public void onWZVideoFrameListenerFrameAvailable(WOWZGLES.EglEnv eglEnv, WOWZSize wowzSize, int i, long l) {

        Log.d(TAG, "onWZVideoFrameListenerFrameAvailable: i: " + i);

    }

    @Override
    public void onWZVideoFrameListenerRelease(WOWZGLES.EglEnv eglEnv) {
        Log.d(TAG, "onWZVideoFrameListenerRelease: eglEnv: " + eglEnv);

    }

    public void registerFrameListener(WOWZRenderAPI.VideoFrameListener frameListener){

        Log.d(TAG, "registerFrameListener: frameListener: " + frameListener);

    }
}
