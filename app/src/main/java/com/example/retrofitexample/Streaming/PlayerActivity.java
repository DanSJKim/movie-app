package com.example.retrofitexample.Streaming;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.BoxOffice.Wallet.config;
import com.example.retrofitexample.Chat.ChatRoomActivity;
import com.example.retrofitexample.Chat.ChatService;
import com.example.retrofitexample.Chat.Model.MessageContent;
import com.example.retrofitexample.Chat.Model.MessageContentAdapter;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Streaming.Model.StreamingMessage;
import com.example.retrofitexample.Streaming.Model.StreamingMessageAdapter;
import com.google.android.gms.common.api.internal.StatusCallback;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.utils.Convert;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import info.bcdev.librarysdkew.GetCredentials;
import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.LoadSmartContract;
import info.bcdev.librarysdkew.utils.InfoDialog;
import info.bcdev.librarysdkew.utils.ToastMsg;
import info.bcdev.librarysdkew.wallet.SendingToken;
import info.bcdev.librarysdkew.web3j.Initiate;

import static com.example.retrofitexample.BootReceiver.isService;
import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;
import static com.example.retrofitexample.Chat.ChatService.currentRoomNo;
import static com.example.retrofitexample.Chat.ChatService.dos;
import static com.example.retrofitexample.Chat.ChatService.socket;
import static com.example.retrofitexample.Streaming.StreamingListActivity.roomcount;


/**
 * 방송 시청 액티비티
 */

public class PlayerActivity extends AppCompatActivity implements ChatService.ServiceCallbacks, CBGetCredential, CBLoadSmartContract, CBBip44,CBSendingToken {
    private static final String TAG = "PlayerActivity: ";

    WOWZPlayerView mStreamPlayerView; // 방송 플레이어
    WOWZPlayerConfig mStreamPlayerConfig; // 방송 시청에 필요한 설정
    private ChatService myService; // 서비스
    EditText etMessage; // 채팅 메세지 입력 폼
    ImageView ivSend; // 채팅 메세지 전송 버튼
    SendThread send; // 메세지 전송 스레드
    String profileImage; //SharedPreference로 부터 가져온 내 이미지
    ImageView ivSendToken;

    // 리사이클러뷰 변수
    private static RecyclerView mRecyclerView;
    private static ArrayList<StreamingMessage> mArrayList;
    private static StreamingMessageAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    int roomNo; // 현재 방 번호
    int pos;


    // 토큰 관련 변수
    String walletAddress; // 방장 지갑 주소

    private InfoDialog mInfoDialog; // 지갑 불러오기 다이얼로그

    private Credentials mCredentials; // 자격 증명

    private String mPasswordwallet = config.passwordwallet(); // 지갑 비밀번호

    private File keydir; // 키 경로

    private String mSmartcontract = config.addresssmartcontract(1); // 스마트 컨트랙트 주소

    private String mNodeUrl = config.addressethnode(2); // 테스트넷 주소

    private Web3j mWeb3j; // 테스트넷 라이브러리

    private BigInteger mGasPrice; // 가스 비용

    private BigInteger mGasLimit; // 가스 제한

    AlertDialog.Builder builder; // 토큰 보내기 다이얼로그
    AlertDialog ad;

    private SendingToken sendingToken; // 토큰 보내기

    String balance; // 나의 토큰 잔액

    public static String tokenBalance; // 선물 할 토큰 액수

    private ToastMsg toastMsg; // 토스트 전송 완료 토스트 메세지

    ImageView ivChatVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Log.d("PlayerActivity: ", "onCreate: 방송 시청 1: 방송 시청 액티비티 시작");

        Intent intent = getIntent();
        roomNo = intent.getIntExtra("roomNo", -111); // 스트리밍 방 번호를 받아 온다.
        walletAddress = intent.getStringExtra("walletAddress"); // 방장 지갑 주소
        Log.d(TAG, "onCreate: walletAddress: " + walletAddress);

        currentRoomNo = roomNo; // 내가 현재 있는 위치를 스트리밍 방 번호로 변경한다.
        Log.d(TAG, "onCreate: 현재 방 번호: " + currentRoomNo);
        pos = intent.getIntExtra("pos", -222);

        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);
        WowzaGoCoder goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-BE46-010C-D7F5-A882-CDEE");

        etMessage = (EditText) findViewById(R.id.etPlayerChatContent); // 채팅 메세지 입력 폼
        ivSend = (ImageView) findViewById(R.id.ivPlayerChatSend); // 채팅 메세지 전송 버튼

        profileImage = SharedPref.getInstance(this).StoredProfileImage(); // 내 프로필 이미지

        // 방송에 필요한 설정을 입력한다.
        mStreamPlayerConfig = new WOWZPlayerConfig();
        mStreamPlayerConfig.setIsPlayback(true);
        mStreamPlayerConfig.setHostAddress("a092d0.entrypoint.cloud.wowza.com");
        mStreamPlayerConfig.setPortNumber(1935);

        //if(pos == 0){
            mStreamPlayerConfig.setApplicationName("app-fc5b");
            mStreamPlayerConfig.setStreamName("fa06bb8a");
            mStreamPlayerConfig.setUsername("client44490");
//        }else if(pos == 1){
//            mStreamPlayerConfig.setApplicationName("app-fee6");
//            mStreamPlayerConfig.setStreamName("89fcd457");
//            mStreamPlayerConfig.setUsername("client44490");
//        }

        // WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        mStreamPlayerView.setScaleMode(WOWZMediaConfig.FILL_VIEW);
//        mStreamPlayerConfig.setHLSEnabled(true);
//        mStreamPlayerConfig.setHLSBackupURL("http://[a092d0.entrypoint.cloud.wowza.com]:1935/[app-fc5b]/[fa06bb8a]/playlist.m3u8");

        WOWZStatusCallback statusCallback = new StatusCallback();
        //mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Handler", "onCreate run: 방송 시청 2: play메소드를 바로 시작하면 오류가 발생하기 때문에 딜레이를 주고 play메소드를 실행한다.");

                // 처음 방송 실행 시에 시스템 메세지를 띄워주기 위해 핸들러를 호출한다.
                // 핸들러에게 전달할 메세지 객체
                Message hdmg = msgHandler.obtainMessage();

                // 핸들러에게 전달할 메세지의 식별자
                hdmg.what = 3333;

                // 핸들러에게 메세지 전달 ( 화면 처리 )
                msgHandler.sendMessage(hdmg);

                // player의 설정과
                mStreamPlayerView.play(mStreamPlayerConfig, statusCallback/*WOWZStatusCallback*/);
            }
        }, 2000);

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString(); // 채팅 메세지

                // 아무것도 입력하지 않았을 때
                if (message == null || TextUtils.isEmpty(message) || message.equals("") || message.trim().matches("")) {
                    Log.d(TAG, "onClick: 아무것도 입력하지 않음.");
                    Toast.makeText(PlayerActivity.this, "메세지를 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {
                    Log.d(TAG, "onClick: 채팅 보내기 1: 클릭 시 SendThread 실행");
                    send = new SendThread(socket, 8, roomNo, profileImage, loggedUseremail, message); // ChatService에서 생성한 클라이언트 소켓 변수
                    send.start();
                }

            }
        });

        ivSendToken = (ImageView) findViewById(R.id.sendVTCToken);
        ivSendToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 별풍선 보내기 버튼 클릭");
                show();
            }
        });

        mArrayList = new ArrayList<>();

        //리사이클러뷰 초기화
        mRecyclerView = (RecyclerView) findViewById(R.id.rvPlayerChat);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new StreamingMessageAdapter(mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        ivChatVisible = (ImageView) findViewById(R.id.ivChatVisible);
        ivChatVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mRecyclerView.getVisibility() == View.VISIBLE){
                    Log.d(TAG, "onClick: 채팅 창 끄기");
                    mRecyclerView.setVisibility(View.INVISIBLE);
                }else if(mRecyclerView.getVisibility() == View.INVISIBLE){
                    Log.d(TAG, "onClick: 채팅 창 켜기");
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    // 토큰 선물하기다이얼로그
    void show()
    {
        Log.d(TAG, "show: 토큰 선물 다이얼로그");
        final EditText edittext = new EditText(this);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("VTC 토큰 선물하기");
        builder.setMessage("VTC 잔액: ");
        builder.setView(edittext);
        edittext.setHint("선물 할 개수");
        builder.setPositiveButton("선물 하기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),edittext.getText().toString() ,Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onClick: 토큰 선물하기");
                        tokenBalance = edittext.getText().toString();
                        Log.d(TAG, "onClick: 보낼 토큰 액수: " + tokenBalance);

                        sendingToken = new SendingToken(mWeb3j,
                                mCredentials,
                                "0",
                                "0");
                        sendingToken.registerCallBackToken(PlayerActivity.this);
                        sendingToken.Send(mSmartcontract,walletAddress,tokenBalance);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                });


        ad = builder.create();
        ad.show();
        // 지갑
        toastMsg = new ToastMsg(); // 토스트 메세지
        getWeb3j();
        keydir = this.getFilesDir();
        getCredentials(keydir);
    }



    // Activity에서 Bind
    // 2-1 Connection 클래스 객체 생성 및 구현
    // Service에서 bind한 것에 대한 결과로, 연결이 되거나, 끊어지기도 하는데 그 상태에 대한 Callback들이다.
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected: onResume()에서 서비스가 bind 된다.");

            // 서비스와 연결되었을 때 호출되는 메서드
            // cast the IBinder and get MyService instance
            ChatService.LocalBinder binder = (ChatService.LocalBinder) service;
            // getService 메소드를 사용해서 service객체를 사용한다.
            myService = binder.getService();
            myService.setCallbacks(PlayerActivity.this); // register

            isService = true; // 실행 여부를 판단
        }
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊기거나 종료되었을 때
            isService = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        currentRoomNo = roomNo;

        Log.d(TAG, "onResume: isService: " + isService);
        // bind to Service
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    // 방송 시청 상태를 알려주는 클래스
    class StatusCallback implements WOWZStatusCallback{
        @Override
        public void onWZStatus(WOWZStatus wzStatus) {
            Log.d(TAG, "onWZStatus : 방송 시청 3: 방송이 시작되거나 중지될 때 상태를 나타낸다.");
            Log.d("시청 상태: ", "onWZStatus: " + wzStatus);

            // 방송 종료 시 IDLE 상태로 진입한다.
            if(wzStatus.isIdle()){
                Log.d(TAG, "onWZStatus: 방송 종료 1: 방송 종료 시 다이얼로그를 띄워 준다.");

                // 처음 방송 실행 시에 시스템 메세지를 띄워주기 위해 핸들러를 호출한다.
                // 핸들러에게 전달할 메세지 객체
                Message hdmg = msgHandler.obtainMessage();

                // 핸들러에게 전달할 메세지의 식별자
                hdmg.what = 4444;

                // 핸들러에게 메세지 전달 ( 화면 처리 )
                msgHandler.sendMessage(hdmg);
            }

        }
        @Override
        public void onWZError(WOWZStatus wzStatus) {
            Log.d(TAG, "onWZError : 방송 시청 3: 방송이 시작되거나 중지될 때 에러 상태를 나타낸다.");
            Log.d("시청 에러: ", "onWZError: " + wzStatus);
        }
    }

    //콜백 메소드
    public void ChatdoSomething(){
        Log.d(TAG, "ChatdoSomething: 스트리밍 방 메세지 수신 시 호출");

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

    // 내부 클래스  ( 메세지 전송용 )
    class SendThread extends Thread{
        Socket socket;
        int mode;
        int roomNo;
        String myEmail;
        String profile;
        String message;
        DataOutputStream output;

        public SendThread(Socket socket, int mode, int roomNo, String profile, String myEmail, String message){
            Log.d(TAG, "SendThread: 채팅 보내기 2: 생성자");

            Log.d(TAG, "SendThread: mode: " + mode);
            Log.d(TAG, "SendThread: message: " + message);
            this.mode = mode;
            this.socket = socket;
            this.roomNo = roomNo;
            this.profile = profile;
            this.myEmail = myEmail;
            this.message = message;
            this.output = dos; // ChatService의 아웃풋스트림 변수를 가져온다.
        }

        // 서버로 메세지 전송
        public  void run(){
            try {
                if (message != null){
                    Log.d(TAG, "SendThread 채팅 보내기 3: run");

                    JSONObject jo = new JSONObject();
                    Log.d(TAG, "SendThread run: 메세지 mode: " + mode);

                    jo.put("mode", mode);
                    jo.put("roomNo", roomNo);
                    jo.put("myEmail", myEmail);
                    jo.put("myProfile", profile);
                    jo.put("message", message);

                    dos.writeUTF(jo.toString());

                    /* 핸들러 */
                    // 핸들러에게 전달할 메세지 객체
                    // 메세지를 보낼 때 나의 채팅 UI를 처리 하기 위한 핸들러
                    Message hdmg = msgHandler.obtainMessage();

                    // 핸들러에게 전달할 메세지의 식별자
                    hdmg.what = 2222;

                    // 메세지의 본문
                    hdmg.obj = jo.toString();

                    // 핸들러에게 메세지 전달 ( 화면 처리 )
                    msgHandler.sendMessage(hdmg);

                    // 채팅 목록에 메세지를 받은 상대방의 아이디가 없으면
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
                Log.d(TAG, "handleMessage: 상대방이 보낸 메세지를 서버로 부터 수신했을 때");
                Log.d(TAG, "handleMessage: message: " + msg.obj.toString());

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(msg.obj.toString());
                    String mode = jsonObject.getString("mode");
                    int modeToInt = Integer.parseInt(mode);
                    Log.d(TAG, "handleMessage: modeToInt: " + modeToInt);

                        String roomNo = jsonObject.getString("roomNo"); // 현재 방 번호
                        String myEmail = jsonObject.getString("myEmail"); // 내 이메일
                        String myProfile = jsonObject.getString("myProfile"); // 내 프로필 이미지
                        String message = jsonObject.getString("message"); // 보낼 메세지
                        int roomNoToInt = Integer.parseInt(roomNo);
                        Log.d(TAG, "handleMessage: roomNo: " + roomNo);
                        Log.d(TAG, "handleMessage: myEmail: " + myEmail);
                        Log.d(TAG, "handleMessage: myProfile: " + myProfile);
                        Log.d(TAG, "handleMessage: message: " + message);

                        // 리사이클러뷰 어댑터 갱신
                        StreamingMessage messageContent = new StreamingMessage(modeToInt, myProfile, myEmail, message);
                        mArrayList.add(messageContent);
                        mAdapter.notifyDataSetChanged();

                        //맨 아래 아이템으로 스크롤
                        mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

                    // 키보드 내려주기
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(btnChatSend.getWindowToken(),0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // 메세지 보낼 때
            }else if(msg.what == 2222){
                Log.d(TAG, "handleMessage: 채팅 보내기 4: 메세지를 보낼 때");
                Log.d(TAG, "handleMessage: 2222");
                Log.d(TAG, "handleMessage: jo.tostring(): " + msg.obj);

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;

                try {

                    jsonObject = new JSONObject(msg.obj.toString());

                    int mode = jsonObject.getInt("mode"); // 메세지 유형
                    String roomNo = jsonObject.getString("roomNo"); // 방 번호
                    String myEmail = jsonObject.getString("myEmail"); // 내 이메일
                    String myProfile = jsonObject.getString("myProfile"); // 내 프로필
                    String message = jsonObject.getString("message"); // 내 메세지

                    int roomNoToInt = Integer.parseInt(roomNo);

                    Log.d(TAG, "handleMessage: mode: " + mode);
                    Log.d(TAG, "handleMessage: roomNo: " + roomNo);
                    Log.d(TAG, "handleMessage: message: " + message);

                    //내 채팅내용
                    StreamingMessage messageContent = new StreamingMessage(mode, myProfile, myEmail, message); // 메세지 설정
                    mArrayList.add(messageContent); // 메세지 추가
                    mAdapter.notifyDataSetChanged(); // 데이터 변화 확인

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
                Log.d(TAG, " 방송 시작 메세지 1: onCreate() -> handleMessage: 처음 방송 실행 시 추가 할 시스템 메세지");

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

            }else if(msg.what == 4444){ // 방송 종료 다이얼로그 띄우기
                Log.d(TAG, "handleMessage: 방송 종료 2: 방송 종료 시 다이얼로그를 띄워 준다.");
                AlertDialog alertDialog = new AlertDialog.Builder(PlayerActivity.this).create();
                alertDialog.setTitle("방송이 종료되었습니다.");
//                alertDialog.setMessage("Alert message to be shown");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called 액티비티가 화면에 보이지 않음");

        currentRoomNo = -1; // 화면이 보이지 않을 떄는 방 번호를 -1로 지정해 준다

        mStreamPlayerView.stop();
        // Unbind from service
        if (isService) {
            Log.d(TAG, "onPause(): unbind 서비스 연결 해제");
            currentRoomNo = -1;
            myService.setCallbacks(null); // unregister
            unbindService(conn);
            //isService = false;
        }
    }

    /* Get Credentials */
    private void getCredentials(File keydir){
        File[] listfiles = keydir.listFiles();
        try {
            Log.e(TAG, "getCredentials: 앱 실행 6.");
            mInfoDialog = new InfoDialog(this);
            mInfoDialog.Get("Load Wallet","Please wait few seconds");
            GetCredentials getCredentials = new GetCredentials();
            getCredentials.registerCallBack(this);
            getCredentials.FromFile(listfiles[0].getAbsolutePath(),mPasswordwallet);
        } catch (IOException e) {

            Log.e(TAG, "getCredentials: IOException " + e);
            e.printStackTrace();
        } catch (CipherException e) {

            Log.e(TAG, "getCredentials: CipherException " + e);
            e.printStackTrace();
        }
    }

    /*Get Token Info*/
    private void GetTokenInfo(){
        Log.e(TAG, "GetTokenInfo: " + "앱 실행 9. 토큰 정보 가져 오기 ");
        mGasPrice = Convert.toWei("20",Convert.Unit.GWEI).toBigInteger();
        mGasLimit = BigInteger.valueOf(Long.valueOf("42000"));
        LoadSmartContract loadSmartContract = new LoadSmartContract(mWeb3j,mCredentials,mSmartcontract,mGasPrice,mGasLimit);
        Log.e(TAG, "GetTokenInfo: mWeb3j: " + mWeb3j);
        Log.e(TAG, "GetTokenInfo: mCredentials: " + mCredentials);
        Log.e(TAG, "GetTokenInfo: mSmartcontract: " + mSmartcontract);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }

    /* Get Web3j*/
    private void getWeb3j(){
        Log.e(TAG, "getWeb3j: 앱 실행 5. 이더리움 테스트넷 연결을 위한 라이브러리 실행");
        new Initiate(mNodeUrl); // 테스트넷 주소 설정
        mWeb3j = Initiate.sWeb3jInstance;
    }

    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        Log.d(TAG, "backGeneration: ");

    }

    @Override
    public void backLoadCredential(Credentials credentials) {
        Log.d(TAG, "backLoadCredential: ");

        Log.e(TAG, "backLoadCredential: 앱 실행. 지갑을 불러 온다.");
        mCredentials = credentials;
        Log.e(TAG, "backLoadCredential: mCredentials: " + mCredentials);
        mInfoDialog.Dismiss();
        GetTokenInfo();
    }

    @Override
    public void backLoadSmartContract(Map<String, String> result) { // 토큰의 잔액을 가져 온다.
        balance = result.get("tokenbalance");
        Log.d(TAG, "backLoadSmartContract: result.get(tokenbalance)" + balance);

        // 단위 변환
        String token = Convert.fromWei(String.valueOf(balance), Convert.Unit.ETHER).toString(); // 토큰 잔액

        ad.setMessage("VTC 잔액: " + token);
    }

    @Override
    public void backSendToken(TransactionReceipt result) {
        Log.d(TAG, "backSendToken: 토큰 전송 완료 " + result.getTransactionHash());

        send = new SendThread(socket, 8, roomNo, "Token", loggedUseremail, loggedUseremail + "님이 VTC 토큰 " + tokenBalance + " 개를 선물 했습니다."); // ChatService에서 생성한 클라이언트 소켓 변수
        send.start();

        toastMsg.Long(this,result.getTransactionHash());
    }
}



