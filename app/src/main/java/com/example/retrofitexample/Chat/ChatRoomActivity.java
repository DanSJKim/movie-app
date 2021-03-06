package com.example.retrofitexample.Chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.Board.BoardActivity;
import com.example.retrofitexample.Board.BoardUpdateActivity;
import com.example.retrofitexample.Board.BoardUploadActivity;
import com.example.retrofitexample.Board.CirclePagerIndicatorDecoration;
import com.example.retrofitexample.Board.Image.BoardImageUploadAdapter;
import com.example.retrofitexample.Board.Image.ResponseImages;
import com.example.retrofitexample.Chat.Model.DatabaseHelper;
import com.example.retrofitexample.Chat.Model.MessageContent;
import com.example.retrofitexample.Chat.Model.MessageContentAdapter;
import com.example.retrofitexample.Chat.Model.MessageContentResponse;
import com.example.retrofitexample.Chat.Model.MessageListAdapter;
import com.example.retrofitexample.Chat.Model.ResponseResult;
import com.example.retrofitexample.Chat.Model.Room;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.MyPage.MyPageUpdateActivity;
import com.example.retrofitexample.MyPage.MyPage_Img;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.VideoCall.CallActivity;
import com.example.retrofitexample.VideoCall.ConnectActivity;
import static com.example.retrofitexample.BootReceiver.isService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;
import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUsername;
import static com.example.retrofitexample.Chat.ChatService.currentRoomNo;
import static com.example.retrofitexample.Chat.ChatService.dos;
import static com.example.retrofitexample.Chat.ChatService.socket;

public class ChatRoomActivity extends AppCompatActivity implements ChatService.ServiceCallbacks, MessageContentAdapter.MessageContentRecyclerViewClickListener{

    public static final String TAG = "ChatRoomActivity : ";

    private static RecyclerView mRecyclerView;
    private static ArrayList<MessageContent> mArrayList;
    private static MessageContentAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private static final int CLIENT_TEXT_UPDATE = 200;

    String profileImage; //SharedPreference로 부터 가져온 내 이미지

    ImageView ivChatImage; // 이미지 전송 버튼
    EditText etChatMessage; // 메세지 입력 폼
    Button btnChatSend; // 메세지 전송 버튼
    SendThread send; // 메세지 전송 스레드
    ResponseThread response; // 읽음 스레드

    DatabaseHelper db;

    private ChatService myService; // 서비스
    //private boolean isService = false; // 서비스 실행 확인

    Toolbar myToolbar; // 상단 툴바

    String yourEmail; // intent로 받아 온 상대방 이메일

    int backbuttonflag; // 뒤로가기 버튼을 눌렀을 때 채팅 리스트 액티비티를 재실행해서 메세지를 갱신하기 위해

    List<Uri> uriList; // 이미지 Uri 리스트

    static int roomNo; // 방을 왔다갔다 할 때 방 번호를 저장해두기 위해 선언

    ArrayList<MessageContent> data;

    int videoCall; // 영상 통화 메세지 여부



    //webrtc
    private static final int CONNECTION_REQUEST = 1;
    // 퍼미션 요청 번호
    private static final int PERMISSION_REQUEST = 2;
    private static boolean commandLineRun;

    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefRoomServerUrl;

    // 영상통화 방의 고유 번호
    String uniqueID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // 회원 이메일
        Log.d(TAG, "onCreate: " + loggedUseremail);

        db = new DatabaseHelper(getApplicationContext());

        etChatMessage = (EditText) findViewById(R.id.etChatContent);

        uriList = new ArrayList(); // 이미지 담는 리스트

        // 채팅 할 상대방 이메일 from BoardActivity
        Intent intent = getIntent();
        roomNo = intent.getIntExtra("roomNo", -1);
        Log.d(TAG, "onCreate: 채팅방 번호: " + roomNo);
        backbuttonflag = intent.getIntExtra("backbuttonflag", -1);
        Log.d(TAG, "onCreate: 채팅방 Flag: " + backbuttonflag);
        yourEmail = intent.getStringExtra("yourEmail");
        Log.d(TAG, "onCreate: 채팅방 yourEmail: " + yourEmail);
        videoCall = intent.getIntExtra("videoCall", -1);
        Log.d(TAG, "onCreate: 채팅방 videoCall: " + videoCall);

        // 현재 방 번호
        currentRoomNo = roomNo;

        Log.d(TAG, "onCreate: currentRoomNo: " + currentRoomNo);
        Log.d(TAG, "onCreate: yourEmail: " + yourEmail);

        profileImage = SharedPref.getInstance(this).StoredProfileImage(); // 내 프로필 이미지

        //Toolbar를 생성한다.
        myToolbar = (Toolbar) findViewById(R.id.chatroom_my_toolbar);
        myToolbar.setTitle(yourEmail);
        setSupportActionBar(myToolbar);

        mArrayList = new ArrayList<>();

        //리사이클러뷰 초기화
        mRecyclerView = (RecyclerView) findViewById(R.id.chat_log_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new MessageContentAdapter(this, mArrayList);
        mAdapter.setOnClickListener(ChatRoomActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        ivChatImage = (ImageView) findViewById(R.id.ivChatImage);
        ivChatImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ChatRoomActivity.this);

                ad.setTitle("게시물");       // 제목 설정

                // 수정 버튼 설정
                ad.setPositiveButton("갤러리", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);

                        dialog.dismiss();//닫기
                    }
                });
                // 중립 버튼 설정
                ad.setNeutralButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();     //닫기
                    }
                });

                ad.show();
            }
        });

        setSendBtn(roomNo, loggedUseremail, yourEmail); // 채팅 내용 전송

        if(videoCall == 1){

            // 현재 시간 받아오기
            long mNow;
            Date mDate;
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);

            // 메세지 전송시 날짜, 시간 생성
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
            String date = mFormat.format(mDate);
            SimpleDateFormat mFormat2 = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);
            String time = mFormat2.format(mDate);
            Log.d(TAG, "run: date: " + date);
            Log.d(TAG, "run: time: " + time);

            Log.d(TAG, "onClick: roomNo: " + roomNo);
            Log.d(TAG, "onClick: myEmail: " + loggedUseremail);
            Log.d(TAG, "onClick: yourEmail: " + yourEmail);

            // 영상통화 메세지 번호: 4
            sendMessageToMySQL(4, roomNo, loggedUseremail, yourEmail, "영상통화 해요", date, time);

//            Intent callIntent = new Intent(ChatRoomActivity.this, ConnectActivity.class);
//            callIntent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivity(callIntent);
        }
    }

    // 상대방 메세지 카운트 0으로 감소시키기
    public void checkMessageCount(){
        Api api = ApiClient.getClient().create(Api.class);
        Log.d(TAG, "checkMessageCount: mail: " + loggedUseremail);
        Log.d(TAG, "checkMessageCount: roomno: " + roomNo);

        Call<MessageContent> call = api.checkMessageCount(loggedUseremail, roomNo);
        call.enqueue(new Callback<MessageContent>() {
            @Override
            public void onResponse(Call<MessageContent> call, final Response<MessageContent> response) {
                Log.d(TAG, "checkMessage onResponse: " + response.body().getContent());
                
            }
            @Override
            public void onFailure(Call<MessageContent> call, Throwable t) {

                Log.d("checkMessage Error!",t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== 1 && resultCode==RESULT_OK && data!=null)
        {

            //단일 이미지
            if(data.getData() != null) {
                Log.d(TAG, "onActivityResult: one image: " + data.getData());

                Uri uri = data.getData();
                //uriList.add(uri);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    // bitmap -> String 변환
                    String image = convertToString(bitmap);

                    // 현재 시간 받아오기
                    long mNow;
                    Date mDate;
                    mNow = System.currentTimeMillis();
                    mDate = new Date(mNow);

                    // 메세지 전송시 날짜, 시간 생성
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
                    String date = mFormat.format(mDate);
                    SimpleDateFormat mFormat2 = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);
                    String time = mFormat2.format(mDate);
                    Log.d(TAG, "run: date: " + date);
                    Log.d(TAG, "run: time: " + time);

                    sendMessageToMySQL(1, roomNo, loggedUseremail, yourEmail, image, date, time);

                    //맨 아래 아이템으로 스크롤
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //다중 이미지
            if(data.getClipData() != null) {

                ClipData clipData = data.getClipData();
                Log.d(TAG, "onActivityResult: multiple images: " + clipData);

                for (int i = 0; i < data.getClipData().getItemCount(); i++) {

                    //clipdata에서 각각의 이미지 uri 꺼내기
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    Log.e("uri item ", uri + " ");

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        // bitmap -> String 변환
                        String image = convertToString(bitmap);

                        // 현재 시간 받아오기
                        long mNow;
                        Date mDate;
                        mNow = System.currentTimeMillis();
                        mDate = new Date(mNow);

                        // 메세지 전송시 날짜, 시간 생성
                        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
                        String date = mFormat.format(mDate);
                        SimpleDateFormat mFormat2 = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);
                        String time = mFormat2.format(mDate);
                        Log.d(TAG, "run: date: " + date);
                        Log.d(TAG, "run: time: " + time);

                        sendMessageToMySQL(2, roomNo, loggedUseremail, yourEmail, image, date, time);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//                    uriList.add(selectedImage);
                }
            }
        }

        // 영상 통화
        Log.d(TAG, "onActivityResult: commandLineRun: " + commandLineRun);
        if (requestCode == CONNECTION_REQUEST && commandLineRun) {
            Log.d(TAG, "Return: " + resultCode);
            setResult(resultCode);
            commandLineRun = false;
            finish();
        }
    }

    //Bitmap -> String 변환
    private String convertToString(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //(압축 옵션( JPEG, PNG ), 품질 설정 ( 0 - 100까지의 int형 ), 압축된 바이트배열을 담을 stream)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        //imgByte는 세번째 인자인 byteArrayOutputStream의 toByteArray() 메서드를 통해 반환받을 수 있다.
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte,Base64.DEFAULT);
    }



    public void getMessageList(int roomNumber){
        Log.d(TAG, "getMessageList: roomNumber: " + roomNumber);

        Api api = ApiClient.getClient().create(Api.class);

        Call<MessageContentResponse> call = api.getChatRoomMessage(roomNumber);
        call.enqueue(new Callback<MessageContentResponse>() {
            @Override
            public void onResponse(Call<MessageContentResponse> call, final Response<MessageContentResponse> response) {
                Log.d("TAG", "getMessageList onResponse: " + response.body().getMessagelist());


                //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
               data = new ArrayList<>(Arrays.asList(response.body().getMessagelist()));

                // 매번 불러올 때마다 리스트에 아이템이 중복돼서 추가되지 않게 clear해 준다.
                mArrayList.clear();

                for(int i = 0 ; i < data.size() ; i++){

                    //내 채팅내용
                    MessageContent messageContent = new MessageContent(data.get(i).getMode(), data.get(i).getImg_path(), data.get(i).getSenderEmail(), data.get(i).getContent(), data.get(i).getChatDate(), data.get(i).getChatTime(), data.get(i).getCount());
                    mArrayList.add(messageContent);
                    mAdapter.notifyDataSetChanged();

                    mRecyclerView.scrollToPosition(data.size() - 1);
                }


            }
            @Override
            public void onFailure(Call<MessageContentResponse> call, Throwable t) {

                Log.d("Error!!!",t.getMessage());
            }
        });
    }

    // 메세지 전송하고 서버로 부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
    @SuppressLint("HandlerLeak")
    Handler msgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1111){
                Log.d(TAG, "handleMessage: 1111");
                Log.d(TAG, "handleMessage: message: " + msg.obj.toString());

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(msg.obj.toString());

                    String mode = jsonObject.getString("mode");
                    int modeToInt = Integer.parseInt(mode);

                    // 읽음 표시 지워주기 위한 메세지
                    if(modeToInt == 3){
                        Log.d(TAG, "handleMessage: modeToInt1: " + modeToInt);

                        for(int i = 0 ; i < mAdapter.getItemCount(); i++){

                            if(mArrayList.get(i).getCount() != 0){

                                mArrayList.get(i).setCount(0);
                                Log.d(TAG, "handleMessage: item: " + mArrayList.get(i).getContent());
                            }
                        }

                        mAdapter.notifyDataSetChanged();


                    // 그 외 텍스트, 이미지 메세지
                    }else{
                        Log.d(TAG, "handleMessage: modeToInt2: " + modeToInt);

                        String roomNo = jsonObject.getString("roomNo");
                        String myEmail = jsonObject.getString("myEmail");
                        String myProfile = jsonObject.getString("myProfile");
                        String message = jsonObject.getString("message");
                        String date = jsonObject.getString("date");
                        String time = jsonObject.getString("time");

                        int roomNoToInt = Integer.parseInt(roomNo);

                        //내 채팅내용 count = 메세지 읽음 표시
                        MessageContent messageContent = new MessageContent(modeToInt, myProfile, myEmail, message, date, time, 1);
                        mArrayList.add(messageContent);
                        mAdapter.notifyDataSetChanged();

                        //맨 아래 아이템으로 스크롤
                        mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);
                    }


                    // 키보드 내려주기
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(btnChatSend.getWindowToken(),0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // 메세지 보낼 때
            }else if(msg.what == 2222){
                Log.d(TAG, "handleMessage: 2222");
                Log.d(TAG, "handleMessage: jo.tostring(): " + msg.obj);

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;

                try {

                    jsonObject = new JSONObject(msg.obj.toString());

                    int mode = jsonObject.getInt("mode");
                    String roomNo = jsonObject.getString("roomNo");
                    String myEmail = jsonObject.getString("myEmail");
                    String myProfile = jsonObject.getString("myProfile");
                    String yourEmail = jsonObject.getString("yourEmail");
                    String message = jsonObject.getString("message");
                    String date = jsonObject.getString("date");
                    String time = jsonObject.getString("time");

                    int roomNoToInt = Integer.parseInt(roomNo);


                    Log.d(TAG, "handleMessage: mode: " + mode);
                    Log.d(TAG, "handleMessage: roomNo: " + roomNo);
                    Log.d(TAG, "handleMessage: message: " + message);

                    //내 채팅내용
                    MessageContent messageContent = new MessageContent(mode, myProfile, myEmail, message, date, time, 1);
                    mArrayList.add(messageContent);

                    mAdapter.notifyDataSetChanged();

                    // 에디트 텍스트 비워주기
                    etChatMessage.setText(null);

                    //맨 아래 아이템으로 스크롤
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);

                    // 키보드 내려주기
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(btnChatSend.getWindowToken(),0);


                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        }
    };

    // 채팅 입력 이벤트
    public void setSendBtn(final int roomNo, final String myEmail, final String yourEmail) {
        btnChatSend = (Button) findViewById(R.id.btnChatContentSend);

        btnChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: etChatMessage: " + etChatMessage.getText().toString());

                String message = etChatMessage.getText().toString();

                if (message == null || TextUtils.isEmpty(message) || message.equals("") || message.trim().matches("")) {
                    Toast.makeText(ChatRoomActivity.this, "메세지를 입력해주세요", Toast.LENGTH_SHORT).show();

                } else {

                    // 현재 시간 받아오기
                    long mNow;
                    Date mDate;
                    mNow = System.currentTimeMillis();
                    mDate = new Date(mNow);

                    // 메세지 전송시 날짜, 시간 생성
                    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREAN);
                    String date = mFormat.format(mDate);
                    SimpleDateFormat mFormat2 = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);
                    String time = mFormat2.format(mDate);
                    Log.d(TAG, "run: date: " + date);
                    Log.d(TAG, "run: time: " + time);

                    Log.d(TAG, "onClick: roomNo: " + roomNo);
                    Log.d(TAG, "onClick: myEmail: " + myEmail);
                    Log.d(TAG, "onClick: yourEmail: " + yourEmail);
                    sendMessageToMySQL(1, roomNo, myEmail, yourEmail, message, date, time);
                }
            }
        });
    }


    public void sendMessageToMySQL(final int mode, final int roomNo, final String myEmail, final String yourEmail, final String message, final String date, final String time){
        Log.d(TAG, "sendMessageToMySQL: mode: " + mode);

        Api api = ApiClient.getClient().create(Api.class);

        Call<Room> call = api.sendMessage(mode, roomNo, myEmail, yourEmail, message, date, time);
        call.enqueue(new Callback<Room>() {

            @Override
            public void onResponse(Call<Room> call, final Response<Room> response) {
                Log.d("TAG", "onResponse: " + response.body().getResult());

                if(mode == 1){
                    // 자바 서버로 메세지 보내주기 위한 쓰레드 생성
                    send = new SendThread(socket, mode, roomNo, myEmail, yourEmail, message, date, time); // ChatService에서 생성한 클라이언트 소켓 변수
                    send.start();
                }else if(mode == 2){
                    // 자바 서버로 메세지 보내주기 위한 쓰레드 생성
                    send = new SendThread(socket, mode, roomNo, myEmail, yourEmail, response.body().getMessage(), date, time); // ChatService에서 생성한 클라이언트 소켓 변수
                    send.start();

                    // 영상 통화 시작 메세지
                }else if(mode == 4){
                    send = new SendThread(socket, mode, roomNo, myEmail, yourEmail, message, date, time); // ChatService에서 생성한 클라이언트 소켓 변수
                    send.start();
                }


            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Log.d("ToMySQL Error",t.getMessage());
            }
        });
    }

    private void uploadImage(Bitmap bitmap){
        Log.d(TAG, "uploadImage() start");

        // bitmap -> String 변환
        String image = convertToString(bitmap);

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

        Api apiInterface = ApiClient.getClient().create(Api.class);
        // 메세지 타입, 방 번호, 이미지, 보내는 사람, 받을 사람, 날짜, 시간
        Call<ResponseResult> call = apiInterface.sendChatImage(2, currentRoomNo, image, loggedUseremail, yourEmail, date, time);

        call.enqueue(new Callback<ResponseResult>() {
            @Override
            public void onResponse(Call<ResponseResult> call, Response<ResponseResult> response) {
                Log.d(TAG, "onResponse: success!");

                //서버에 업로드 된 이미지 경로 받아오기
                Log.d("서버로 부터 받아온 이미지 경로: ",""+response.body().getResponse());

                // 자바 서버로 메세지 보내주기 위한 쓰레드 생성
                send = new SendThread(socket, 2, currentRoomNo, loggedUseremail, yourEmail, response.body().getResponse(), date, time); // ChatService에서 생성한 클라이언트 소켓 변수
                send.start();
            }

            @Override
            public void onFailure(Call<ResponseResult> call, Throwable t) {
                Log.d("Server Response fail!: ",""+t.toString());

            }
        });
    }

    @Override
    public void onImageClicked(int position) {
        Log.d(TAG, "onImageClicked ");

        // 클릭 한 아이템의 이미지 상세이미지 액티비티로 넘겨주기
        String img = mArrayList.get(position).getContent();
        Intent intent = new Intent(ChatRoomActivity.this, ChatImageDetailActivity.class);
        intent.putExtra("image", img);
        startActivity(intent);
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
                        jo.put("myProfile", profileImage);
                        jo.put("yourEmail", yourEmail);
                        jo.put("message", message);
                        jo.put("date", date);
                        jo.put("time", time);
                        if(mode == 4){
                            uniqueID = UUID.randomUUID().toString();
                            jo.put("callID", uniqueID);
                        }

                        dos.writeUTF(jo.toString());

                        if(mode == 4){

                            // 영상 통화 시작하기
                            webrtcstart();
                        }

                        /* 핸들러 */
                        // 핸들러에게 전달할 메세지 객체
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

    public void ChatdoSomething(){
        Log.d(TAG, "ChatdoSomething: callback");

        // 서비스로 부터 받은 메세지 내용
        String msg = myService.getMsg();
        Log.d(TAG, "ChatdoSomething: msg: " + msg);


        try {
            JSONObject jsonObject = new JSONObject(msg);
            String mode = jsonObject.getString("mode");
            int modeToInt = Integer.parseInt(mode);

            if(modeToInt == 3){
                // 핸들러에게 전달할 메세지 객체
                Message hdmg = msgHandler.obtainMessage();

                // 핸들러에게 전달할 메세지의 식별자
                hdmg.what = 1111;

                // 메세지의 본문
                hdmg.obj = msg;

                // 핸들러에게 메세지 전달 ( 화면 처리 )
                msgHandler.sendMessage(hdmg);


            }else{

                String myEmail = jsonObject.getString("myEmail");
                String roomNo = jsonObject.getString("roomNo");

                // 핸들러에게 전달할 메세지 객체
                Message hdmg = msgHandler.obtainMessage();

                // 핸들러에게 전달할 메세지의 식별자
                hdmg.what = 1111;

                // 메세지의 본문
                hdmg.obj = msg;

                // 핸들러에게 메세지 전달 ( 화면 처리 )
                msgHandler.sendMessage(hdmg);

                // 자바 서버로 메세지 보내주기 위한 쓰레드 생성
                response = new ResponseThread(socket, 3, myEmail, roomNo); // ChatService에서 생성한 클라이언트 소켓 변수
                response.start();
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    // 내부 클래스  ( 메세지 응답용 )
    class ResponseThread extends Thread{
        Socket socket;
        int mode;
        String message;
        String yourEmail;
        String roomNo;


        public ResponseThread(Socket socket, int mode, String yourEmail, String roomNo){
            Log.d(TAG, "ResponseThread: ");

            this.mode = mode;
            this.socket = socket;
            this.yourEmail = yourEmail;
            this.roomNo = roomNo;
        }

        // 서버로 메세지 전송
        public  void run(){
            try {
                //if (message != null){
                    Log.d(TAG, "ResponseThread run: ");

                    JSONObject jo = new JSONObject();
                    Log.d(TAG, "ResponseThread run: mode: " + mode);
                    jo.put("mode", mode);
                    jo.put("yourEmail", yourEmail);
                    jo.put("roomNo", roomNo);
                    jo.put("message", "this is response message");

                    dos.writeUTF(jo.toString());

                // 상대방 메세지 읽음 카운트 DB 0으로 변경
                checkMessageCount();
                //}

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    // 처음 채팅 방 입장 시 방이 존재하는지 확인 후 채팅 메세지를 불러오거나 아무것도 하지 않는다.
    public void CheckRoomNoIsExistOrNot(int roomNo){
        Log.d(TAG, "CheckRoomNoIsExistOrNot: roomNo: " + roomNo);

        Api api = ApiClient.getClient().create(Api.class);
        Call<Room> call = api.CheckRoomNoExistOrNot(roomNo);
        call.enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, final Response<Room> response) {
                Log.d("TAG", "onResponse: roomNo" + response.body().getResult());

                // 방번호 조회후 방이 있으면 채팅 목록을 불러온다.
                if(response.body().getResult() == 1){
                    getMessageList(roomNo);
                }


            }
            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Log.d("CheckRoomNoIs Error",t.getMessage());
            }
        });
    }



    // Activity에서 Bind
    // 2-1 Connection 클래스 객체 생성 및 구현
    // Service에서 bind한 것에 대한 결과로, 연결이 되거나, 끊어지기도 하는데 그 상태에 대한 Callback들이다.
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {

            // 서비스와 연결되었을 때 호출되는 메서드
            // cast the IBinder and get MyService instance
            ChatService.LocalBinder binder = (ChatService.LocalBinder) service;
            // getService 메소드를 사용해서 service객체를 사용한다.
            myService = binder.getService();
            myService.setCallbacks(ChatRoomActivity.this); // register

            isService = true; // 실행 여부를 판단
        }
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊기거나 종료되었을 때
            isService = false;
        }
    };



    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

        // 2-2 onStart()에서 bind 하기
        // mConnection객체를 가지고, bindService()메소드를 통해서 Service에 bind한다.
        // 서비스 시작하기
        // 읽음 표시 지워주기 위한 메세지.



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");


        // 상대방 메세지 읽음 카운트 DB 0으로 변경
        //checkMessageCount();

        // 기존 채팅방이 존재하는지 확인하고 존재하면 데이터베이스에 저장되어 있는 대화내용을 불러온다.
        // 화면을 홈버튼으로 나갔다가 들어올 때는 onCreate가 실행되지 않기 때문에 채팅목록을 불러올 때는 onResume()에서 불러온다.
        currentRoomNo = roomNo; // 화면이 보이지 않을 때 변경했던 방 번호를 화면이 나타날 때 다시 기존의 방 번호로 지정해 준다.
        Log.d(TAG, "onResume: currentRoomNo: " + currentRoomNo);
        CheckRoomNoIsExistOrNot(roomNo);

        Log.d(TAG, "onResume: isService: " + isService);
        // bind to Service
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        String roomNoToString = Integer.toString(roomNo);

        response = new ResponseThread(socket, 3, yourEmail, roomNoToString); // ChatService에서 생성한 클라이언트 소켓 변수
        response.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");

        currentRoomNo = -1; // 화면이 보이지 않을 떄는 방 번호를 -1로 지정해 준다

        // Unbind from service
        if (isService) {
            Log.d(TAG, "onPause(): unbind");
            currentRoomNo = -1;
            myService.setCallbacks(null); // unregister
            unbindService(conn);
            //isService = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

     //뒤로가기 버튼 클릭 시 액티비티 재실행
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called");

        if(backbuttonflag == 1){
            Log.d(TAG, "onBackPressed: 1");

            this.finish();
            startActivity(new Intent(this, ChatActivity.class));
        }else{
            this.finish();
        }

    }

    public void webrtcstart(){
        Log.d(TAG, "webrtcstart: ");

        // Get setting keys.
        // xml 환경설정 파일의 기본값을 설정한다.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // SharedPreferences에 저장된 값에 접근 헌더,
        // 지정된 컨텍스트에서 기본 설정 프레임 워크가 사용하는 기본 파일을 가리키는 SharedPreferences 인스턴스를 가져옵니다.
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // String.xml에 설정한 문자열 값을 아래 변수들에 넣어 준다.
        // resolution_preference
        keyprefResolution = getString(R.string.pref_resolution_key);
        // fps_preference
        keyprefFps = getString(R.string.pref_fps_key);
        // maxvideobitrate_preference
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        // maxvideobitratevalue_preference
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        // startaudiobitrate_preference
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        // startaudiobitratevalue_preference
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        // room_server_url_preference
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);

        // 퍼미션 요청 메소드
        requestPermissions();
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            Log.d(TAG, "onRequestPermissionsResult: ");
            String[] missingPermissions = getMissingPermissions();
            Log.d(TAG, "onRequestPermissionsResult: missingPermissions: " + missingPermissions);

              if (missingPermissions.length != 0) {
                  Log.d(TAG, "onRequestPermissionsResult: missingPermissions.length: " + missingPermissions.length);
                // User didn't grant all the permissions. Warn that the application might not work
                // correctly.
                new AlertDialog.Builder(this)
                    .setMessage(R.string.missing_permissions_try_again)
                    .setPositiveButton(R.string.yes,
                        (dialog, id) -> {
                          // User wants to try giving the permissions again.
                          dialog.cancel();
                          requestPermissions();
                        })
                    .setNegativeButton(R.string.no,
                        (dialog, id) -> {
                          // User doesn't want to give the permissions.
                          dialog.cancel();
                          onPermissionsGranted();
                        })
                    .show();
              } else {
                  Log.d(TAG, "onRequestPermissionsResult: missingPermissions.length: " + missingPermissions.length);
                    // All permissions granted.
                    onPermissionsGranted();
              }
        }
    }

    // 퍼미션이 승인됐을 경우 실행되는 메소드
    private void onPermissionsGranted() {
        Log.d(TAG, "onPermissionsGranted: ");

        // 영통 자동 연결
        Log.d(TAG, "onPermissionsGranted: uniqueID: " + uniqueID);
        connectToRoom(uniqueID,false, 0);

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        Log.d(TAG, "requestPermissions: ");

        // SDK
        // SDK 버전이 마시멜로보다 낮으면 퍼미션 요청을 하지 않고 넘어간다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "requestPermissions: Build.VERSION.SDK_INT < Build.VERSION_CODES.M");
            // 기기 SDK 버전
            Log.d(TAG, "requestPermissions: Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
            // 마시멜로 버전 코드
            Log.d(TAG, "requestPermissions: Build.VERSION_CODES.M: " + Build.VERSION_CODES.M);
            // 마시멜로 이전 버전은 권한을 사용자에게 요청하지 않는다.
            onPermissionsGranted();
            return;
        }

        // 퍼미션 누락을 확인해서 누락된 퍼미션을 missingPermissions 배열에 담아 준다.
        String[] missingPermissions = getMissingPermissions();
        // 누락된 퍼미션이 있을 경우
        if (missingPermissions.length != 0) {
            Log.d(TAG, "requestPermissions: missingPermissions.length != 0");
            Log.d(TAG, "requestPermissions: missingPermissions.length: " + missingPermissions.length);

            // 앱에 권한을 요청한다.
            requestPermissions(missingPermissions, PERMISSION_REQUEST);

        //  누락된 퍼미션이 없을 경우
        } else {
            Log.d(TAG, "requestPermissions: else");
            onPermissionsGranted();
        }
    }


    // requestPermissions에서 퍼미션 요청 시 누락 된 퍼미션을 찾아서 배열에 저장한다.
    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {

        // SDK 버전이 마시멜로보다 낮으면 조건 실행
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "getMissingPermissions: Build.VERSION.SDK_INT < Build.VERSION_CODES.M");
            Log.d(TAG, "getMissingPermissions: 현재 기기에서 실행되고 있는 SDK 버전: " + Build.VERSION.SDK_INT);
            Log.d(TAG, "getMissingPermissions: 마시멜로우 버전: " + Build.VERSION_CODES.M);
            return new String[0];
        }

        PackageInfo info; // 패키지의 내용에 대한 전체 정보.AndroidManifest.xml에서 수집 한 모든 정보를 담은 클래스
        try {

            //시스템에 설치된 응용 프로그램 패키지에 대한 전체 정보를 검색한다..
            //PackageManager.GET_PERMISSIONS : permissions에서 패키지의 사용 권한에 대한 정보를 반환한다.
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            Log.d(TAG, "getMissingPermissions: info getPackageName: " + getPackageName());
            Log.d(TAG, "getMissingPermissions: PackageManager.GET_PERMISSIONS: " + PackageManager.GET_PERMISSIONS);


        // 퍼미션 검색 실패시
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to retrieve permissions.");
            return new String[0];
        }

        // 요청 된 퍼미션이 없음
        if (info.requestedPermissions == null) {
            Log.w(TAG, "No requested permissions.");
            return new String[0];
        }


        // 누락 된 퍼미션을 담는 배열을 선언한다.
        ArrayList<String> missingPermissions = new ArrayList<>();
        // 요청 된 모든 퍼미션의 크기만큼 반복해서 퍼미션을 거절할 경우 missingPermissions 배열에 거절한 퍼미션을 추가해 준다
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {

                Log.d(TAG, "getMissingPermissions: " + i + " : " + info.requestedPermissions[i]);
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }

        // 누락 된 퍼미션
        Log.d(TAG, "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    // SharedPreference 혹은 intent로 부터 boolean 값을 받아 온다.
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.parseBoolean(getString(defaultId));

        // useFromIntent가 true일 경우
        if (useFromIntent) {
            // intent의 value를 가져 온다.
            return getIntent().getBooleanExtra(intentName, defaultValue);

        // useFromIntent가 false일 경우
        } else {

            //
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    @SuppressWarnings("StringSplitter")
    private void connectToRoom(String roomId, boolean useValuesFromIntent, int runTimeMs) {
        Log.d(TAG, "connectToRoom start: ");
        ChatRoomActivity.commandLineRun = commandLineRun;

        //roomId 방번호
        //useValuseFromIntent 영상통화 옵션 체크 여부
        //

        // 영상통화 url 주소를 SharedPreferences에서 가져온다.
        String roomUrl = sharedPref.getString(
                keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));

        // Video call enabled flag.
        // 영상 통화 화면 출력 여부
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                CallActivity.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // 화면 캡쳐 여부
        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                CallActivity.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // 두번째 카메라 사용 여부
        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, CallActivity.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // 비디오, 오디오 기본 코덱
        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                CallActivity.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                CallActivity.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        //  하드웨어 코덱 사용 여부
        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                CallActivity.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);


        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        //FlexFEC. on off
        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                CallActivity.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        // aec dump
        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                CallActivity.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        // 오디오를 파일로 저장한다.
        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        CallActivity.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                CallActivity.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);


        // Get video resolution from settings.
        // 설정한 해상도 값을 가져온다.
        int videoWidth = 0; // 화면 가로
        int videoHeight = 0; // 화면 세로

        // 체크되어 있는 해상도 값을 가져와서 width, height에 각각 저장해 준다.
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 0);
        }

        // 가로, 세로 값이 둘다 0이면 기본 해상도로 설정한다.
        if (videoWidth == 0 && videoHeight == 0) {

            // default 문자를 shared에서 가져온다.
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));

            // (000x000)문자열을 잘라서 width, height 값을 구분해 준다.
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {

                // String으로 되어있는 숫자를 int로 변환시켜서 width->0번 인덱스, heigth->1번 인덱스에 넣어 준다.
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);

                    // 해상도 설정이 잘못됐을 경우
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // 설정에서 카메라 FPS 값을 받아온다.
        int cameraFps = 0;
        Log.d(TAG, "connectToRoom: useValuesFromIntent: " + useValuesFromIntent);

        // useValuesFromIntent이 true면 설정한 cameraFps를 받아 온다.
        if (useValuesFromIntent) {

            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
            Log.d(TAG, "connectToRoom: cameraFps: " + cameraFps);
        }


        // camera fps 설정
        if (cameraFps == 0) {

            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            Log.d(TAG, "connectToRoom: fps: " + fps);
            String[] fpsValues = fps.split("[ x]+"); // FPS 설정 값
            Log.d(TAG, "connectToRoom: fpsValues.length: " + fpsValues.length);
            Log.d(TAG, "connectToRoom: fpsValues[0]: " + fpsValues[0]);

            // fpsValues가 Default가 아닌 15 or 30 fps인 경우
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                    Log.d(TAG, "connectToRoom: cameraFps: " + cameraFps);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        // 화면 캡쳐 퀄리티 설정
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        // 비디오 비트레이트 설정
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
            Log.d(TAG, "connectToRoom: videoStartBitrate: " + videoStartBitrate);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            Log.d(TAG, "connectToRoom: birateType: " + bitrateType);

            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                Log.d(TAG, "connectToRoom: bitrateValue: " + bitrateValue);
                videoStartBitrate = Integer.parseInt(bitrateValue);
                Log.d(TAG, "connectToRoom: videoStartBitrate: " + videoStartBitrate);
            }
        }

        // 오디오 비트레이트 설정
        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_AUDIO_BITRATE, 0);
            Log.d(TAG, "connectToRoom: audioStartBitrate: " + audioStartBitrate);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            Log.d(TAG, "connectToRoom: bitrateType: " + bitrateType);

            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
                Log.d(TAG, "connectToRoom: audioStartBitrate: " + audioStartBitrate);
            }
        }


        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, CallActivity.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                CallActivity.EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default,
                useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                CallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, CallActivity.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                CallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                CallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, CallActivity.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, CallActivity.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                CallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start CallActivity.
        Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);

        // roomUrl이 http or https주소가 맞으면
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Log.d(TAG, "connectToRoom: uri: " + uri);
            Intent intent = new Intent(this, CallActivity.class);
            intent.setData(uri);

            // CallActivity에 선언 한 변수를 인텐트 명으로 지정한다.
            // 방 번호
            intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);

            // 설정창 내용
            // 영상 통화 화면
            intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
            // 화면 캡쳐
            intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
            // 카메라 2
            intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
            // 카메라 가로
            intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
            // 카메라 세로
            intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
            // 카메라 FPS
            intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
            // 캡쳐 퀄리티 슬라이더 표시
            intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            // 비디오 비트레이트
            intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            // 비디오 코덱
            intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
            // 하드웨어 코덱
            intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(CallActivity.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            // 오디오 작동 중지
            intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            // 오디오 녹음
            intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
            // 오디오 녹음 파일로 저장
            intent.putExtra(CallActivity.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
            // opensl es 를 이용해 오디오를 플레이
            intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);

            // 음성 옵션
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);

            //오디오 비트레이트
            intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            // 오디오 코덱
            intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
            intent.putExtra(CallActivity.EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
            intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);
            intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(CallActivity.EXTRA_ORDERED, ordered);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(CallActivity.EXTRA_PROTOCOL, protocol);
                intent.putExtra(CallActivity.EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(CallActivity.EXTRA_ID, id);
            }

            if (useValuesFromIntent) {
                if (getIntent().hasExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                    String videoFileAsCamera =
                            getIntent().getStringExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA);
                    intent.putExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                    String saveRemoteVideoToFile =
                            getIntent().getStringExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                    int videoOutWidth =
                            getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                    int videoOutHeight =
                            getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
                }
            }

            // 방 번호와 발신자 이메일을 함께 보내준다.
            Log.d(TAG, "connectToRoom: currentRoomNo: " + roomNo);
            intent.putExtra("roomNo", roomNo); // 채팅 방 번호
            intent.putExtra("senderEmail", loggedUseremail); // 영상 통화 발신자
            intent.putExtra("yourEmail", yourEmail); // 영상 통화 수신자

            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }

    private boolean validateUrl(String url) {
        Log.d(TAG, "validateUrl: url: " + url);
        Log.d(TAG, "validateUrl: URLUtil.isHttpsUrl(url): " + URLUtil.isHttpsUrl(url));
        Log.d(TAG, "validateUrl: URLUtil.isHttpUrl(url): " + URLUtil.isHttpUrl(url));

        // url https이거나 http이면 true
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        //https url or http url 둘다 true가 아닐 경우 alert 알림을 띄우고 false를 되돌려 준다.
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }



}
