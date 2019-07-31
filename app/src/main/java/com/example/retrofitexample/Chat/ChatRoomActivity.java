package com.example.retrofitexample.Chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private boolean isService = false; // 서비스 실행 확인

    Toolbar myToolbar; // 상단 툴바

    String yourEmail; // intent로 받아 온 상대방 이메일

    int backbuttonflag; // 뒤로가기 버튼을 눌렀을 때 채팅 리스트 액티비티를 재실행해서 메세지를 갱신하기 위해

    List<Uri> uriList; // 이미지 Uri 리스트

    static int roomNo; // 방을 왔다갔다 할 때 방 번호를 저장해두기 위해 선언

    ArrayList<MessageContent> data;



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

                    sendMessageToMySQL(2, roomNo, loggedUseremail, yourEmail, image, date, time);

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

                Log.d("Error!",t.getMessage());
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

                        //내 채팅내용
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

        Api api = ApiClient.getClient().create(Api.class);
        Call<Room> call = api.CheckRoomNoExistOrNot(roomNo);
        call.enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, final Response<Room> response) {
                Log.d("TAG", "onResponse: " + response.body().getResult());

                // 방번호 조회후 방이 있으면 채팅 목록을 불러온다.
                if(response.body().getResult() == 1){
                    getMessageList(currentRoomNo);
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


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

        String roomNoToString = Integer.toString(roomNo);
        // 자바 서버로 메세지 보내주기 위한 쓰레드 생성

        response = new ResponseThread(socket, 3, yourEmail, roomNoToString); // ChatService에서 생성한 클라이언트 소켓 변수
        response.start();

        // 상대방 메세지 읽음 카운트 DB 0으로 변경
        //checkMessageCount();

        // 기존 채팅방이 존재하는지 확인하고 존재하면 데이터베이스에 저장되어 있는 대화내용을 불러온다.
        // 화면을 홈버튼으로 나갔다가 들어올 때는 onCreate가 실행되지 않기 때문에 채팅목록을 불러올 때는 onResume()에서 불러온다.
        currentRoomNo = roomNo; // 화면이 보이지 않을 때 변경했던 방 번호를 화면이 나타날 때 다시 기존의 방 번호로 지정해 준다.
        Log.d(TAG, "onResume: currentRoomNo: " + currentRoomNo);
        CheckRoomNoIsExistOrNot(roomNo);

        // bind to Service
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");

        currentRoomNo = -1; // 화면이 보이지 않을 떄는 방 번호를 -1로 지정해 준다
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
        finish();
    }

    // 뒤로가기 버튼 클릭 시 액티비티 재실행
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called");

        // Unbind from service
        if (isService) {
            Log.d(TAG, "onBackPressed(): unbind");
            currentRoomNo = -1;
            myService.setCallbacks(null); // unregister
            unbindService(conn);
            //isService = false;
        }

        if(backbuttonflag == 1){
            Log.d(TAG, "onBackPressed: 1");

            this.finish();
            startActivity(new Intent(this, ChatActivity.class));
        }else{
            this.finish();
        }

    }


}
