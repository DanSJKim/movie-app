package com.example.retrofitexample.MovieSearch;

import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.example.retrofitexample.Board.BoardItemAdapter;
import com.example.retrofitexample.Board.BoardResponse;
import com.example.retrofitexample.BoxOffice.Item.Movie;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieSearchActivity extends AppCompatActivity implements DataAdapter.MovieRecyclerviewClickListener{

    public static final String TAG = "MovieSearchActivity : ";

    private RecyclerView mRecyclerView;
    private ArrayList<Movie> mArrayList;
    private DataAdapter mAdapter;

    int itemLimit = 10;//서버에서 불러오는 아이템 개수
    int itemTotalCount = 10;

    String searchText = "";

    ImageView Home, Board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search);

        Home = (ImageView) findViewById(R.id.ivHome);
        Board = (ImageView) findViewById(R.id.ivBoard);

        Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovieSearchActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        Board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovieSearchActivity.this, BoardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });


//        //초기화
//        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
//
//        //툴바 설정
//        toolbar.setTitleTextColor(Color.parseColor("#ffff33")); //제목의 칼라
//        toolbar.setNavigationIcon(R.mipmap.ic_launcher); //제목앞에 아이콘 넣기
//        setSupportActionBar(toolbar); //툴바를 액션바와 같게 만들어 준다.

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        firstloadJSON();
    }

    private void initViews(){
        mRecyclerView = (RecyclerView)findViewById(R.id.movie_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void firstloadJSON(){
        Api api = ApiClient.getClient().create(Api.class);
        Call<SearchResponse> call = api.searchedList();
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, final Response<SearchResponse> response) {

                Log.d("TAG", "onResponse: " + response.body().getSearchlist());

                //영화 검색해서 받아오는 값이 없으면
                if(response.body().getSearchlist() == null){
                    Log.d(TAG, "onResponse: is null "+response.body().getSearchlist());

                    mRecyclerView = (RecyclerView)findViewById(R.id.movie_recycler_view);
                    mRecyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MovieSearchActivity.this);
                    mRecyclerView.setLayoutManager(layoutManager);

                    mArrayList = new ArrayList<>();
                    mAdapter = new DataAdapter(mArrayList);
                    mAdapter.setOnClickListener(MovieSearchActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }else{
                    Log.d(TAG, "onResponse: is not null "+response.body().getSearchlist());
                    SearchResponse searchResponse = response.body();

                    mRecyclerView = (RecyclerView)findViewById(R.id.movie_recycler_view);
                    mRecyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MovieSearchActivity.this);
                    mRecyclerView.setLayoutManager(layoutManager);

                    mArrayList = new ArrayList<>(Arrays.asList(searchResponse.getSearchlist()));
                    mAdapter = new DataAdapter(mArrayList);
                    mAdapter.setOnClickListener(MovieSearchActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d("firstloadJSON Error",t.getMessage());
            }
        });
    }

    private void loadJSON(){
        Api api = ApiClient.getClient().create(Api.class);
        Call<SearchResponse> call = api.searchMovies(searchText, itemLimit);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, final Response<SearchResponse> response) {

                //Log.d("TAG", "onResponse: " + response.body().getSearchlist());

                //영화 검색해서 받아오는 값이 없으면
                if(response.body().getSearchlist() == null){
                    Log.d(TAG, "onResponse: is null "+response.body().getSearchlist());
                    mArrayList = new ArrayList<>();
                    mAdapter = new DataAdapter(mArrayList);
                    mAdapter.setOnClickListener(MovieSearchActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }else{
                    Log.d(TAG, "onResponse: is not null "+response.body().getSearchlist());
                    SearchResponse searchResponse = response.body();
                    mArrayList = new ArrayList<>(Arrays.asList(searchResponse.getSearchlist()));
                    mAdapter = new DataAdapter(mArrayList);
                    mAdapter.setOnClickListener(MovieSearchActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }

                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            Log.d(TAG, "loadJSON onScrolled: last!!");

                            if(response.body().getSearchlist() == null){

                            }else{
                                if(itemLimit <= response.body().getSearchlist().length){
                                    Log.d(TAG, "onScrolled: itemLimit@: " + itemLimit);
                                    itemLimit = itemLimit+10;
                                    Log.d(TAG, "onScrolled: itemlength: " + response.body().getSearchlist().length);

                                    Log.d(TAG, "onScrolled: searchText: " + searchText);
                                    if(searchText.isEmpty() || searchText == ""){

                                    }else {
                                        onLoadMore();
                                    }
                                }
                            }
                        }
                    }
                });

            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d("loadJson Error: ",t.getMessage());
            }
        });
    }

    //프로그레스바 생성
    public void onLoadMore() {
//        data.add(null);
//        adapter.notifyItemInserted(data.size() - 1);
//        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(data.size()-1);//프로그레스바 보여주기 위해 포지션 맨 아래로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                data.remove(data.size() - 1);
//                adapter.notifyItemRemoved(data.size());

                pagingBoards();//게시물 새로 불러오기
//                 adapter.notifyDataSetChanged();
//                adapter.setLoaded();//뷰홀더 타입 되돌리기

            }
        }, 2000);
    }

    //게시물 페이징 출력
    private void pagingBoards(){

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<SearchResponse> call = api.searchMovies(searchText, itemLimit);

        call.enqueue(new Callback<SearchResponse>() {

            @Override
            public void onResponse(Call<SearchResponse> call, final Response<SearchResponse> response) {
                Log.d(TAG, "onResponse: response.body().getBoardItems() " + response.body().getSearchlist());

                //progressDoalog.dismiss();
                if(response.body().getSearchlist() != null) {

                    mArrayList = new ArrayList<>(Arrays.asList(response.body().getSearchlist())); //BoardItem객체들을 받아온 배열을 리스트로 바꿔준다.
                    //어댑터
                    mAdapter = new DataAdapter(mArrayList);
                    mAdapter.setOnClickListener(MovieSearchActivity.this);
                    mRecyclerView.setAdapter(mAdapter);

                    //스크롤 이동
                    Log.d(TAG, "onResponse: itemTotalCount: " + itemTotalCount);
                    ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPosition(itemTotalCount - 5);

                    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                        }

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() + 1;
                            itemTotalCount = recyclerView.getAdapter().getItemCount();

                            //마지막에 보인 아이템 포지션 값과 전체 아이템 개수 값이 같으면 바닥으로 판단
                            if (lastVisibleItemPosition == itemTotalCount) {
                                //리스트 마지막(바닥) 도착 다음 페이지 데이터 로드
                                Log.d(TAG, "pagingBoards onScrolled: last!!");
                                Log.d(TAG, "pagingBoards onScrolled: itemTotalCount: " + itemTotalCount);
                                Log.d(TAG, "pagingBoards onScrolled: itemLimit@: " + itemLimit);//limit

                                //limit 설정한 값과 아이템 전체 개수가 같으면 실행
                                //메소드 여러번 겹쳐서 실행 되는 현상 방지
                                if (itemLimit <= itemTotalCount) {
                                    Log.d(TAG, "pagingBoards onScrolled: itemLimit@: " + itemLimit);
                                    itemLimit = itemLimit + 10;
                                    Log.d(TAG, "pagingBoards onScrolled: itemlength: " + response.body().getSearchlist().length);

                                    if(searchText.isEmpty() || searchText == ""){

                                    }else {
                                        onLoadMore();
                                    }
                                }

                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());

            }
        });
    }


    //서치뷰
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem search = menu.findItem(R.id.svMovie);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
                itemLimit = 7;
                searchText = newText;
                Log.d(TAG, "onQueryTextChange: newText: " + searchText);
                if(newText.isEmpty()){
                    Log.d(TAG, "onQueryTextChange: is empty " +searchText);
                    firstloadJSON();
                }else{
                    Log.d("newText: ", "onQueryTextChange: is not empty " +searchText);
                    loadJSON();
                }
                //mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    public void onItemClicked(int position){
        Log.d(TAG, "onItemClicked: clicked!!");

        int id = mArrayList.get(position).getMoveid();
        String title = mArrayList.get(position).getTitle();//영화 제목
        String posterimg = mArrayList.get(position).getPosterimg();//영화 이미지
        String overview = mArrayList.get(position).getOverview();
        String director = mArrayList.get(position).getDirector();//영화 감독
        String actor = mArrayList.get(position).getActor();//영화 배우
        String rating = mArrayList.get(position).getRating();//영화 등급
        String boxoffice = mArrayList.get(position).getBoxoffice();//영화 박스오피스

        Intent intent = new Intent(MovieSearchActivity.this, MovieDetailActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("posterimg", posterimg);
        intent.putExtra("overview", overview);
        intent.putExtra("director", director);
        intent.putExtra("actor", actor);
        intent.putExtra("rating", rating);
        intent.putExtra("boxoffice", boxoffice);
        startActivity(intent);
        Log.d(TAG, "onItemClicked: id: " + id);
        Log.d(TAG, "onItemClicked: director: " + mArrayList.get(position).getDirector());



        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<SearchResponse> call = api.searchedMovie(id);

        call.enqueue(new Callback<SearchResponse>() {

            @Override
            public void onResponse(Call<SearchResponse> call, final Response<SearchResponse> response) {
                Log.d(TAG, "onResponse: response.body().getBoardItems() " + response.body().getSearchlist());

                //progressDoalog.dismiss();
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());

            }
        });

    }
}
