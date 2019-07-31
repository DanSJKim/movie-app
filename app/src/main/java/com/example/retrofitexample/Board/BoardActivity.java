package com.example.retrofitexample.Board;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.retrofitexample.Board.Comment.BoardCommentActivity;
import com.example.retrofitexample.Board.Image.BoardImageItem;
import com.example.retrofitexample.Chat.ChatActivity;
import com.example.retrofitexample.Chat.UserPageActivity;
import com.example.retrofitexample.MovieSearch.MovieSearchActivity;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardActivity extends AppCompatActivity implements BoardItemAdapter.BoardItemRecyclerViewClickListener{

    public static final String TAG = "BoardActivity : ";

    FloatingActionButton fabUpload;

    private RecyclerView recyclerView;
    private ArrayList<BoardItem> data;
    private ArrayList<BoardImageItem> data2;
    private BoardItemAdapter adapter;
    SnapHelper snapHelper;

    ImageView ivHome, ivSearch, ivChat;
    Button btnHome;
    String loggedUseremail;
    int loggedUserid;

    int pos; // 수정 버튼 내의 포지션
    int itemLimit = 5;//서버의 Limit값을 지정해 준다.

    private SwipeRefreshLayout swipeContainer; //스와이프 새로고침

    //현재 화면에 보이는 포지션 값 저장
    int currentVisiblePosition = 0;

    //리사이클러뷰 총 아이템 개수
    int itemTotalCount = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        Log.d(TAG, "onCreate() called");

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.board_swipe_layout);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.

                //refresh
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);

                swipeContainer.setRefreshing(false);

            }
        });


        //ShardPreferences
        loggedUseremail = SharedPref.getInstance(this).LoggedInEmail();// 유저 이메일
        loggedUserid = SharedPref.getInstance(this).LoggedInId();// 유저 번호
        Log.d(TAG, "onCreate: loggeduseremail:" + loggedUseremail);
        Log.d(TAG, "onCreate: shared loggeduserid: " + loggedUserid);


        //뷰 초기화
        fabUpload = (FloatingActionButton) findViewById(R.id.fabUpload);
        ivHome = (ImageView) findViewById(R.id.ivHome);

        //홈 버튼 클릭 리스너
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(BoardActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        ivSearch = (ImageView) findViewById(R.id.ivSearch);
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(BoardActivity.this, MovieSearchActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        ivChat = (ImageView) findViewById(R.id.ivChat);
        ivChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(BoardActivity.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        //업로드 버튼 클릭 리스너
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, BoardUploadActivity.class);
                startActivity(intent);
            }
        });

        //boardList(loggedUseremail);

        initViews();
    }

    //리사이클러뷰 초기화
    private void initViews(){
        Log.d(TAG, "initViews: ");

        recyclerView = (RecyclerView)findViewById(R.id.board_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnFlingListener(null);//snaphelper에서 발생하는 onflinglistener already set 오류 방지용 코드


        //게시물 목록 업로드
        listBoards();
    }

    //게시물 목록 출력
    private void listBoards(){

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardResponse> call = api.getBoard(loggedUserid, itemLimit);
        Log.d(TAG, "listBoards: loggeduserid: " + loggedUserid);
        call.enqueue(new Callback<BoardResponse>() {

            @Override
            public void onResponse(Call<BoardResponse> call, final Response<BoardResponse> response) {
                Log.d(TAG, "onResponse: response.body().getBoardItems() " + response.body().getBoardItems());
                //리사이클러뷰 게시물 목록 불러오기

                data = new ArrayList<>(Arrays.asList(response.body().getBoardItems())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
                Log.d(TAG, "onResponse: data: " + data);


                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }
                    
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                        super.onScrolled(recyclerView, dx, dy);
                        
                        int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition()+1;
                        int itemTotalCount = recyclerView.getAdapter().getItemCount();
                        
                        if(lastVisibleItemPosition == itemTotalCount){
                            //리스트 마지막(바닥) 도착 다음 페이지 데이터 로드
                            Log.d(TAG, "onScrolled: last!!");


                            if(itemLimit <= response.body().getBoardItems().length){
                                Log.d(TAG, "onScrolled: itemLimit@: " + itemLimit);
                                itemLimit = itemLimit+5;
                                Log.d(TAG, "onScrolled: itemlength: " + response.body().getBoardItems().length);
                                onLoadMore();
                            }
                            
                        }
                    }
                });


                adapter = new BoardItemAdapter(recyclerView,data,BoardActivity.this);
                adapter.setOnClickListener(BoardActivity.this);
                recyclerView.setAdapter(adapter);

                //onPause에서 저장했던 scroll position으로 복귀
                Log.d(TAG, "listBoards: currentvisibleposition: " + currentVisiblePosition);
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(currentVisiblePosition-1);
                currentVisiblePosition = 0;
            }

            @Override
            public void onFailure(Call<BoardResponse> call, Throwable t) {
                Log.d(TAG, "listBoards onFailure: " + t.getMessage());

            }
        });
    }

    //게시물 페이징 출력
    private void pagingBoards(){

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardResponse> call = api.getBoard(loggedUserid, itemLimit);
        Log.d(TAG, "listBoards: loggeduserid: " + loggedUserid);

        call.enqueue(new Callback<BoardResponse>() {

            @Override
            public void onResponse(Call<BoardResponse> call, final Response<BoardResponse> response) {
                Log.d(TAG, "onResponse: response.body().getBoardItems() " + response.body().getBoardItems());
                //리사이클러뷰 게시물 목록 불러오기

                //progressDoalog.dismiss();

                data = new ArrayList<>(Arrays.asList(response.body().getBoardItems())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
                Log.d(TAG, "onResponse: data: " + data);

                //어댑터
                adapter = new BoardItemAdapter(recyclerView, data, BoardActivity.this);
                adapter.setOnClickListener(BoardActivity.this);
                recyclerView.setAdapter(adapter);

                //스크롤 이동
                Log.d(TAG, "onResponse: itemTotalCount: " + itemTotalCount);
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(itemTotalCount-1);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                        super.onScrolled(recyclerView, dx, dy);

                        int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition()+1;
                        itemTotalCount = recyclerView.getAdapter().getItemCount();

                        //마지막에 보인 아이템 포지션 값과 전체 아이템 개수 값이 같으면 바닥으로 판단
                        if(lastVisibleItemPosition == itemTotalCount){
                            //리스트 마지막(바닥) 도착 다음 페이지 데이터 로드
                            Log.d(TAG, "onScrolled: last!!");
                            Log.d(TAG, "onScrolled: itemTotalCount: " + itemTotalCount);
                            Log.d(TAG, "onScrolled: itemLimit@: " + itemLimit);//limit

                            //limit 설정한 값과 아이템 전체 개수가 같으면 실행
                            //메소드 여러번 겹쳐서 실행 되는 현상 방지
                            if(itemLimit <= itemTotalCount){
                                Log.d(TAG, "onScrolled: itemLimit@: " + itemLimit);
                                itemLimit = itemLimit+5;
                                Log.d(TAG, "onScrolled: itemlength: " + response.body().getBoardItems().length);
                                onLoadMore();
                            }

                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<BoardResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());

            }
        });
    }

    //프로그레스바 생성
    public void onLoadMore() {
            data.add(null);
            adapter.notifyItemInserted(data.size() - 1);
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(data.size()-1);//프로그레스바 보여주기 위해 포지션 맨 아래로 이동
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    data.remove(data.size() - 1);
                    adapter.notifyItemRemoved(data.size());
                    pagingBoards();//게시물 새로 불러오기
//                  adapter.notifyDataSetChanged();
                    adapter.setLoaded();//뷰홀더 타입 되돌리기

                }
            }, 2000);
    }

    private void deleteBoardItem(int position){

        Log.d(TAG, "deleteBoardItem: data.get(position).getId(): " + data.get(position).getId());
        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardItem> call = api.boardRemove(data.get(position).getId());

        call.enqueue(new Callback<BoardItem>() {
            @Override
            public void onResponse(Call<BoardItem> call, Response<BoardItem> response) {

                BoardItem boardResponse = response.body();
                Log.d(TAG, "onResponse: successfully deleted");

                //refresh
                finish();
                startActivity(getIntent());
            }

            @Override
            public void onFailure(Call<BoardItem> call, Throwable t) {
                Log.d("Error!",t.getMessage());
            }
        });
    }

    @Override
    public void onItemClicked(int position) {

        Toast.makeText(this, "position: " + position, Toast.LENGTH_SHORT).show();

        //log
        Log.d(TAG, "onItemClicked: content: " + data.get(position).getId());
    }

    //리사이클러뷰 설정버튼
    public void onMoreClicked(int position) {

        Log.d(TAG, "메뉴 이미지 클릭");
        pos = position;
        AlertDialog.Builder ad = new AlertDialog.Builder(BoardActivity.this);
        Log.d(TAG, "onMoreClicked: getwriter(): " + data.get(position).getWriter());
        if (loggedUseremail.equals(data.get(position).getWriter())) {

            ad.setTitle("게시물");       // 제목 설정
            //ad.setMessage("편집");   // 내용 설정

            // 수정 버튼 설정
            ad.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(BoardActivity.this, BoardUpdateActivity.class);
                    Log.d(TAG, "onClick: pos: " + pos);
                    intent.putExtra("id", data.get(pos).getId());
                    intent.putExtra("writer", data.get(pos).getWriter());
                    intent.putExtra("title", data.get(pos).getTitle());
                    intent.putExtra("content", data.get(pos).getContent());
                    intent.putExtra("datetime", data.get(pos).getDatetime());

                    data2 = new ArrayList<>(Arrays.asList(data.get(pos).getBoardImageItems()));
                    intent.putExtra("images", data2);
                    Log.d(TAG, "onClick: images: " + data2);
                    startActivity(intent);

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
            // 삭제 버튼 설정
            ad.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    deleteBoardItem(pos);
                    dialog.dismiss();//닫기

                }
            });
            // 창 띄우기
            ad.show();
        } else {
            ad.setTitle("게시물");       // 제목 설정
            ad.show();
        }
    }

    //좋아요 클릭
    public void onLikeClicked(int position){

        pos = position;
        int boardId = data.get(position).getId();

        Log.d(TAG, "onLikeClicked: loggeduserid: " + loggedUserid);
        Log.d(TAG, "onLikeClicked: boardId: " + boardId);

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardItem> call = api.boardLike(loggedUserid, boardId);

        call.enqueue(new Callback<BoardItem>() {
            @Override
            public void onResponse(Call<BoardItem> call, Response<BoardItem> response) {

                Log.d(TAG, "onResponse: like getisLiked: " + response.body().getIsLiked());
                Log.d(TAG, "onResponse: like likecount: " + response.body().getLikeCount());
                Log.d(TAG, "onResponse: locallike: " + data.get(pos).getLikeCount());

                //isLiked값 받아와서 좋아요 버튼 바꾸기
                data.get(pos).setIsLiked(response.body().getIsLiked());
                data.get(pos).setLikeCount(data.get(pos).getLikeCount()+1);
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void onFailure(Call<BoardItem> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }


    public void onProfileClicked(int position){

        Intent intent = new Intent(BoardActivity.this, UserPageActivity.class);
        String writer = data.get(position).getWriter(); // 해당 게시물 유저 이메일
        Log.d(TAG, "onProfileClicked: thiswriter: " + writer);
        intent.putExtra("userEmail", writer);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onUnlikeClicked(int position){
        pos = position;
        int boardId = data.get(position).getId();

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<BoardItem> call = api.boardLike(loggedUserid, boardId);

        call.enqueue(new Callback<BoardItem>() {
            @Override
            public void onResponse(Call<BoardItem> call, Response<BoardItem> response) {

                Log.d(TAG, "onResponse: unlike getisLiked: " + response.body().getIsLiked());
                Log.d(TAG, "onResponse: unlike getLikeCount: " + response.body().getLikeCount());
                Log.d(TAG, "onResponse: localunlike: " + data.get(pos).getLikeCount());
                data.get(pos).setIsLiked(response.body().getIsLiked());
                data.get(pos).setLikeCount(data.get(pos).getLikeCount()-1);
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void onFailure(Call<BoardItem> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }

    public void onLikeCountClicked(int position){

        Intent intent = new Intent(BoardActivity.this, BoardLikeListActivity.class);
        intent.putExtra("board_id", data.get(position).getId());
        Log.d(TAG, "onLikeCountClicked: data.get(position).getId() " + data.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onCommentClicked(int position) {
        Intent intent = new Intent(BoardActivity.this, BoardCommentActivity.class);
        int boardid = data.get(position).getId();
        intent.putExtra("boardid", boardid);
        startActivity(intent);
    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");

        initViews();
//        finish();
//        startActivity(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        currentVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
        Log.d(TAG, "onPause: currentvisibleposition: " + currentVisiblePosition);
        Log.d(TAG, "onPause() called");
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


}
