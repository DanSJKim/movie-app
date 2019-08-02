package com.example.retrofitexample.Chat;

import android.content.Intent;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.Board.BoardActivity;
import com.example.retrofitexample.Board.BoardItemAdapter;
import com.example.retrofitexample.Board.BoardResponse;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.Chat.Model.Room;
import com.example.retrofitexample.Chat.Model.UserInfo;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.retrofitexample.BoxOffice.ProfileActivity.loggedUseremail;


/**
 * 게시판의 유저 프로필 사진을 클릭하면 유저 정보와 채팅, 영상통화 버튼을 표시해 주는 액티비티
 */

public class UserPageActivity extends AppCompatActivity {

    public static final String TAG = "UserPageActivity : ";

    CircleImageView civProfile;
    TextView tvName;
    TextView tvEmail;
    ImageView ivChat; // 채팅 버튼
    ImageView ivVideoCall;

    String yourEmail;// BoardActivity -> UserPageActivity 이메일 인텐트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        //초기화
        civProfile = (CircleImageView) findViewById(R.id.civUserPageProfile);
        tvName = (TextView) findViewById(R.id.tvUserPageName);
        tvEmail = (TextView) findViewById(R.id.tvUserPageEmail);
        ivChat = (ImageView) findViewById(R.id.ivUserPageChat);
        ivVideoCall = (ImageView) findViewById(R.id.ivUserPageVideoCall);

        // 게시물 작성자의 이메일을 받아온다.
        Intent intent = getIntent();
        yourEmail = intent.getStringExtra("userEmail");// BoardActivity -> UserPageActivity 이메일 인텐트
        Log.d(TAG, "onCreate: yourEmail: " + yourEmail);

        getUserInfo(yourEmail);

        if(loggedUseremail.equals(yourEmail)){
            ivChat.setVisibility(View.GONE);
            ivVideoCall.setVisibility(View.GONE);
        }

        // 상대와 채팅 버튼
        ivChat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // 방 번호를 가져온 후 채팅방 인텐트로 넘겨 준다.
                getRoomNo(loggedUseremail, yourEmail);
            }
        });

        ivVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 방 번호를 가져온 후 채팅방 인텐트로 넘겨 준다.
                getRoomNoVideoCall(loggedUseremail, yourEmail);
            }
        });
    }

    // 방 번호 가져오기
    private void getRoomNo(String myEmail, final String yourEmail){

        Api api = ApiClient.getClient().create(Api.class);
        Call<Room> call = api.CheckRoomExistOrNot(myEmail, yourEmail);

        call.enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                Log.d(TAG, "onResponse: roomNo: " + response.body().getRoomNo());

                int roomNo = response.body().getRoomNo();

                Intent intent = new Intent(UserPageActivity.this, ChatRoomActivity.class);
                intent.putExtra("roomNo", roomNo);
                intent.putExtra("yourEmail", yourEmail);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {

            }
        });
    }

    // 방 번호 가져오기
    private void getRoomNoVideoCall(String myEmail, final String yourEmail){

        Api api = ApiClient.getClient().create(Api.class);
        Call<Room> call = api.CheckRoomExistOrNot(myEmail, yourEmail);

        call.enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                Log.d(TAG, "onResponse: roomNo: " + response.body().getRoomNo());

                int roomNo = response.body().getRoomNo();

                Intent intent = new Intent(UserPageActivity.this, ChatRoomActivity.class);
                intent.putExtra("roomNo", roomNo);
                intent.putExtra("yourEmail", yourEmail); // 수신자
                intent.putExtra("videoCall", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {

            }
        });
    }

    // 채팅 할 상대방 이메일을 이용해서 해당 유저의 사진, 이름, 이메일을 서버에서 가져오기
    private void getUserInfo(String email){

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<UserInfo> call = api.getUserPageInfo(email); // from ProfileActivity -> public static String loggedUseremail
        Log.d(TAG, "getUserInfo: email: " + email);
        call.enqueue(new Callback<UserInfo>() {

            @Override
            public void onResponse(Call<UserInfo> call, final Response<UserInfo> response) {
                Log.d(TAG, "onResponse: " + response.body());
                //리사이클러뷰 게시물 목록 불러오기

                String profile = response.body().getUserprofile(); // 프로필 사진
                String name = response.body().getUsername(); // 유저 이름
                String email = response.body().getUseremail(); // 유저 이메일
                Log.d(TAG, "onResponse: email: " + email);
                Log.d(TAG, "onResponse: name: " + name);
                Log.d(TAG, "onResponse: profile: " + profile);

                // 프로필 사진
                GlideApp.with(UserPageActivity.this).load(response.body().getUserprofile())
                        .override(300,400)
                        .into(civProfile);

                tvName.setText(name);
                tvEmail.setText(email);

            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());

            }
        });
    }
}
