package com.example.retrofitexample.MyPage;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.Image.Img_Pojo;
import com.example.retrofitexample.LoginRegister.MainActivity;
import com.example.retrofitexample.MyPage.OpenCV.OpenCVActivity;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;
import com.example.retrofitexample.LoginRegister.Model;
import com.example.retrofitexample.LoginRegister.SharedPref;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Streaming.PlayerActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.bcdev.librarysdkew.utils.ToastMsg;
import info.bcdev.librarysdkew.wallet.SendingToken;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageUpdateActivity extends AppCompatActivity {

    public static final String TAG = "MyPageUpdateActivity : ";

    ImageView Profile;
    EditText Name, Password;
    Button Submit, Back;
    String holder_password, holder_name;
    int loggedUserid;

    //이미지 변수
    private static final int IMAGE = 100;
    Bitmap bitmap;
    String responseImg;//서버로부터 응답 이미지

    //OpenCV
    private static final int OPENCV = 200;

    AlertDialog.Builder builder; // 카메라 선택 다이얼로그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page_update);

        Profile = (ImageView) findViewById(R.id.ivUpdateProfileImage);
        Name = (EditText) findViewById(R.id.etUpdateName);
        Password = (EditText) findViewById(R.id.etUpdatePassword);
        Submit = (Button) findViewById(R.id.btnSubmit);
        Back = (Button) findViewById(R.id.btnBack);

        String loggedUsername = SharedPref.getInstance(this).LoggedInUser();//회원 이름
        Log.d(TAG, "onCreate: loggdUsername: " + loggedUsername);
        final String loggedUseremail = SharedPref.getInstance(this).LoggedInEmail();//회원 이메일
        Log.d(TAG, "onCreate: loggedinEamil: " + loggedUseremail);
        loggedUserid = SharedPref.getInstance(this).LoggedInId();//회원 번호
        Log.d(TAG, "onCreate: loggedUserid: " + loggedUserid);

        Name.setText(loggedUsername);

        String sharedImg = SharedPref.getInstance(this).StoredProfileImage();
        GlideApp.with(this).load(sharedImg)
                .override(300,400)
                .into(Profile);

        //확인
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 확인 버튼 클릭");

                holder_name = Name.getText().toString();
                holder_password = Password.getText().toString();
                updateUser(loggedUseremail, holder_name, holder_password);
            }
        });

        //돌아가기
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 돌아가기 버튼 클릭");
                finish();
            }
        });

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 프로필 사진 클릭");

                show();
            }
        });
    }

    private void updateUser(String useremail, String username, String password) {

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<Model> updatemypage = api.updatemypage(useremail, username, password);

        updatemypage.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {

                //데이터 사용자가 데이터베이스에 입력 한 데이터와 일치하면 SharedPreferences에 저장된 사용자 이름으로 응답을 보내고 ActivityProfile이 시작됩니다.
                //php로부터 받은 getIsSuccess값이 1이면
                if(response.body().getIsSuccess() == 1){

                    //get username
                    String user = response.body().getUsername();

                    //storing the user in shared preferences
                    SharedPref.getInstance(MyPageUpdateActivity.this).storeUserName(user);
                    Log.d(TAG, "onResponse: response.body().getUsername(): " + user);

                    if(bitmap != null) {
                        Log.d(TAG, "onResponse: bitmap != null");
                        uploadImage();
                    }else{
                        Log.d(TAG, "onResponse: bitmap else");
                        finish();
                    }

                }else{
                    Log.e(TAG, "onResponse: else");
                    Toast.makeText(MyPageUpdateActivity.this,response.body().getMessage(),Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                Toast.makeText(MyPageUpdateActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void uploadImage(){
        Log.d(TAG, "uploadImage: 데이터베이스에 이미지 업로드");

        String image = convertToString();
        // 이미지 파일 이름 ( img_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageName = "img_" + timeStamp + "_";

        Log.d(TAG, "uploadImage: image: " + image);
        Log.d(TAG, "uploadImage: imageName: " + imageName);
        Log.d(TAG, "uploadImage: loggedUserid: " + loggedUserid);

        Api apiInterface = ApiClient.getClient().create(Api.class);
        Call<MyPage_Img> call = apiInterface.uploadProfileImage(imageName, image, loggedUserid);

        call.enqueue(new Callback<MyPage_Img>() {
            @Override
            public void onResponse(Call<MyPage_Img> call, Response<MyPage_Img> response) {
                Log.d(TAG, "onResponse: success!");

                //서버에 업로드 된 이미지 경로 받아오기
                MyPage_Img myPage_img = response.body();
                Log.d("Server Response:",""+myPage_img.getResponse());
                responseImg = myPage_img.getResponse();

                //이미지 경로 SharedPreferences에 저장
                SharedPref.getInstance(MyPageUpdateActivity.this).storeProfileImage(responseImg);

                finish();//액티비티 종료
            }

            @Override
            public void onFailure(Call<MyPage_Img> call, Throwable t) {
                Log.d("Server Response fail!: ",""+t.toString());

            }
        });
    }


    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE);
    }

    //Bitmap -> String 변환
    private String convertToString()
    {
        Log.d(TAG, "convertToString: Bitmap을 String으로 변환 한다.");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //(압축 옵션( JPEG, PNG ), 품질 설정 ( 0 - 100까지의 int형 ), 압축된 바이트배열을 담을 stream)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        //imgByte는 세번째 인자인 byteArrayOutputStream의 toByteArray() 메서드를 통해 반환받을 수 있다.
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte,Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE && resultCode == RESULT_OK && data != null)
        {
            Log.d(TAG, "onActivityResult: 갤러리 결과");
            Uri path = data.getData();
            Log.d(TAG, "onActivityResult: path: " + path);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);

                //대댓글 프로필 이미지
                GlideApp.with(this).load(path)
                        .override(300,400)
                        .into(Profile);
                Log.d(TAG, "onActivityResult: glide success");

            } catch (IOException e) {
                Log.e(TAG, "onActivityResult: error: ", e);
                e.printStackTrace();
            }
        }else if(requestCode == OPENCV && resultCode == RESULT_OK && data != null){
            Log.d(TAG, "onActivityResult: OpenCV 결과");
        }
    }

    // 카메라 선택 다이얼로그
    void show()
    {
        Log.d(TAG, "show: 이미지 업로드");
        builder = new AlertDialog.Builder(this);
        builder.setTitle("이미지 업로드");
        //builder.setMessage("VTC 잔액: ");
        builder.setPositiveButton("OPENCV",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MyPageUpdateActivity.this, OpenCVActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("갤러리",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        selectImage();
                        dialog.dismiss();
                    }
                });
        builder.setNeutralButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        builder.show();
    }

}
