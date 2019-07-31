package com.example.retrofitexample.MyPage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;

public class MyPageActivity extends AppCompatActivity {

    public static final String TAG = "MyPageActivity : ";

    ImageView userProfile;
    TextView userEmail, userName;

    Button MyPage, Update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        Log.d(TAG, "onCreate() called");

        userProfile = (ImageView) findViewById(R.id.ivProfile);
        userEmail = (TextView) findViewById(R.id.tvEmail);
        userName = (TextView) findViewById(R.id.tvName);

        MyPage = (Button) findViewById(R.id.btnMyPage);
        Update = (Button) findViewById(R.id.btnUpdate);

        String loggedUsername = SharedPref.getInstance(this).LoggedInUser();
        String loggedUseremail = SharedPref.getInstance(this).LoggedInEmail();
        String profileImage = SharedPref.getInstance(this).StoredProfileImage();
        Log.d(TAG, "onCreate: loggedUserName: " + loggedUsername);
        Log.d(TAG, "onCreate: loggedUserEmail: " + loggedUseremail);
        Log.d(TAG, "onCreate: profileImage: " + profileImage);

        userEmail.setText(loggedUseremail);
        userName.setText(loggedUsername);

        MyPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPageActivity.this, MyPageUpdateActivity.class);
                startActivity(intent);
            }
        });

        GlideApp.with(this).load(profileImage)
                .override(300,400)
                .into(userProfile);

    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");

        //refresh
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

    }

    @Override
    protected void onPause() {
        super.onPause();
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
        finish();
    }
}
