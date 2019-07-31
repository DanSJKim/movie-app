package com.example.retrofitexample.Map;

import android.content.Intent;
import android.net.Uri;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.BoxOffice.BoxOfficeAdapter;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.LoginRegister.MainActivity;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TheaterDetailActivity extends AppCompatActivity {

    private static final String TAG = "TheaterDetailActivity";
    private ArrayList<TheaterDetail> mArrayList;

    ImageView ivPoster; // 영화관 대표 이미지
    TextView tvTheaterName; // 영화관 이름
    TextView tvTheaterAddress; // 영화관 주소
    TextView tvTheaterTel; // 영화관 전화 번호
    TextView tvTheaterSeats; // 영화관 총 좌석 수
    Button btnBack; // 뒤로 가기

    private RecyclerView theaterDetailRecyclerview;
    private TheaterDetailAdapter theaterDetailAdapter;

    private String theaterUrl;

    String url; // 영화관 url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater_detail);

        ivPoster = (ImageView) findViewById(R.id.ivTheaterPoster);
        tvTheaterName = (TextView) findViewById(R.id.tvTheaterName);
        tvTheaterAddress = (TextView) findViewById(R.id.tvtheaterAddress);
        tvTheaterTel = (TextView) findViewById(R.id.tvTheaterTelNum);
        tvTheaterSeats = (TextView) findViewById(R.id.tvTheaterSeats);

        Intent intent = getIntent();
        String name = intent.getStringExtra("theaterName"); // 영화관 이름
        String address = intent.getStringExtra("theaterAddress"); // 영화관 주소

        tvTheaterName.setText(name);
        tvTheaterAddress.setText(address);

        MaterialButton button = (MaterialButton) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(TheaterDetailActivity.this, "Hello World", Toast.LENGTH_LONG).show();
                //URL urlresult = new URL(url);
                Intent intent = new Intent(TheaterDetailActivity.this, TheaterDetailWebviewActivity.class);
                intent.putExtra("url", url);
                Log.d(TAG, "onClick: url: " + url);
                startActivity(intent);
            }
        });

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Log.d(TAG, "onCreate: name: " + name);
        Log.d(TAG, "onCreate: address: " + address);

        theaterDetail(name, address);


    }


    private void theaterDetail(String name, String address){

        Api api = ApiClient.getClient().create(Api.class);
        Call<TheaterDetailResponse> call = api.theaterDetail(name, address);
        Log.d(TAG, "theaterDetail: name: " + name);
        Log.d(TAG, "theaterDetail: address: " + address);
        call.enqueue(new Callback<TheaterDetailResponse>() {
            @Override
            public void onResponse(Call<TheaterDetailResponse> call, final Response<TheaterDetailResponse> response) {

                //배열을 arraylist로 변환
                mArrayList = new ArrayList<>(Arrays.asList(response.body().getTheaterdetail()));

                Log.d(TAG, "onResponse: poster: " + mArrayList.get(0).getTheaterPosterImg());
                String posterimg = mArrayList.get(0).getTheaterPosterImg();
                String tel = mArrayList.get(0).getTheaterTelNum();
                String seats = mArrayList.get(0).getTheaterSeats();
                String moviename[] = mArrayList.get(0).getMovieName();
                url = mArrayList.get(0).getUrl();
                Log.d(TAG, "onResponse: getUrl(): " + url);
                Log.d(TAG, "onResponse: size: " + moviename.length);
//                String movietime[] = mArrayList.get(0).getMovieTime();

                GlideApp.with(TheaterDetailActivity.this).load(posterimg)
                        .override(300,400)
                        .into(ivPoster);

                tvTheaterTel.setText(tel);
                tvTheaterSeats.setText(seats);

                theaterDetailRecyclerview = (RecyclerView)findViewById(R.id.theater_recyclerview);
                theaterDetailRecyclerview.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                theaterDetailRecyclerview.setLayoutManager(layoutManager);

                theaterDetailAdapter = new TheaterDetailAdapter(mArrayList, moviename);//mArrayList의 크기는 객체 한개 고정이기 때문에, 시간표에 있는 영화 개수만큼 리사이클러뷰를 돌리기 위해 moviename 배열을 넣어준다.
                //theaterDetailAdapter.setOnClickListener(TheaterDetailActivity.this);
                theaterDetailRecyclerview.setAdapter(theaterDetailAdapter);
            }

            @Override
            public void onFailure(Call<TheaterDetailResponse> call, Throwable t) {
                Log.d("Error",t.getMessage());
            }
        });
    }
}
