package com.example.retrofitexample.Board.Comment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoardCommentUpdateActivity  extends Activity {
    public static final String TAG = "BoardCommentUpdate : ";

    Button cancel;
    EditText etContent;

    int cmtid;
    int visible;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_board_comment_update);

        cancel = (Button) findViewById(R.id.btnBoardCommentUpdateCancel);
        etContent = (EditText) findViewById(R.id.etBoardCommentUpdateForm);

        //받아온 댓글 번호와 내용
        Intent intent = getIntent();
        cmtid = intent.getIntExtra("cmtid",-2);
        visible = intent.getIntExtra("visible", 0);
        String content = intent.getStringExtra("content");
        Log.d(TAG, "onCreate: cmtid: " + cmtid);
        Log.d(TAG, "onCreate: content: " + content);

        etContent.setText(content);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
//        Intent intent = new Intent();
//        intent.putExtra("result", "Close Popup");
//        setResult(RESULT_OK, intent);

        String fcontent = etContent.getText().toString();
        Log.d(TAG, "mOnClose: update content: " + fcontent);
        Log.d(TAG, "mOnClose: cmtid: " + cmtid);

        Intent intent = new Intent();
        intent.putExtra("visible", visible);
        updateComment(cmtid, fcontent);
        //액티비티(팝업) 닫기
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    //댓글 수정
    private void updateComment(int cmtid, String content) {
        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<CommentItem> updateComment = api.updateComment(cmtid, content);

        updateComment.enqueue(new Callback<CommentItem>() {
            @Override
            public void onResponse(Call<CommentItem> call, Response<CommentItem> response) {
                Log.d(TAG, "onResponse: Successfully Uploaded" + response.body().getDate().toString());

                finish();
                Toast.makeText(getApplicationContext(), "댓글이 수정 되었습니다..", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<CommentItem> call, Throwable t) {
                Log.d(TAG, "onFailure: fail!");

                Toast.makeText(BoardCommentUpdateActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");

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