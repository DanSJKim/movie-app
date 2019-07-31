package com.example.retrofitexample.LoginRegister;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//사용자가 데이터를 입력하면 우리는 데이를 확인한 다음 사용자를 등록합니다.
//등록이 성공하면 로그인 활동으로 이동합니다.
public class RegisterActivity extends AppCompatActivity {
    EditText username,email,password,cpassword;
    TextView login;
    Button btnRegister;
    Vibrator v;
    //change to your register url
    final String registerUrl = "http://13.209.49.7/movieApp/retrofit/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText) findViewById(R.id.registerName);
        email = (EditText) findViewById(R.id.registerEmail);
        password = (EditText) findViewById(R.id.confirmpassword);
        cpassword = (EditText) findViewById(R.id.confirmpassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        login = (TextView) findViewById(R.id.login);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserData();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void validateUserData() {

        //find values
        final String reg_name = username.getText().toString();
        final String reg_email = email.getText().toString();
        final String reg_password = cpassword.getText().toString();
        final String reg_cpassword = cpassword.getText().toString();


//        checking if username is empty
        if (TextUtils.isEmpty(reg_name)) {
            username.setError("Please enter username");
            username.requestFocus();
            // Vibrate for 100 milliseconds
            v.vibrate(100);
            return;
        }
        //checking if email is empty
        if (TextUtils.isEmpty(reg_email)) {
            email.setError("Please enter email");
            email.requestFocus();
            // Vibrate for 100 milliseconds
            v.vibrate(100);
            return;
        }
        //checking if password is empty
        if (TextUtils.isEmpty(reg_password)) {
            password.setError("Please enter password");
            password.requestFocus();
            //Vibrate for 100 milliseconds
            v.vibrate(100);
            return;
        }
        //validating email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(reg_email).matches()) {
            email.setError("Enter a valid email");
            email.requestFocus();
            //Vibrate for 100 milliseconds
            v.vibrate(100);
            return;
        }
        //checking if password matches
        if (!reg_password.equals(reg_cpassword)) {
            password.setError("Password Does not Match");
            password.requestFocus();
            //Vibrate for 100 milliseconds
            v.vibrate(100);
            return;
        }

        //After Validating we register User
        registerUser(reg_name,reg_email,reg_password);

    }

    private void registerUser(String user_name, String user_mail, String user_pass) {


        final String reg_username = username.getText().toString();
        final String reg_email = email.getText().toString();
        final String reg_password = cpassword.getText().toString();

        //call retrofit
        //making api call
        //Retrofit객체의 create() method와 미리 정의해둔 Interface를 이용하여 실질적으로 사용할 클라이언트 객체를 생성합니다.
        //이렇게 생성된 클라이언트 객체를 이용하여 요청을 보내고 받아온 응답을 가지고 특정 작업을 하고나면 해당 Retrofit객체의 사용은 끝이나게 되지요.
        Api api = ApiClient.getClient().create(Api.class);
        Call<Model> login = api.register(user_name,user_mail,user_pass);

        //생성된 클라이언트 객체가 제공하는 enqueue() method를 이용하여 요청을 처리하였고, 해당 method에 익명의 Callback Interface를 구현하여 주었습니다.
        //해당 Callback Interface는 onResponse와 onFailure method를 구현해주어야 한다.
        login.enqueue(new Callback<Model>() {

            //요청이 성공된 경우
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {

                if(response.body().getIsSuccess() == 1){
                    //get username
                    String user = response.body().getUsername();

                    Toast.makeText(RegisterActivity.this,response.body().getUsername(),Toast.LENGTH_LONG).show();

                    startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                }else{
                    Toast.makeText(RegisterActivity.this,response.body().getMessage(),Toast.LENGTH_LONG).show();
                }

            }

            //요청이 실패한 경우
            //API 서버의 다운 / DB Query 중 오류 등와 같은 서버의 비정상적인 동작으로 인해 요청에 대한 응답을 받지 못하는 경우
            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();

            }
        });




    }

}