package com.example.retrofitexample.LoginRegister;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.BoxOffice.ProfileActivity;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity : ";

    EditText useremail_input,password_input;
    TextView register;
    Button btnLogin;
    Vibrator v;

    //change this to match your url
    final String loginURL = "http://13.209.49.7/movieApp/retrofit/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useremail_input = findViewById(R.id.userEmail);
        password_input = findViewById(R.id.loginPassword);
        register = findViewById(R.id.register);
        btnLogin = findViewById(R.id.btnLogin);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserData();
            }
        });

        //when someone clicks on login
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
            }
        });




    }

    //사용자 인증을 위해 서버로 전송되는 사용자 이름과 암호를 입력합니다.
    private void validateUserData() {

        //first getting the values
        final String useremail = useremail_input.getText().toString();
        final String password = password_input.getText().toString();

        //checking if useremail is empty
        if (TextUtils.isEmpty(useremail)) {
            useremail_input.setError("Please enter your email");
            useremail_input.requestFocus();
            // Vibrate for 100 milliseconds
            v.vibrate(100);
            btnLogin.setEnabled(true);
            return;
        }
        //checking if password is empty
        if (TextUtils.isEmpty(password)) {
            password_input.setError("Please enter your password");
            password_input.requestFocus();
            //Vibrate for 100 milliseconds
            v.vibrate(100);
            btnLogin.setEnabled(true);
            return;
        }



        //Login User if everything is fine
        loginUser(useremail,password);


    }

    private void loginUser(String useremail, String password) {
        Log.d(TAG, "loginUser");

        //making api call
        Api api = ApiClient.getClient().create(Api.class);
        Call<Model> login = api.login(useremail,password);

        login.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                Log.d(TAG, "onResponse");

                //데이터 사용자가 데이터베이스에 입력 한 데이터와 일치하면 SharedPreferences에 저장된 사용자 이름으로 응답을 보내고 ActivityProfile이 시작됩니다.
                //php로부터 받은 getIsSuccess값이 1이면
                if(response.body().getIsSuccess() == 1){
                    //get useremail
                    String user = response.body().getUsername();

                    //get email
                    String email = response.body().getEmail();

                    //get id
                    int id = response.body().getId();

                    //storing the user in shared preferences
                    SharedPref.getInstance(MainActivity.this).storeUserName(user);
                    SharedPref.getInstance(MainActivity.this).storeUserEmail(email);
                    SharedPref.getInstance(MainActivity.this).storeUserId(id);
                    Log.d(TAG, "onResponse: user: " + user);
                    Log.d(TAG, "onResponse: email: " + email);
                    Log.d(TAG, "onResponse: id: " + id);
//                    Toast.makeText(MainActivity.this,response.body().getUsername(),Toast.LENGTH_LONG).show();

                    finish();
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                }else{
                    Toast.makeText(MainActivity.this,response.body().getMessage(),Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Toast.makeText(MainActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });


    }

}
