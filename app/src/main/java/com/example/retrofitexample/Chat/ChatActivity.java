package com.example.retrofitexample.Chat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.retrofitexample.Board.BoardActivity;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.Chat.Model.DatabaseHelper;
import com.example.retrofitexample.Chat.Model.MessageContent;
import com.example.retrofitexample.Chat.Model.MessageListAdapter;
import com.example.retrofitexample.Chat.Model.MessageListContent;
import com.example.retrofitexample.Chat.Model.MessageListResponse;
import com.example.retrofitexample.MovieSearch.SearchResponse;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import static com.example.retrofitexample.BootReceiver.isService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;
import static com.example.retrofitexample.Chat.ChatService.currentRoomNo;

/**
 * 채팅 목록을 표시해주는 액티비티
 */

public class ChatActivity extends AppCompatActivity implements ChatService.ServiceCallbacks, MessageListAdapter.MessageListRecyclerViewClickListener {
    public static final String TAG = "ChatActivity : ";

    ImageView Home, Board;
    private RecyclerView mRecyclerView;
    private MessageListAdapter mAdapter;
    private static ArrayList<MessageListContent> mChatArrayList;

    private ChatService myService; // 서비스
    //private boolean isService = false; // 서비스 실행 확인

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Home = (ImageView) findViewById(R.id.ivHome);
        Board = (ImageView) findViewById(R.id.ivBoard);

        //db = new DatabaseHelper(getApplicationContext());
        currentRoomNo = 0;
        Log.d(TAG, "run: currentRoomNo: " + currentRoomNo);

        initViews();

        /*
        init Toolbar
         */
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        /*
        Menu Tabs
         */
        Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        Board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, BoardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        getChatList();

        //db.onUpgrade(db, 0,1);

        // Reading all contacts
//        Log.d("Reading: ", "Reading all contacts..");
//        List<MessageListContent> messageListContents = db.getAllMessageListContents();
//
//        for (MessageListContent mlc : messageListContents) {
//            String log = "Id: " + mlc.getId() + ", profile: " + mlc.getImg_path() + " ,title: " + mlc.getMyUser() + " ,content: " +
//                    mlc.getContent() + ", date: " + mlc.getChatTime();
//            // Writing Contacts to log
//            Log.d("Name: ", log);
//
//            //id profile title content date
//            MessageListContent messageListContent = new MessageListContent(mlc.getRoomNo(), mlc.getImg_path(), mlc.getMyUser(), mlc.getYourUser(), mlc.getContent(), mlc.getContent());
//
//            mChatArrayList.add(messageListContent);
//            mAdapter.notifyDataSetChanged();
//        }

    }

    public void countNewMessages(final ArrayList<MessageListContent> data, String myEmail){


        for(int i = 0 ; i < data.size() ; i++) {
            Log.d(TAG, "countNewMessages: data.get(i).getRoomNo(): " + data.get(i).getRoomNo());
            Log.d(TAG, "countNewMessages: myEmail: " + myEmail);

            Api api = ApiClient.getClient().create(Api.class);
            Call<MessageContent> call = api.countNewMessage(myEmail, data.get(i).getRoomNo());

            final int finalI = i;
            call.enqueue(new Callback<MessageContent>() {
                @Override
                public void onResponse(Call<MessageContent> call, final Response<MessageContent> response) {
                    Log.d(TAG, "checkMessage onResponse count: " + response.body().getCount());

                    int count = response.body().getCount();

                    Log.d(TAG, "onResponse: data.get(finalI).getRoomNo()" + finalI + " : " +data.get(finalI).getRoomNo());
                    Log.d(TAG, "onResponse: data.get(finalI).getTitle()" + finalI + " : " +data.get(finalI).getTitle());

                    //내 채팅내용
                    MessageListContent messageListContent = new MessageListContent(data.get(finalI).getRoomNo(), data.get(finalI).getImg_path(), data.get(finalI).getTitle(), data.get(finalI).getContent(), data.get(finalI).getChatTime(), count);
                    mChatArrayList.add(messageListContent);
                    mAdapter.notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<MessageContent> call, Throwable t) {

                    Log.d("checkMessage Error!", t.getMessage());
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 채팅 목록 불러오기
    public void getChatList(){
        Api api = ApiClient.getClient().create(Api.class);
        Call<MessageListResponse> call = api.getChatRoomList(loggedUseremail);
        Log.d(TAG, "getChatList: loggedUseremail: " + loggedUseremail);
        call.enqueue(new Callback<MessageListResponse>() {
            @Override
            public void onResponse(Call<MessageListResponse> call, final Response<MessageListResponse> response) {
                Log.d(TAG, "chat room list onResponse: " + response.body().getChatroomlist());

                ArrayList<MessageListContent> data = new ArrayList<>(Arrays.asList(response.body().getChatroomlist())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
                Log.d(TAG, "onResponse: data size: " + data.size());
                Log.d(TAG, "onResponse: data.get(0).getTitle(): " + data.get(0).getTitle());

                // 채팅 방 번호를 가져온 후 안 읽은 메세지를 카운트해서 아이템에 추가한다.
                countNewMessages(data, loggedUseremail);

                //mRecyclerView.scrollToPosition(data.size() - 1);



            }
            @Override
            public void onFailure(Call<MessageListResponse> call, Throwable t) {
                Log.e("Error",t.getMessage());
            }
        });
    }

    // ( 메세지 수신용 )   -  서버로부터 받아서, 핸들러에서 처리하도록 할 거.
    class ReceiveThread extends Thread{

        Socket rcSocket = null;
        DataInputStream input = null;
        String rcMsg;

        public ReceiveThread(String msg){
            //this.rcSocket = socket;
            this.rcMsg = msg;

            try {
                // 채팅 서버로부터 메세지를 받기 위한 스트림 생성.
                //input = new DataInputStream(socket.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                    // 핸들러에게 전달할 메세지 객체
                    Message hdmg = msgHandler.obtainMessage();

                    // 핸들러에게 전달할 메세지의 식별자
                    hdmg.what = 1111;

                    // 메세지의 본문
                    hdmg.obj = rcMsg;


                    Log.d(TAG, "run: receivedMsg: " + rcMsg);

                    // 핸들러에게 메세지 전달 ( 화면 처리 )
                    msgHandler.sendMessage(hdmg);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // 서버로부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
    @SuppressLint("HandlerLeak")
    Handler msgHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1111){
                // 메세지가 왔다면.
                //Toast.makeText(ChatActivity.this, "메세지 : "+msg.obj.toString(), Toast.LENGTH_SHORT).show();
                Log.d("받은 메세지 ",msg.obj.toString());

                // JSON 형식의 데이터 추출
                JSONObject jsonObject= null;
                try {
                    jsonObject = new JSONObject(msg.obj.toString());

                    String roomNo = jsonObject.getString("roomNo");
                    String myEmail = jsonObject.getString("myEmail");
                    String myProfile = jsonObject.getString("myProfile");
                    String yourEmail = jsonObject.getString("yourEmail");
                    String message = jsonObject.getString("message");
                    String date = jsonObject.getString("date");
                    String time = jsonObject.getString("time");

                    int roomNoToInt = Integer.parseInt(roomNo);

                    Log.d(TAG, "handleMessage: chat list mChatArrayList.size(): " + mChatArrayList.size());

                    // 채팅 목록에 기존 방이 있는지 여부
                    int cnt = 0;
                    for(int i = 0 ; i < mChatArrayList.size() ; i++){

                        Log.d(TAG, "handleMessage: getroomNo: " + mChatArrayList.get(i).getRoomNo());
                        Log.d(TAG, "handleMessage: roomNo: " + roomNo);

                        // 서버에서 받은 방 번호와 채팅리스트의 방 번호와 번호를 비교해서 같으면 기존 방이 있는 것
                        if(mChatArrayList.get(i).getRoomNo() == roomNoToInt){

                            Log.d(TAG, "handleMessage: item updated");

                            //내 채팅내용
                            MessageListContent messageListContent = new MessageListContent(roomNoToInt, myProfile, myEmail, message, time, mChatArrayList.get(i).getCount()+1);
                            // 새로운 메세지가 오면 아이템의 포지션을 맨 위로 올려준다.

                            mChatArrayList.remove(i);
                            mChatArrayList.add(0, messageListContent);
                            mAdapter.notifyDataSetChanged();
                            //mAdapter.notifyItemMoved(i, 0);
                            cnt = 1;
                            break;
                        }
                    }

                    Log.d(TAG, "handleMessage: cnt: " + cnt);

                    // 기존 방이 없으면
                    if(cnt == 0) {
                        Log.d(TAG, "handleMessage: cnt == 0");
                        MessageListContent messageListContent = new MessageListContent(roomNoToInt, myProfile, myEmail, message, time, 1);
                        // 새로운 메세지가 오면 아이템의 포지션을 맨 위로 올려준다.
                        mChatArrayList.add(0, messageListContent);
                        mAdapter.notifyDataSetChanged();
                    }




                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //리사이클러뷰 초기화
    private void initViews(){
        mRecyclerView = (RecyclerView)findViewById(R.id.rvMessageList);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mChatArrayList = new ArrayList<>();

        mAdapter = new MessageListAdapter(mChatArrayList);
        mAdapter.setOnClickListener(ChatActivity.this);
        mRecyclerView.setAdapter(mAdapter);

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
            myService.setCallbacks(ChatActivity.this); // register

            isService = true; // 실행 여부를 판단
        }
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊기거나 종료되었을 때
            isService = false;
        }
    };

    /* Defined by ServiceCallbacks interface */
    @Override
    public void ChatdoSomething() {
        Log.d(TAG, "ChatdoSomething: callback");

        String msg = myService.getMsg();
        Log.d(TAG, "doSomething: myService.getMsg(): " + msg);

        // 핸들러에게 전달할 메세지 객체
        Message hdmg = msgHandler.obtainMessage();

        // 핸들러에게 전달할 메세지의 식별자
        hdmg.what = 1111;

        // 메세지의 본문
        hdmg.obj = msg;

        // 핸들러에게 메세지 전달 ( 화면 처리 )
        msgHandler.sendMessage(hdmg);
    }


    /**
     * 유저 검색
     */
    // 최초에 메뉴키가 눌렸을 때 호출
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem search = menu.findItem(R.id.svMovie);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }



    // onOptionsItemSelected() 메뉴 아이템이 클릭되었을때 호출
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");

        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
        
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: ");

                loadJSON(newText);
                return true;
            }
        });
    }
    /*
    서치뷰 끝
     */


    public void onChatListClicked(int position){
        Log.d(TAG, "onItemClick:");

        int roomNo = mChatArrayList.get(position).getRoomNo();
        Log.d(TAG, "onChatListClicked: roomNo: " + roomNo);
        String yourEmail = mChatArrayList.get(position).getTitle();
        Log.d(TAG, "onChatListClicked: 받을 사람 이메일: " + yourEmail);

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("roomNo", roomNo);
        intent.putExtra("yourEmail", yourEmail);
        intent.putExtra("backbuttonflag", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }


    private void loadJSON(String text){
        Api api = ApiClient.getClient().create(Api.class);
        Call<SearchResponse> call = api.searchUser(text);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, final Response<SearchResponse> response) {
                Log.d("TAG", "onResponse: " + response.body().getSearchlist());

            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }
    /**
     * 유저 검색 끝
     */


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

        currentRoomNo = 0;

        // bind to Service
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onResume: conn value: " + conn);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");

        // Unbind from service
        if (isService) {
            currentRoomNo = -1;
            Log.d(TAG, "onPause: isService:" + isService);

            Log.d(TAG, "onPause: unbind");
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
        finish();
    }



}
