package com.example.retrofitexample.Image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.retrofitexample.GlideApp;
import com.example.retrofitexample.R;
import com.example.retrofitexample.Retrofit.Api;
import com.example.retrofitexample.Retrofit.ApiClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageActivity extends AppCompatActivity{

    Bitmap bitmap;
    ImageView imageView, imageView2;
    Button selectImg,uploadImg;
    EditText imgTitle;
    private  static final int IMAGE = 100;
    String responseImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = (ImageView) findViewById(R.id.imageView);
        selectImg = (Button) findViewById(R.id.selectImg);
        uploadImg = (Button) findViewById(R.id.uploadImg);
        imgTitle = (EditText) findViewById(R.id.imgTitle);
        imageView2 = (ImageView) findViewById(R.id.responseImg);


        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage();

            }
        });

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImage();

            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getImage();
            }
        });

    }


    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE);
    }

    private String convertToString()
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte,Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== IMAGE && resultCode==RESULT_OK && data!=null)
        {
            Uri path = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(){

        String image = convertToString();
        String imageName = imgTitle.getText().toString();
        Api apiInterface = ApiClient.getClient().create(Api.class);
        Call<Img_Pojo> call = apiInterface.uploadImage(imageName,image);

        call.enqueue(new Callback<Img_Pojo>() {
            @Override
            public void onResponse(Call<Img_Pojo> call, Response<Img_Pojo> response) {

                Img_Pojo img_pojo = response.body();
                Log.d("Server Response",""+img_pojo.getResponse());
                responseImg = img_pojo.getResponse();

            }

            @Override
            public void onFailure(Call<Img_Pojo> call, Throwable t) {
                Log.d("Server Response",""+t.toString());

            }
        });

    }

    private void getImage(){
//        GlideApp.with(this).load(responseImg)
//                .override(300,400)
//                .into(imageView2);

        Glide.with(this)
                .load(responseImg)
                .into(imageView2);
    }

}