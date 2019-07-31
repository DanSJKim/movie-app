package com.example.retrofitexample.Board;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;

import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardLikeListActivity extends Activity {
    public static final String TAG = "BoardLikeList : ";

    int board_id;

    private RecyclerView recyclerView;
    private ArrayList<BoardItem> data;
    private LikeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_board_like_list);

        Intent intent = getIntent();
        board_id = intent.getIntExtra("board_id", -1);

        initViews();
    }
    //리사이클러뷰 초기화
    private void initViews(){

        recyclerView = (RecyclerView)findViewById(R.id.like_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        //게시물 목록 업로드
        likeList();
    }

    //게시물 목록 출력
    private void likeList(){

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardResponse> call = api.getLikeList(board_id);
        Log.d(TAG, "likeList: board_id!" + board_id);

        call.enqueue(new Callback<BoardResponse>() {

            @Override
            public void onResponse(Call<BoardResponse> call, Response<BoardResponse> response) {

                Log.d(TAG, "onResponse!: " + response.body().getBoardItems());
                BoardResponse likeResponse = response.body();
                if(likeResponse.getBoardItems() != null) {
                    data = new ArrayList<>(Arrays.asList(likeResponse.getBoardItems())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
                    adapter = new LikeListAdapter(data);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<BoardResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
