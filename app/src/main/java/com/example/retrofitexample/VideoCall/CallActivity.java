/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.retrofitexample.VideoCall;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.example.retrofitexample.Chat.ChatActivity;
import com.example.retrofitexample.Chat.ChatService;
import com.example.retrofitexample.Chat.Model.Room;
import com.example.retrofitexample.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.RuntimeException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.VideoCall.AppRTCAudioManager.AudioDevice;
import com.example.retrofitexample.VideoCall.AppRTCAudioManager.AudioManagerEvents;
import com.example.retrofitexample.VideoCall.AppRTCClient.RoomConnectionParameters;
import com.example.retrofitexample.VideoCall.AppRTCClient.SignalingParameters;
import com.example.retrofitexample.VideoCall.PeerConnectionClient.DataChannelParameters;
import com.example.retrofitexample.VideoCall.PeerConnectionClient.PeerConnectionParameters;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BootReceiver.isService;
import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;
import static com.example.retrofitexample.Chat.ChatService.currentRoomNo;
import static com.example.retrofitexample.Chat.ChatService.dos;
import static com.example.retrofitexample.Chat.ChatService.socket;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallActivity extends Activity implements AppRTCClient.SignalingEvents,
                                                      PeerConnectionClient.PeerConnectionEvents,
                                                      CallFragment.OnCallEvents,
                                                      ChatService.ServiceCallbacks{
  private static final String TAG = "CallActivity";

  // intent 받을 변수명
  public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
  public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
  public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
  public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
  public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
  public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
  public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
  public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
  public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
  public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
      "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
  public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
  public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
  public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
  public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
  public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
  public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
  public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
  public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
      "org.appspot.apprtc.NOAUDIOPROCESSING";
  public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
  public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
      "org.appspot.apprtc.SAVE_INPUT_AUDIO_TO_FILE";
  public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
  public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
  public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
  public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
  public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
      "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
  public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
  public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
  public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
  public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
      "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
      "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
      "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
  public static final String EXTRA_USE_VALUES_FROM_INTENT =
      "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
  public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
  public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
  public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
  public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
  public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
  public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
  public static final String EXTRA_ID = "org.appspot.apprtc.ID";
  public static final String EXTRA_ENABLE_RTCEVENTLOG = "org.appspot.apprtc.ENABLE_RTCEVENTLOG";

  private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

  private ChatService myService; // 서비스

  SendThread send; // 채팅 종료 메세지 보내주기 위한 스레드

  // List of mandatory application permissions.
  // 반드시 포함되어야 하는 퍼미션
  // 오디오 세팅 수정, 오디오 녹음, 인터넷 접속
  private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
      "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

  // Peer connection statistics callback period in ms.
  // 영상통화 콜백 딜레이 시간
  private static final int STAT_CALLBACK_PERIOD = 1000;

  private static class ProxyVideoSink implements VideoSink {
    private VideoSink target;

    @Override
    synchronized public void onFrame(VideoFrame frame) {
      if (target == null) {
        Logging.d(TAG, "Dropping frame in proxy because target is null.");
        return;
      }

      target.onFrame(frame);
    }

    synchronized public void setTarget(VideoSink target) {
      this.target = target;
    }
  }

  private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
  private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
  @Nullable private PeerConnectionClient peerConnectionClient;
  @Nullable
  private AppRTCClient appRtcClient;
  @Nullable
  private SignalingParameters signalingParameters;
  @Nullable private AppRTCAudioManager audioManager;
  @Nullable
  private SurfaceViewRenderer pipRenderer;
  @Nullable
  private SurfaceViewRenderer fullscreenRenderer;
  @Nullable
  private VideoFileRenderer videoFileRenderer;
  private final List<VideoSink> remoteSinks = new ArrayList<>();
  private Toast logToast;
  private boolean commandLineRun;
  private boolean activityRunning;
  private RoomConnectionParameters roomConnectionParameters;
  @Nullable
  private PeerConnectionParameters peerConnectionParameters;
  private boolean connected;
  private boolean isError;
  private boolean callControlFragmentVisible = true;
  private long callStartedTimeMs;
  private boolean micEnabled = true;
  private boolean screencaptureEnabled;
  private static Intent mediaProjectionPermissionResultData;
  private static int mediaProjectionPermissionResultCode;
  // True if local view is in the fullscreen renderer.
  private boolean isSwappedFeeds;

  // Controls
  private CallFragment callFragment;

  int roomNo; // 방 번호
  String senderEmail; // 영상 통화 보낸 사람 이메일
  String receiverEmail; // 영상 통화 받는 사람 이메일

  ReceiveCallActivity RCA;
  @Override
  // TODO(bugs.webrtc.org/8580): LayoutParams.FLAG_TURN_SCREEN_ON and
  // LayoutParams.FLAG_SHOW_WHEN_LOCKED are deprecated.
  @SuppressWarnings("deprecation")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
    Log.d(TAG, "onCreate: video call started");

    // 전화 수신 대기 액티비티
    RCA = (ReceiveCallActivity) ReceiveCallActivity._Receive_Call_Activity;


    // 영상 통화 액티비티를 입장해 있는 방의 번호와 일치시킨다.
    Intent getroomnointent = getIntent();
    roomNo = getroomnointent.getIntExtra("roomNo", -1); // 방 번호
    senderEmail = getroomnointent.getStringExtra("senderEmail"); // 영상 통화 발신자
    receiverEmail = getroomnointent.getStringExtra("yourEmail"); // 영상 통화 수신자


    currentRoomNo = -2; // 통화용 방번호
    Log.d(TAG, "onCreate: received room number: " + roomNo);
    Log.d(TAG, "onCreate: senderEmail: " + senderEmail);
    Log.d(TAG, "onCreate: receiverEmail: " + receiverEmail);

    // Set window styles for fullscreen-window size. Needs to be done before
    // adding content.
    // requestWindowFeature -> 확장 윈도우 기능을 활성화 하는 메소드
    requestWindowFeature(Window.FEATURE_NO_TITLE);// 프레임의 타이틀 제거

    //  이 창이 표시되어있는 동안 모든 화면 장식을 숨김,
    //  화면을 켠 상태로 유지,
    //  폰이 락 상태이면 보여줌,
    //  화면을 ON 시킨다.
    getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON
        | LayoutParams.FLAG_SHOW_WHEN_LOCKED | LayoutParams.FLAG_TURN_SCREEN_ON);

    getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
    // xml 뷰 연결
    setContentView(R.layout.activity_call);

    connected = false;
    signalingParameters = null;

    // Create UI controls.
    // 카메라 제어 관련 버튼
    pipRenderer = findViewById(R.id.pip_video_view);
    // 영상 통화 풀 스크린
    fullscreenRenderer = findViewById(R.id.fullscreen_video_view);
    // 영상 통화 제어 버튼 프래그먼트
    callFragment = new CallFragment();

    // Show/hide call control fragment on view click.
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        // 클릭 시 제어 버튼이 사라진다.
        toggleCallControlFragmentVisibility();
      }
    };

    // Swap feeds on pip view click.
    pipRenderer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setSwappedFeeds(!isSwappedFeeds);
      }
    });

    fullscreenRenderer.setOnClickListener(listener);
    remoteSinks.add(remoteProxyRenderer);

    final Intent intent = getIntent();
    final EglBase eglBase = EglBase.create();

    // Create video renderers.
    pipRenderer.init(eglBase.getEglBaseContext(), null);
    pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
    String saveRemoteVideoToFile = intent.getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);

    // When saveRemoteVideoToFile is set we save the video from the remote to a file.
    if (saveRemoteVideoToFile != null) {
      int videoOutWidth = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
      int videoOutHeight = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
      try {
        videoFileRenderer = new VideoFileRenderer(
            saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.getEglBaseContext());
        remoteSinks.add(videoFileRenderer);
      } catch (IOException e) {
        throw new RuntimeException(
            "Failed to open video file for output: " + saveRemoteVideoToFile, e);
      }
    }
    fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
    fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);

    pipRenderer.setZOrderMediaOverlay(true);
    pipRenderer.setEnableHardwareScaler(true /* enabled */);
    fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);
    // Start with local feed in fullscreen and swap it to the pip when the call is connected.
    setSwappedFeeds(true /* isSwappedFeeds */);

    // Check for mandatory permissions.
    for (String permission : MANDATORY_PERMISSIONS) {
      if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        logAndToast("Permission " + permission + " is not granted");
        setResult(RESULT_CANCELED);
        finish();
        return;
      }
    }

    Uri roomUri = intent.getData();
    if (roomUri == null) {
      logAndToast(getString(R.string.missing_url));
      Log.e(TAG, "Didn't get any URL in intent!");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    // Get Intent parameters.
    String roomId = intent.getStringExtra(EXTRA_ROOMID);
    Log.d(TAG, "Room ID: " + roomId);
    if (roomId == null || roomId.length() == 0) {
      logAndToast(getString(R.string.missing_url));
      Log.e(TAG, "Incorrect room ID in intent!");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }


    boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
    boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

    // 비디오의 해상도
    int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
    int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

    screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
    // If capturing format is not specified for screencapture, use screen resolution.
    if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
      DisplayMetrics displayMetrics = getDisplayMetrics();
      videoWidth = displayMetrics.widthPixels;
      videoHeight = displayMetrics.heightPixels;
    }
    DataChannelParameters dataChannelParameters = null;
    if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
      dataChannelParameters = new DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
          intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
          intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
          intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
    }
    peerConnectionParameters =
        new PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
            tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
            intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
            intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
            intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
            intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
            intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
            intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
            intent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
            intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
            intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
            intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
            intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
            intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
            intent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);
    commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
    int runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

    Log.d(TAG, "VIDEO_FILE: '" + intent.getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA) + "'");

    // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
    // standard WebSocketRTCClient.
    if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
      appRtcClient = new WebSocketRTCClient(this);
    } else {
      Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
      appRtcClient = new DirectRTCClient(this);
    }
    // Create connection parameters.
    String urlParameters = intent.getStringExtra(EXTRA_URLPARAMETERS);
    roomConnectionParameters =
        new RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

    // Create CPU monitor
//    if (CpuMonitor.isSupported()) {
//      cpuMonitor = new CpuMonitor(this);
//      hudFragment.setCpuMonitor(cpuMonitor);
//    }

    // Send intent arguments to fragments.
    callFragment.setArguments(intent.getExtras());
    //hudFragment.setArguments(intent.getExtras());
    // Activate call and HUD fragments and start the call.
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    ft.add(R.id.call_fragment_container, callFragment);
    //ft.add(R.id.hud_fragment_container, hudFragment);
    ft.commit();

    // For command line execution run connection for <runTimeMs> and exit.
    if (commandLineRun && runTimeMs > 0) {
      (new Handler()).postDelayed(new Runnable() {
        @Override
        public void run() {
          disconnect();
        }
      }, runTimeMs);
    }

    // Create peer connection client.
    peerConnectionClient = new PeerConnectionClient(
        getApplicationContext(), eglBase, peerConnectionParameters, CallActivity.this);
    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
    if (loopback) {
      options.networkIgnoreMask = 0;
    }
    peerConnectionClient.createPeerConnectionFactory(options);

    if (screencaptureEnabled) {
      startScreenCapture();
    } else {
      startCall();
    }
  }

  @TargetApi(17)
  private DisplayMetrics getDisplayMetrics() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager windowManager =
        (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
    windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
    return displayMetrics;
  }

  @TargetApi(19)
  private static int getSystemUiVisibility() {
    int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
    return flags;
  }

  @TargetApi(21)
  private void startScreenCapture() {
    MediaProjectionManager mediaProjectionManager =
        (MediaProjectionManager) getApplication().getSystemService(
            Context.MEDIA_PROJECTION_SERVICE);
    startActivityForResult(
        mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
      return;
    mediaProjectionPermissionResultCode = resultCode;
    mediaProjectionPermissionResultData = data;
    startCall();
  }

  private boolean useCamera2() {
    return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
  }

  private boolean captureToTexture() {
    return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
  }

  private @Nullable VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
    final String[] deviceNames = enumerator.getDeviceNames();

    // First, try to find front facing camera
    Logging.d(TAG, "Looking for front facing cameras.");
    for (String deviceName : deviceNames) {
      if (enumerator.isFrontFacing(deviceName)) {
        Logging.d(TAG, "Creating front facing camera capturer.");
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

        if (videoCapturer != null) {
          return videoCapturer;
        }
      }
    }

    // Front facing camera not found, try something else
    Logging.d(TAG, "Looking for other cameras.");
    for (String deviceName : deviceNames) {
      if (!enumerator.isFrontFacing(deviceName)) {
        Logging.d(TAG, "Creating other camera capturer.");
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

        if (videoCapturer != null) {
          return videoCapturer;
        }
      }
    }

    return null;
  }

  @TargetApi(21)
  private @Nullable VideoCapturer createScreenCapturer() {
    if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
      reportError("User didn't give permission to capture the screen.");
      return null;
    }
    return new ScreenCapturerAndroid(
        mediaProjectionPermissionResultData, new MediaProjection.Callback() {
      @Override
      public void onStop() {
        reportError("User revoked permission to capture the screen.");
      }
    });
  }

  // Activity interfaces
  @Override
  public void onStop() {
    super.onStop();
    activityRunning = false;
    // Don't stop the video when using screencapture to allow user to show other apps to the remote
    // end.
    if (peerConnectionClient != null && !screencaptureEnabled) {
      peerConnectionClient.stopVideoSource();
    }
//    if (cpuMonitor != null) {
//      cpuMonitor.pause();
//    }
  }

  @Override
  public void onStart() {
    super.onStart();
    activityRunning = true;
    // Video is not paused for screencapture. See onPause.
    if (peerConnectionClient != null && !screencaptureEnabled) {
      peerConnectionClient.startVideoSource();
    }
//    if (cpuMonitor != null) {
//      cpuMonitor.resume();
//    }
  }

  @Override
  public void onResume(){
    super.onResume();
    Log.d(TAG, "onResume: ");

    // bind to Service
    Intent intent = new Intent(this, ChatService.class);
    bindService(intent, conn, Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onPause(){
    super.onPause();

    // Unbind from service
    if (isService) {
      currentRoomNo = -1;
      Log.d(TAG, "onPause: callactivity isService:" + isService);

      Log.d(TAG, "onPause: callactivity unbind");
      myService.setCallbacks(null); // unregister
      unbindService(conn);
      //isService = false;

      // 통화 종료 시 ReceiveCallActivity(통화 대기 액티비티)를 종료한다.
      if(RCA != null){
        RCA.finish();
      }
    }
  }

  public void sendMessageToMySQL(final int mode, final int roomNo, final String myEmail, final String yourEmail, final String message, final String date, final String time){
    Log.d(TAG, "sendMessageToMySQL: mode: " + mode);

    Api api = ApiClient.getClient().create(Api.class);

    Call<Room> call = api.sendMessage(mode, roomNo, myEmail, yourEmail, message, date, time);
    call.enqueue(new Callback<Room>() {

      @Override
      public void onResponse(Call<Room> call, final Response<Room> response) {
        Log.d("TAG", "onResponse: " + response.body().getResult());

      }

      @Override
      public void onFailure(Call<Room> call, Throwable t) {
        Log.d("ToMySQL Error",t.getMessage());
      }
    });
  }

  @Override
  protected void onDestroy() {
    Thread.setDefaultUncaughtExceptionHandler(null);
    disconnect();
    if (logToast != null) {
      logToast.cancel();
    }
    activityRunning = false;
    super.onDestroy();
  }

  // CallFragment.OnCallEvents interface implementation.
  @Override
  public void onCallHangUp() {
    Log.d(TAG, "onCallHangUp: ");

          // 현재 시간 받아오기
      long mNow;
      Date mDate;
      mNow = System.currentTimeMillis();
      mDate = new Date(mNow);
      // 메세지 전송시 날짜, 시간 생성
      SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
      final String date = mFormat.format(mDate);
      SimpleDateFormat mFormat2 = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);
      final String time = mFormat2.format(mDate);

      // 통화 종료 시 종료 메세지를 데이터 베이스에 저장한다.
      //sendMessageToMySQL(5, roomNo, senderEmail, senderEmail, "영상통화 종료", date, time);

      // 통화 종료를 누르는 사용자가 발신자와 동일할 경우 영상 통화 수신자에게 종료 메세지 전송
      if(loggedUseremail.equals(senderEmail)){
        Log.d(TAG, "onCallHangUp: 1");

        //receiverEmail = 영상 통화 수신자

        send = new SendThread(socket, 5, roomNo, senderEmail, receiverEmail, "영상통화 종료", date, time);
        send.start();

      // 통화 종료를 누르는 사용자가 수신자일 경우 영상 통화 발신자에게 종료 메세지 전송
      }else{
        Log.d(TAG, "onCallHangUp: 2");

        send = new SendThread(socket, 5, roomNo, senderEmail, senderEmail, "영상통화 종료", date, time);
        send.start();

      }



    disconnect();
  }

  // 내부 클래스  ( 메세지 전송용 )
  class SendThread extends Thread{
    Socket socket;
    int mode;
    int roomNo;
    String myEmail;
    String yourEmail;
    String message;
    String date;
    String time;
    DataOutputStream output;


    public SendThread(Socket socket, int mode, int roomNo, String myEmail, String yourEmail, String message, String date, String time){
      Log.d(TAG, "SendThread: ");

      Log.d(TAG, "SendThread: mode: " + mode);
      Log.d(TAG, "SendThread: message: " + message);
      this.mode = mode;
      this.socket = socket;
      this.roomNo = roomNo;
      this.myEmail = myEmail;
      this.yourEmail = yourEmail;
      this.message = message;
      this.date = date;
      this.time = time;
      this.output = dos; // ChatService의 아웃풋스트림 변수를 가져온다.
    }

    // 서버로 메세지 전송
    public  void run(){
      try {
        if (message != null){
          Log.d(TAG, "SendThread run: ");

          JSONObject jo = new JSONObject();

          Log.d(TAG, "SendThread run: mode: " + mode);

          jo.put("mode", mode);
          jo.put("roomNo", roomNo);
          jo.put("myEmail", myEmail);
          //jo.put("myProfile", profileImage);
          jo.put("yourEmail", yourEmail);
          jo.put("message", message);
          jo.put("date", date);
          jo.put("time", time);
          dos.writeUTF(jo.toString());
          finish();
        }

      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onCameraSwitch() {
    if (peerConnectionClient != null) {
      peerConnectionClient.switchCamera();
    }
  }

  @Override
  public void onVideoScalingSwitch(ScalingType scalingType) {
    fullscreenRenderer.setScalingType(scalingType);
  }

  @Override
  public void onCaptureFormatChange(int width, int height, int framerate) {
    if (peerConnectionClient != null) {
      peerConnectionClient.changeCaptureFormat(width, height, framerate);
    }
  }

  @Override
  public boolean onToggleMic() {
    if (peerConnectionClient != null) {
      micEnabled = !micEnabled;
      peerConnectionClient.setAudioEnabled(micEnabled);
    }
    return micEnabled;
  }

  // Helper functions.
  private void toggleCallControlFragmentVisibility() {
    if (!connected || !callFragment.isAdded()) {
      return;
    }
    // Show/hide call control fragment
    callControlFragmentVisible = !callControlFragmentVisible;
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (callControlFragmentVisible) {
      ft.show(callFragment);
      //ft.show(hudFragment);
    } else {
      ft.hide(callFragment);
      //ft.hide(hudFragment);
    }
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
  }

  private void startCall() {
    if (appRtcClient == null) {
      Log.e(TAG, "AppRTC client is not allocated for a call.");
      return;
    }
    callStartedTimeMs = System.currentTimeMillis();

    // Start room connection.
    logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
    appRtcClient.connectToRoom(roomConnectionParameters);

    // Create and audio manager that will take care of audio routing,
    // audio modes, audio device enumeration etc.
    audioManager = AppRTCAudioManager.create(getApplicationContext());
    // Store existing audio settings and change audio mode to
    // MODE_IN_COMMUNICATION for best possible VoIP performance.
    Log.d(TAG, "Starting the audio manager...");
    audioManager.start(new AudioManagerEvents() {
      // This method will be called each time the number of available audio
      // devices has changed.
      @Override
      public void onAudioDeviceChanged(
          AudioDevice audioDevice, Set<AudioDevice> availableAudioDevices) {
          onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
      }
    });
  }

  // Should be called from UI thread
  private void callConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    Log.i(TAG, "Call connected: delay=" + delta + "ms");
    if (peerConnectionClient == null || isError) {
      Log.w(TAG, "Call is connected in closed or error state");
      return;
    }
    // Enable statistics callback.
    peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    setSwappedFeeds(false /* isSwappedFeeds */);
  }

  // This method is called when the audio manager reports audio device change,
  // e.g. from wired headset to speakerphone.
  private void onAudioManagerDevicesChanged(
      final AudioDevice device, final Set<AudioDevice> availableDevices) {
    Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
            + "selected: " + device);
    // TODO(henrika): add callback handler.
  }

  // Disconnect from remote resources, dispose of local resources, and exit.
  private void disconnect() {
    activityRunning = false;
    remoteProxyRenderer.setTarget(null);
    localProxyVideoSink.setTarget(null);
    if (appRtcClient != null) {
      appRtcClient.disconnectFromRoom();
      appRtcClient = null;
    }
    if (pipRenderer != null) {
      pipRenderer.release();
      pipRenderer = null;
    }
    if (videoFileRenderer != null) {
      videoFileRenderer.release();
      videoFileRenderer = null;
    }
    if (fullscreenRenderer != null) {
      fullscreenRenderer.release();
      fullscreenRenderer = null;
    }
    if (peerConnectionClient != null) {
      peerConnectionClient.close();
      peerConnectionClient = null;
    }
    if (audioManager != null) {
      audioManager.stop();
      audioManager = null;
    }
    if (connected && !isError) {
      setResult(RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
    }
    Log.d(TAG, "disconnect: chat finish()");

    finish();
  }

  
  private void disconnectWithErrorMessage(final String errorMessage) {
    if (commandLineRun || !activityRunning) {
      Log.e(TAG, "Critical error: " + errorMessage);
      disconnect();
    } else {
      new AlertDialog.Builder(this)
          .setTitle(getText(R.string.channel_error_title))
          .setMessage(errorMessage)
          .setCancelable(false)
          .setNeutralButton(R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
                  disconnect();
                }
              })
          .create()
          .show();
    }
  }

  // Log |msg| and Toast about it.
  private void logAndToast(String msg) {
    Log.d(TAG, msg);
    if (logToast != null) {
      logToast.cancel();
    }
    logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    logToast.show();
  }

  private void reportError(final String description) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!isError) {
          isError = true;
          disconnectWithErrorMessage(description);
        }
      }
    });
  }

  private @Nullable VideoCapturer createVideoCapturer() {
    final VideoCapturer videoCapturer;
    String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
    if (videoFileAsCamera != null) {
      try {
        videoCapturer = new FileVideoCapturer(videoFileAsCamera);
      } catch (IOException e) {
        reportError("Failed to open video file for emulated camera");
        return null;
      }
    } else if (screencaptureEnabled) {
      return createScreenCapturer();
    } else if (useCamera2()) {
      if (!captureToTexture()) {
        reportError(getString(R.string.camera2_texture_only_error));
        return null;
      }

      Logging.d(TAG, "Creating capturer using camera2 API.");
      videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
    } else {
      Logging.d(TAG, "Creating capturer using camera1 API.");
      videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
    }
    if (videoCapturer == null) {
      reportError("Failed to open camera");
      return null;
    }
    return videoCapturer;
  }

  private void setSwappedFeeds(boolean isSwappedFeeds) {
    Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
    this.isSwappedFeeds = isSwappedFeeds;
    localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
    remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
    fullscreenRenderer.setMirror(isSwappedFeeds);
    pipRenderer.setMirror(!isSwappedFeeds);
  }

  // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
  // All callbacks are invoked from websocket signaling looper thread and
  // are routed to UI thread.
  private void onConnectedToRoomInternal(final SignalingParameters params) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;

    signalingParameters = params;
    logAndToast("Creating peer connection, delay=" + delta + "ms");
    VideoCapturer videoCapturer = null;
    if (peerConnectionParameters.videoCallEnabled) {
      videoCapturer = createVideoCapturer();
    }
    peerConnectionClient.createPeerConnection(
        localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters);

    if (signalingParameters.initiator) {
      logAndToast("Creating OFFER...");
      // Create offer. Offer SDP will be sent to answering client in
      // PeerConnectionEvents.onLocalDescription event.
      peerConnectionClient.createOffer();
    } else {
      if (params.offerSdp != null) {
        peerConnectionClient.setRemoteDescription(params.offerSdp);
        logAndToast("Creating ANSWER...");
        // Create answer. Answer SDP will be sent to offering client in
        // PeerConnectionEvents.onLocalDescription event.
        peerConnectionClient.createAnswer();
      }
      if (params.iceCandidates != null) {
        // Add remote ICE candidates from room.
        for (IceCandidate iceCandidate : params.iceCandidates) {
          peerConnectionClient.addRemoteIceCandidate(iceCandidate);
        }
      }
    }
  }

  @Override
  public void onConnectedToRoom(final SignalingParameters params) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        onConnectedToRoomInternal(params);
      }
    });
  }

  @Override
  public void onRemoteDescription(final SessionDescription sdp) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
          return;
        }
        logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms");
        peerConnectionClient.setRemoteDescription(sdp);
        if (!signalingParameters.initiator) {
          logAndToast("Creating ANSWER...");
          // Create answer. Answer SDP will be sent to offering client in
          // PeerConnectionEvents.onLocalDescription event.
          peerConnectionClient.createAnswer();
        }
      }
    });
  }

  @Override
  public void onRemoteIceCandidate(final IceCandidate candidate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
          return;
        }
        peerConnectionClient.addRemoteIceCandidate(candidate);
      }
    });
  }

  @Override
  public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
          return;
        }
        peerConnectionClient.removeRemoteIceCandidates(candidates);
      }
    });
  }

  @Override
  public void onChannelClose() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("Remote end hung up; dropping PeerConnection");
        disconnect();
      }
    });
  }

  @Override
  public void onChannelError(final String description) {
    reportError(description);
  }

  // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
  // Send local peer connection SDP and ICE candidates to remote party.
  // All callbacks are invoked from peer connection client looper thread and
  // are routed to UI thread.
  @Override
  public void onLocalDescription(final SessionDescription sdp) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (appRtcClient != null) {
          logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms");
          if (signalingParameters.initiator) {
            appRtcClient.sendOfferSdp(sdp);
          } else {
            appRtcClient.sendAnswerSdp(sdp);
          }
        }
        if (peerConnectionParameters.videoMaxBitrate > 0) {
          Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
          peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
        }
      }
    });
  }

  @Override
  public void onIceCandidate(final IceCandidate candidate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (appRtcClient != null) {
          appRtcClient.sendLocalIceCandidate(candidate);
        }
      }
    });
  }

  @Override
  public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (appRtcClient != null) {
          appRtcClient.sendLocalIceCandidateRemovals(candidates);
        }
      }
    });
  }

  @Override
  public void onIceConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("ICE connected, delay=" + delta + "ms");
      }
    });
  }

  @Override
  public void onIceDisconnected() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("ICE disconnected");
      }
    });
  }

  @Override
  public void onConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("DTLS connected, delay=" + delta + "ms");
        connected = true;
        callConnected();
      }
    });
  }

  @Override
  public void onDisconnected() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("DTLS disconnected");
        connected = false;
        disconnect();
      }
    });
  }

  @Override
  public void onPeerConnectionClosed() {}

  @Override
  public void onPeerConnectionStatsReady(final StatsReport[] reports) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!isError && connected) {
          //hudFragment.updateEncoderStatistics(reports);
        }
      }
    });
  }

  @Override
  public void onPeerConnectionError(final String description) {
    reportError(description);
  }

  private ServiceConnection conn = new ServiceConnection() {
    public void onServiceConnected(ComponentName name,
                                   IBinder service) {

      // 서비스와 연결되었을 때 호출되는 메서드
      // cast the IBinder and get MyService instance
      ChatService.LocalBinder binder = (ChatService.LocalBinder) service;
      // getService 메소드를 사용해서 service객체를 사용한다.
      myService = (ChatService) binder.getService();
      myService.setCallbacks(CallActivity.this); // register

      isService = true; // 실행 여부를 판단
    }
    public void onServiceDisconnected(ComponentName name) {
      // 서비스와 연결이 끊기거나 종료되었을 때
      isService = false;
    }
  };

  public void ChatdoSomething(){
    Log.d(TAG, "ChatdoSomething: callback disconnect");

    //disconnect();
    finish();
  }

}
