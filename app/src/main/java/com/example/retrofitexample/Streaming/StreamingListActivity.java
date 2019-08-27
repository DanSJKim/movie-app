package com.example.retrofitexample.Streaming;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.Chat.ChatActivity;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.MovieSearch.DataAdapter;
import com.example.retrofitexample.MovieSearch.MovieSearchActivity;
import com.example.retrofitexample.MovieSearch.SearchResponse;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.Streaming.Model.StreamingListAdapter;
import com.example.retrofitexample.Streaming.Model.StreamingListContent;
import com.example.retrofitexample.Streaming.Model.StreamingListResponse;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;

/**
 * 방송 중인 방 목록을 나타내는 액티비티
 */

public class StreamingListActivity extends AppCompatActivity implements StreamingListAdapter.StreamingListContentRecyclerviewClickListener {
    private static final String TAG = "StreamingListActivity: ";
    Button btnCreate; // 방 생성 버튼
    ImageView ivHome; // 홈버튼

    private RecyclerView mRecyclerView;// 방 목록 나타내는 리사이클러뷰
    private ArrayList<StreamingListContent> mArrayList;// 각각의 방 아이템을 담는 리스트
    private StreamingListAdapter mAdapter; // 방 목록 어댑터

    SwipeRefreshLayout swipeContainer; // 새로고침 레이아웃

    public static int roomcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_list);

        btnCreate = (Button)findViewById(R.id.btnCreateStRoom); // 방 만들기 액티비티 로 넘어가는 버튼
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 방 생성 1: ");

                Intent intent = new Intent(StreamingListActivity.this, CreateStreamingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        ivHome = (ImageView) findViewById(R.id.ivHome);
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ProfileActivity로 이동");
                Intent intent = new Intent(StreamingListActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.streaming_swipe_layout);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.

                Log.d(TAG, "onRefresh: 스트리밍 방 목록 새로고침");

                //refresh
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);

                // 새로고침 완료
                swipeContainer.setRefreshing(false); // 리프래시가 완료되었을 때 새로고침 아이콘을 제거한다.
            }
        });

        streamingRoomList();
    }

    // 스트리밍 중인 방 리스트를 데이터베이스에서 가져 온다.
    private void streamingRoomList(){
        Api api = ApiClient.getClient().create(Api.class);
        Call<StreamingListResponse> call = api.getStreamingList();
        call.enqueue(new Callback<StreamingListResponse>() {
            @Override
            public void onResponse(Call<StreamingListResponse> call, final Response<StreamingListResponse> response) {
                Log.d(TAG, "onResponse: onCreate() -> 데이터베이스에서 방 목록을 불러 온다.");
                Log.d("TAG", "onResponse: " + response.body().getStreamingroomlist());

                    Log.d(TAG, "onResponse: is not null "+response.body().getStreamingroomlist());
                    StreamingListResponse searchResponse = response.body();

                    roomcount = response.body().getStreamingroomlist().length; // 방 개수
                Log.d(TAG, "onResponse: roomcount");

                // 리사이클러뷰 초기화
                    mRecyclerView = (RecyclerView)findViewById(R.id.rvStreamingList);
                    mRecyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(StreamingListActivity.this);
                    mRecyclerView.setLayoutManager(layoutManager);

                    // 데이터 베이스에서 방 목록을 불러 온다.
                    mArrayList = new ArrayList<>(Arrays.asList(searchResponse.getStreamingroomlist()));
                    mAdapter = new StreamingListAdapter(mArrayList);
                    mAdapter.setOnClickListener(StreamingListActivity.this);
                    mRecyclerView.setAdapter(mAdapter);

                    // 데이터 변경 알림
                    mAdapter.notifyDataSetChanged();
                }
            //}

            @Override
            public void onFailure(Call<StreamingListResponse> call, Throwable t) {
                Log.d("firstloadJSON Error",t.getMessage());
            }
        });
    }

    public void onItemClicked(int position){
        Log.d(TAG, "onItemClicked: 들어갈 방을 클릭함.");

        int roomNo = mArrayList.get(position).getRoomNo();
        Log.d(TAG, "onChatListClicked: 방 번호: " + roomNo);
        String roomHost = mArrayList.get(position).getRoomHost();
        Log.d(TAG, "onChatListClicked: 방장 이름: " + roomHost);
        String walletAddress = mArrayList.get(position).getWalletAddress();//회원 이름
        Log.d(TAG, "onItemClicked: 지갑 주소: " + walletAddress);

        Intent intent = new Intent(StreamingListActivity.this, PlayerActivity.class);
        intent.putExtra("roomNo", roomNo);
        intent.putExtra("pos", position);
        intent.putExtra("walletAddress", walletAddress);
//        if(mArrayList.size() == 1){
//            intent.putExtra("app", "app-fc5b");
//            intent.putExtra("streamname", "fa06bb8a");
//            intent.putExtra("username", "client44490");
//            intent.putExtra("password", "b12dbc74");
//        }else if(mArrayList.size() == 2){
//            intent.putExtra("app", "app-fee6");
//            intent.putExtra("streamname", "89fcd457");
//            intent.putExtra("username", "client44490");
//            intent.putExtra("password", "ca5ce168");
//        }

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
