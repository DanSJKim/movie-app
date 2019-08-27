package com.example.retrofitexample.Streaming;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;

/**
 * 스트리밍 방을 생성하는 액티비티
 */

public class CreateStreamingActivity extends AppCompatActivity {
    private static final String TAG = "CreateStreamingActivi: ";
    EditText etRoomName; // 입력한 스트리밍 방 이름
    Button btnCreate; // 스트리밍 방 만들기
    Button btnCancel; // 스트리밍 방 만들기 취소

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_streaming);

        //뷰 초기화
        etRoomName = (EditText) findViewById(R.id.etStreamingRoomName); // 방제 입력 에디트텍스트
        btnCreate = (Button) findViewById(R.id.btnCreateStreamingRoom); // 방 만들기
        btnCancel = (Button) findViewById(R.id.btnCreateStreamingRoomCancel); // 방 만들기 취소

        btnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 방 만들기 버튼 클릭");
                
                finish(); // 액티비티 종료
                Intent intent = new Intent(CreateStreamingActivity.this, StreamingActivity.class); // 이동 할 클래스
                String roomName = etRoomName.getText().toString(); // Edittext에 입력 한 방 이름
                intent.putExtra("roomName", roomName); // 방 이름 intent로 전송
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 버튼 중복 클릭 방지
                startActivity(intent); // intent 시작
                overridePendingTransition(0,0); // 액티비티 전환 애니메이션 제거

                // 키보드 내려주기
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(btnCreate.getWindowToken(),0);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 방 만들기 취소");

                finish(); // 액티비티 종료
            }
        });
    }
}
